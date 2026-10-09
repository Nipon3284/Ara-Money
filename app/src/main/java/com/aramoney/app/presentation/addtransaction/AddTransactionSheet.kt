package com.aramoney.app.presentation.addtransaction

import com.aramoney.app.presentation.theme.AraTheme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import com.aramoney.app.presentation.theme.isAppInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aramoney.app.data.local.entity.CategoryEntity
import com.aramoney.app.presentation.components.getIconVector
import com.aramoney.app.presentation.theme.DeepBerry
import com.aramoney.app.presentation.theme.DeepBerryDark
import com.aramoney.app.presentation.theme.ErrorSoftRed
import com.aramoney.app.presentation.theme.PrimarySakuraPink
import com.aramoney.app.presentation.theme.PrimarySakuraPinkContainer
import com.aramoney.app.presentation.theme.SecondaryLavender
import com.aramoney.app.presentation.theme.SuccessMintGreen
import com.aramoney.app.presentation.theme.SurfaceCard
import com.aramoney.app.presentation.theme.SurfaceCardDark
import com.aramoney.app.presentation.theme.SurfaceElevated
import com.aramoney.app.presentation.theme.SurfaceElevatedDark
import com.aramoney.app.presentation.theme.TextPrimaryDark
import com.aramoney.app.presentation.theme.softShadow

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddTransactionSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    onSaved: (message: String) -> Unit = {},
    viewModel: AddTransactionViewModel = hiltViewModel(),
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isDark = isAppInDarkTheme()
    val haptic = LocalHapticFeedback.current

    // Jika berhasil tersimpan, tampilkan feedback, tutup modal bottom sheet dan reset
    LaunchedEffect(uiState.isSavedSuccess) {
        if (uiState.isSavedSuccess) {
            val label = if (uiState.type == "EXPENSE") "Pengeluaran" else "Pemasukan"
            onSaved("$label ${uiState.formattedAmount} tersimpan")
            viewModel.resetState()
            onDismissRequest()
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            viewModel.resetState()
            onDismissRequest()
        },
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = AraTheme.colors.surfaceElevated,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 22.dp)
        ) {
            // Konten form yang dapat di-scroll
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Header & Segmented Toggle Pengeluaran / Pemasukan
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Catat Transaksi 🎀",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = AraTheme.colors.textStrong
                )

                IconButton(
                    onClick = {
                        viewModel.resetState()
                        onDismissRequest()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Tutup",
                        tint = MaterialSchemeTint(isDark)
                    )
                }
            }

            // Segmented Control Pastel
            TransactionTypeSegmentedControl(
                selectedType = uiState.type,
                onTypeSelected = { viewModel.setTransactionType(it) },
                isDark = isDark
            )

            // 2. Display Nominal Live (Besar & Jelas)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .softShadow(elevation = 3.dp, shape = RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                color = AraTheme.colors.surfaceCard,
                border = BorderStroke(1.dp, AraTheme.colors.border)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp, horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Nominal",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isDark) Color(0xFFD8B8DD) else DeepBerry
                    )
                    Text(
                        text = uiState.formattedAmount,
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (uiState.type == "EXPENSE") {
                            AraTheme.colors.expense
                        } else {
                            AraTheme.colors.income
                        },
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Presets Tambah Cepat (+10rb, +20rb, +50rb, +100rb) untuk input 3-ketukan
            QuickAmountChipsRow(
                onQuickAdd = { viewModel.onQuickAmountAdd(it) },
                isDark = isDark
            )

            // Pesan Error Validasi (jika melebihi batas 100 juta atau kosong)
            if (uiState.errorMessage != null) {
                Surface(
                    color = ErrorSoftRed.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = uiState.errorMessage!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = AraTheme.colors.danger,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }

            // 3. Grid Pilihan Kategori (FlowRow Bubble Chips)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Kategori",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = AraTheme.colors.textStrong
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.categories.forEach { category ->
                        BubbleCategoryChip(
                            category = category,
                            isSelected = category.id == uiState.selectedCategoryId,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.onCategorySelected(category.id)
                            },
                            isDark = isDark
                        )
                    }
                }
            }

            // 4. Catatan Opsional
            OutlinedTextField(
                value = uiState.note,
                onValueChange = { viewModel.onNoteChanged(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "Beli boba varian taro 🧋 (opsional)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AraTheme.colors.accent,
                    unfocusedBorderColor = AraTheme.colors.border,
                    focusedContainerColor = if (isDark) SurfaceCardDark else Color.White,
                    unfocusedContainerColor = if (isDark) SurfaceCardDark else Color.White
                )
            )

            // 5. Custom Pastel Numpad
            CustomPastelNumpad(
                onDigitClick = { viewModel.onDigitPressed(it) },
                onBackspaceClick = { viewModel.onBackspacePressed() },
                onClearClick = { viewModel.onClearPressed() },
                isDark = isDark
            )

                Spacer(modifier = Modifier.height(4.dp))
            }

            // 6. Tombol Simpan Transaksi (Sticky Footer - Selalu tampak utuh, tidak pernah terpotong!)
            Surface(
                color = Color.Transparent,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 14.dp)
            ) {
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.saveTransaction()
                    },
                    enabled = uiState.canSave,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .softShadow(
                            elevation = if (uiState.canSave) 8.dp else 0.dp,
                            shape = RoundedCornerShape(20.dp),
                            shadowColor = PrimarySakuraPink.copy(alpha = 0.35f)
                        ),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AraTheme.colors.action,
                        disabledContainerColor = AraTheme.colors.disabledContainer
                    )
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Text(
                            text = "Simpan Transaksi 🌸",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (uiState.canSave) Color.White else Color(0xFF9E8B95)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Segmented Control pastel untuk beralih antara Pengeluaran dan Pemasukan
 */
@Composable
private fun TransactionTypeSegmentedControl(
    selectedType: String,
    onTypeSelected: (String) -> Unit,
    isDark: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = AraTheme.colors.surfaceCard
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Tab Pengeluaran
            val isExpense = selectedType == "EXPENSE"
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onTypeSelected("EXPENSE") },
                shape = RoundedCornerShape(12.dp),
                color = if (isExpense) AraTheme.colors.action else Color.Transparent
            ) {
                Text(
                    text = "💸 Pengeluaran",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isExpense) FontWeight.Bold else FontWeight.Medium,
                    color = if (isExpense) Color.White else MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 10.dp)
                )
            }

            // Tab Pemasukan
            val isIncome = selectedType == "INCOME"
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onTypeSelected("INCOME") },
                shape = RoundedCornerShape(12.dp),
                color = if (isIncome) {
                    if (isDark) Color(0xFF388E3C) else SuccessMintGreen
                } else Color.Transparent
            ) {
                Text(
                    text = "💰 Pemasukan",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isIncome) FontWeight.Bold else FontWeight.Medium,
                    color = if (isIncome) {
                        if (isDark) Color.White else DeepBerryDark
                    } else MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 10.dp)
                )
            }
        }
    }
}

