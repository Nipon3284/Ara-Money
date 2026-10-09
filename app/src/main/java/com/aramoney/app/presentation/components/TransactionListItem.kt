package com.aramoney.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.aramoney.app.data.local.entity.CategoryEntity
import com.aramoney.app.data.local.entity.TransactionEntity
import com.aramoney.app.domain.model.DebtCategoryConstants
import com.aramoney.app.presentation.theme.AraShape
import com.aramoney.app.presentation.theme.AraTheme
import com.aramoney.app.presentation.theme.PrimarySakuraPink
import com.aramoney.app.presentation.theme.softShadow
import com.aramoney.app.util.CurrencyFormatter
import com.aramoney.app.util.DateTimeUtil

/**
 * Baris transaksi tunggal yang dipakai Beranda & Laporan.
 * - Ketuk baris untuk mengubah, tombol hapus 48dp di kanan (dengan konfirmasi + Undo).
 * - Tanpa SwipeToDismissBox (sebelumnya menyebabkan crash di LazyColumn Laporan) sehingga
 *   cara menghapus konsisten di semua layar.
 *
 * @param showTimeOnly true jika baris berada di bawah header tanggal (cukup tampilkan jam).
 */
@Composable
fun TransactionListItem(
    transaction: TransactionEntity,
    category: CategoryEntity?,
    onClick: () -> Unit,
    onDeleteRequest: () -> Unit,
    modifier: Modifier = Modifier,
    showTimeOnly: Boolean = false
) {
    val isIncome = transaction.type == "INCOME"
    val categoryName = category?.name ?: "Transaksi"
    val amountText = CurrencyFormatter.formatSigned(transaction.amount, isIncome)
    val timeText = if (showTimeOnly) {
        DateTimeUtil.formatTime(transaction.timestamp)
    } else {
        DateTimeUtil.formatTransactionDate(transaction.timestamp)
    }
    val isDebtRelated = transaction.isSplitBillRelated && !DebtCategoryConstants.isSystemLocked(transaction.categoryId)

    val a11yLabel = buildString {
        append(if (isIncome) "Pemasukan " else "Pengeluaran ")
        append(categoryName)
        append(", ")
        append(CurrencyFormatter.formatRupiah(transaction.amount))
        if (!transaction.note.isNullOrBlank()) append(", catatan ${transaction.note}")
        if (isDebtRelated) append(", terkait utang")
        append(", $timeText")
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .softShadow(
                elevation = 3.dp,
                shape = AraShape.card,
                shadowColor = PrimarySakuraPink.copy(alpha = 0.12f)
            )
            .clip(AraShape.card)
            .clickable(onClickLabel = "Ubah transaksi", onClick = onClick)
            .clearAndSetSemantics {
                contentDescription = a11yLabel
                onClick(label = "Ubah transaksi") { onClick(); true }
                customActions = listOf(
                    CustomAccessibilityAction("Hapus transaksi") { onDeleteRequest(); true }
                )
            },
        shape = AraShape.card,
        color = AraTheme.colors.surfaceElevated
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, top = 12.dp, bottom = 12.dp, end = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val iconColor = category?.tintColorHex?.let {
                runCatching { Color(android.graphics.Color.parseColor(it)) }.getOrNull()
            } ?: PrimarySakuraPink

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getIconVector(category?.iconResName ?: "category"),
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = categoryName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = AraTheme.colors.textStrong,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (isDebtRelated) {
                        Surface(
                            color = PrimarySakuraPink.copy(alpha = 0.20f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "Utang",
                                style = MaterialTheme.typography.labelSmall,
                                color = AraTheme.colors.berry,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                if (!transaction.note.isNullOrBlank()) {
                    Text(
                        text = transaction.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = timeText,
                    style = MaterialTheme.typography.labelSmall,
                    color = AraTheme.colors.textMuted
                )
            }

            Text(
                text = amountText,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (isIncome) AraTheme.colors.income else AraTheme.colors.expense,
                maxLines = 1
            )

            IconButton(onClick = onDeleteRequest) {
                Icon(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = "Hapus transaksi",
                    tint = AraTheme.colors.danger.copy(alpha = 0.75f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
