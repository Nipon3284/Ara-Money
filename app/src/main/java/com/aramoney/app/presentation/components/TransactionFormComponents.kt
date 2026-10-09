package com.aramoney.app.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.aramoney.app.data.local.entity.CategoryEntity
import com.aramoney.app.presentation.theme.AraShape
import com.aramoney.app.presentation.theme.AraTheme
import com.aramoney.app.presentation.theme.IncomeGreen
import com.aramoney.app.presentation.theme.PrimarySakuraPink
import com.aramoney.app.presentation.theme.isAppInDarkTheme
import com.aramoney.app.presentation.theme.softShadow
import com.aramoney.app.util.AmountInput
import com.aramoney.app.util.CurrencyFormatter

/** Tipe transaksi yang dipakai form Tambah & Ubah. */
enum class TransactionKind(val value: String, val label: String) {
    EXPENSE("EXPENSE", "Pengeluaran"),
    INCOME("INCOME", "Pemasukan");

    companion object {
        fun from(value: String) = if (value == INCOME.value) INCOME else EXPENSE
    }
}

/** Toggle Pengeluaran / Pemasukan dengan warna pemasukan hijau. */
@Composable
fun TransactionKindToggle(
    selected: TransactionKind,
    onSelected: (TransactionKind) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isAppInDarkTheme()
    AraSegmentedControl(
        options = TransactionKind.entries,
        selected = selected,
        onSelected = onSelected,
        label = { it.label },
        selectedColor = { if (it == TransactionKind.INCOME) (if (isDark) Color(0xFF388E3C) else IncomeGreen) else Color.Unspecified },
        modifier = modifier
    )
}

/** Tampilan nominal besar + pesan validasi inline. */
@Composable
fun AmountDisplay(
    rawAmount: String,
    kind: TransactionKind,
    modifier: Modifier = Modifier
) {
    val amount = AmountInput.toAmount(rawAmount)
    val error = AmountInput.validationMessage(rawAmount)
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .softShadow(elevation = 3.dp, shape = AraShape.card),
        shape = AraShape.card,
        color = AraTheme.colors.surfaceCard,
        border = BorderStroke(1.dp, if (error != null) AraTheme.colors.danger else AraTheme.colors.border)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 20.dp)
                .semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Nominal ${kind.label.lowercase()}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = CurrencyFormatter.formatRupiah(amount),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = if (kind == TransactionKind.EXPENSE) AraTheme.colors.expense else AraTheme.colors.income,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (error != null) {
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall,
                    color = AraTheme.colors.danger,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/** Chip nominal cepat (+10rb, +20rb, +50rb, +100rb). */
@Composable
fun QuickAmountChips(
    onQuickAdd: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val quickAmounts = listOf(10_000L, 20_000L, 50_000L, 100_000L)

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        quickAmounts.forEach { amount ->
            val label = "+${amount / 1000}rb"
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 44.dp)
                    .clip(AraShape.chip)
                    .clickable(role = Role.Button) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onQuickAdd(amount)
                    }
                    .clearAndSetSemantics {
                        role = Role.Button
                        contentDescription = "Tambah ${CurrencyFormatter.formatRupiah(amount.toDouble())}"
                    },
                shape = AraShape.chip,
                color = AraTheme.colors.selectedContainer.copy(alpha = 0.7f),
                border = BorderStroke(1.dp, PrimarySakuraPink.copy(alpha = 0.35f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = AraTheme.colors.onSelectedContainer,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                }
            }
        }
    }
}

/** Grid pilihan kategori berbentuk bubble (radio group). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryBubblePicker(
    categories: List<CategoryEntity>,
    selectedId: Long?,
    onSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AraSectionTitle(text = "Kategori")
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { category ->
                CategoryBubble(
                    category = category,
                    isSelected = category.id == selectedId,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onSelected(category.id)
                    }
                )
            }
        }
    }
}

@Composable
private fun CategoryBubble(
    category: CategoryEntity,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scaleAnim by animateFloatAsState(
        targetValue = if (isSelected) 1.04f else 1.0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
        label = "BubbleScale"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) PrimarySakuraPink else Color.Transparent,
        label = "BubbleBorder"
    )
    val iconTint = runCatching { Color(android.graphics.Color.parseColor(category.tintColorHex)) }
        .getOrDefault(PrimarySakuraPink)

    Surface(
        modifier = Modifier
            .scale(scaleAnim)
            .heightIn(min = 44.dp)
            .clip(AraShape.chip)
            .selectable(selected = isSelected, role = Role.RadioButton, onClick = onClick),
        shape = AraShape.chip,
        color = if (isSelected) AraTheme.colors.selectedContainer else AraTheme.colors.surfaceCard,
        border = BorderStroke(1.5.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = getIconVector(category.iconResName),
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = category.name,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) AraTheme.colors.onSelectedContainer else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Numpad pastel bersama (Tambah & Ubah transaksi). Perilaku identik di kedua layar:
 * maksimal [AmountInput.MAX_DIGITS] digit, DEL menghapus satu digit, tekan-lama DEL mengosongkan.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AraNumpad(
    onKey: (String) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("000", "0", "DEL")
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                row.forEach { key ->
                    val isDel = key == "DEL"
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 52.dp)
                            .clip(AraShape.button)
                            .then(
                                if (isDel) {
                                    Modifier.combinedClickable(
                                        role = Role.Button,
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            onBackspace()
                                        },
                                        onLongClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            onClear()
                                        }
                                    )
                                } else {
                                    Modifier.clickable(role = Role.Button) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onKey(key)
                                    }
                                }
                            )
                            .clearAndSetSemantics {
                                role = Role.Button
                                contentDescription = when (key) {
                                    "DEL" -> "Hapus angka terakhir. Tekan lama untuk mengosongkan"
                                    "000" -> "Tiga nol"
                                    else -> key
                                }
                            },
                        shape = AraShape.button,
                        color = AraTheme.colors.surfaceCard
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isDel) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.Backspace,
                                    contentDescription = null,
                                    tint = AraTheme.colors.textStrong,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Text(
                                    text = key,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AraTheme.colors.textStrong
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