/**
 * Bubble Chip Kategori di dalam modal bottom sheet dengan animasi scale & border
 */
@Composable
private fun BubbleCategoryChip(
    category: CategoryEntity,
    isSelected: Boolean,
    onClick: () -> Unit,
    isDark: Boolean
) {
    val scaleAnim by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1.0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
        label = "BubbleScale"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) PrimarySakuraPink else Color.Transparent,
        label = "BubbleBorder"
    )

    val chipBg = when {
        isSelected -> AraTheme.colors.selectedContainer
        isDark -> SurfaceCardDark
        else -> SurfaceCard
    }

    Surface(
        modifier = Modifier
            .scale(scaleAnim)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .semantics {
                role = Role.RadioButton
                contentDescription = "Kategori ${category.name}, ${if (isSelected) "terpilih" else "belum terpilih"}"
            },
        shape = RoundedCornerShape(14.dp),
        color = chipBg,
        border = BorderStroke(1.5.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val iconVector = getIconVector(category.iconResName)
            val iconTint = runCatching { Color(android.graphics.Color.parseColor(category.tintColorHex)) }
                .getOrDefault(PrimarySakuraPink)

            Icon(
                imageVector = iconVector,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(16.dp)
            )

            Text(
                text = category.name,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) DeepBerry else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Custom Pastel Numpad dengan tombol bulat nyaman disentuh
 */
@Composable
private fun CustomPastelNumpad(
    onDigitClick: (String) -> Unit,
    onBackspaceClick: () -> Unit,
    onClearClick: () -> Unit,
    isDark: Boolean
) {
    val haptic = LocalHapticFeedback.current
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("000", "0", "DEL")
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                row.forEach { item ->
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 50.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                when (item) {
                                    "DEL" -> onBackspaceClick()
                                    else -> onDigitClick(item)
                                }
                            }
                            .semantics {
                                role = Role.Button
                                contentDescription = if (item == "DEL") "Hapus angka terakhir" else "Angka $item"
                            },
                        shape = RoundedCornerShape(16.dp),
                        color = AraTheme.colors.surfaceCard
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (item == "DEL") {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.Backspace,
                                    contentDescription = null,
                                    tint = AraTheme.colors.textStrong,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Text(
                                    text = item,
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

/**
 * Chip preset nominal cepat (+10rb, +20rb, +50rb, +100rb) untuk input 3-ketukan ultra-cepat
 */
@Composable
private fun QuickAmountChipsRow(
    onQuickAdd: (Long) -> Unit,
    isDark: Boolean
) {
    val haptic = LocalHapticFeedback.current
    val quickAmounts = listOf(
        10_000L to "+10rb",
        20_000L to "+20rb",
        50_000L to "+50rb",
        100_000L to "+100rb"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        quickAmounts.forEach { (amount, label) ->
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onQuickAdd(amount)
                    }
                    .semantics {
                        role = Role.Button
                        contentDescription = "Tambah $label rupiah"
                    },
                shape = RoundedCornerShape(12.dp),
                color = if (isDark) SurfaceCardDark else PrimarySakuraPinkContainer.copy(alpha = 0.55f),
                border = BorderStroke(1.dp, if (isDark) Color(0xFF4B2E52) else PrimarySakuraPink.copy(alpha = 0.35f))
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = AraTheme.colors.expense,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 9.dp)
                )
            }
        }
    }
}

@Composable
private fun MaterialSchemeTint(isDark: Boolean): Color {
    return AraTheme.colors.textStrong
}
