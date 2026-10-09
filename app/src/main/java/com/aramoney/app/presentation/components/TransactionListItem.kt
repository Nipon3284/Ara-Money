package com.aramoney.app.presentation.components

import com.aramoney.app.presentation.theme.AraTheme

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import com.aramoney.app.presentation.theme.isAppInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.aramoney.app.data.local.entity.CategoryEntity
import com.aramoney.app.data.local.entity.TransactionEntity
import com.aramoney.app.domain.model.DebtCategoryConstants
import com.aramoney.app.presentation.theme.DeepBerry
import com.aramoney.app.presentation.theme.DeepBerryDark
import com.aramoney.app.presentation.theme.ErrorSoftRed
import com.aramoney.app.presentation.theme.PrimarySakuraPink
import com.aramoney.app.presentation.theme.SuccessMintGreen
import com.aramoney.app.presentation.theme.SurfaceCard
import com.aramoney.app.presentation.theme.SurfaceCardDark
import com.aramoney.app.presentation.theme.SurfaceElevated
import com.aramoney.app.presentation.theme.SurfaceElevatedDark
import com.aramoney.app.presentation.theme.softShadow
import com.aramoney.app.util.CurrencyFormatter
import com.aramoney.app.util.DateTimeUtil

/**
 * Item transaksi keuangan dalam daftar dengan gesture SwipeToDismissBox,
 * penanda pemasukan/pengeluaran pastel, badge split bill, dan bayangan lembut.
 */
@Composable
fun TransactionListItem(
    transaction: TransactionEntity,
    category: CategoryEntity?,
    onDeleteRequest: () -> Unit,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val isDark = isAppInDarkTheme()
    val isIncome = transaction.type == "INCOME"

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDeleteRequest()
                false // Jangan langsung hilangkan elemen sebelum konfirmasi dialog
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(20.dp))
                    .background(ErrorSoftRed.copy(alpha = 0.85f))
                    .padding(end = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = "Hapus transaksi",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        content = {
            Surface(
                modifier = modifier
                    .fillMaxWidth()
                    .softShadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(20.dp),
                        shadowColor = PrimarySakuraPink.copy(alpha = 0.12f)
                    )
                    .clip(RoundedCornerShape(20.dp))
                    .clickable(onClick = onClick)
                    .semantics {
                        contentDescription = "Transaksi ${category?.name ?: "Umum"}, ${if (isIncome) "Pemasukan" else "Pengeluaran"} ${CurrencyFormatter.formatRupiah(transaction.amount)}"
                    },
                shape = RoundedCornerShape(20.dp),
                color = AraTheme.colors.surfaceElevated
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Avatar icon kategori
                    val iconColor = category?.tintColorHex?.let {
                        runCatching { Color(android.graphics.Color.parseColor(it)) }.getOrNull()
                    } ?: PrimarySakuraPink

                    Box(
                        modifier = Modifier
                            .size(46.dp)
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

                    // Detail Kategori, Catatan, dan Waktu
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = category?.name ?: "Transaksi",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDark) MaterialTheme.colorScheme.onSurface else DeepBerryDark,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )

                            // Badge jika terkait pelunasan hutang/piutang (hanya ditampilkan jika bukan kategori Hutang/Piutang itu sendiri)
                            if (transaction.isSplitBillRelated && !DebtCategoryConstants.isSystemLocked(transaction.categoryId)) {
                                Surface(
                                    color = PrimarySakuraPink.copy(alpha = 0.20f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Group,
                                            contentDescription = "Hutang",
                                            tint = AraTheme.colors.accent,
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Text(
                                            text = " Hutang",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = AraTheme.colors.berry,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }

                        if (!transaction.note.isNullOrBlank()) {
                            Text(
                                text = transaction.note,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }

                        Text(
                            text = DateTimeUtil.formatTransactionDate(transaction.timestamp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }

                    // Nominal Transaksi (+ / -)
                    val amountColor = if (isIncome) {
                        AraTheme.colors.income
                    } else {
                        AraTheme.colors.expense
                    }
                    val amountPrefix = if (isIncome) "+ " else "- "

                    Text(
                        text = "$amountPrefix${CurrencyFormatter.formatRupiah(transaction.amount)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = amountColor
                    )
                }
            }
        }
    )
}
