package com.aramoney.app.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.aramoney.app.data.local.entity.CategoryEntity
import com.aramoney.app.data.local.entity.TransactionEntity
import com.aramoney.app.presentation.theme.DeepBerry
import com.aramoney.app.presentation.theme.DeepBerryDark
import com.aramoney.app.presentation.theme.ErrorSoftRed
import com.aramoney.app.presentation.theme.PrimarySakuraPink
import com.aramoney.app.presentation.theme.PrimarySakuraPinkContainer
import com.aramoney.app.presentation.theme.SuccessMintGreen
import com.aramoney.app.presentation.theme.SurfaceCard
import com.aramoney.app.presentation.theme.SurfaceCardDark
import com.aramoney.app.presentation.theme.SurfaceElevated
import com.aramoney.app.presentation.theme.SurfaceElevatedDark
import com.aramoney.app.presentation.theme.TextPrimaryDark
import com.aramoney.app.presentation.theme.isAppInDarkTheme
import com.aramoney.app.presentation.theme.softShadow
import com.aramoney.app.util.CurrencyFormatter
import com.aramoney.app.util.DateTimeUtil

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditTransactionSheet(
    transaction: TransactionEntity,
    categories: List<CategoryEntity>,
    onDismissRequest: () -> Unit,
    onSave: (TransactionEntity) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isAppInDarkTheme()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var type by remember(transaction) { mutableStateOf(transaction.type) }
    var rawAmount by remember(transaction) { mutableStateOf(transaction.amount.toLong().toString()) }
    var selectedCategoryId by remember(transaction) { mutableLongStateOf(transaction.categoryId) }
    var note by remember(transaction) { mutableStateOf(transaction.note ?: "") }

    val isOlderThan24h = remember(transaction) {
        (System.currentTimeMillis() - transaction.timestamp) > 24 * 60 * 60 * 1000L
    }

    val amountValue = rawAmount.toDoubleOrNull() ?: 0.0
    val canSave = amountValue > 0.0 && amountValue <= 100_000_000.0 && selectedCategoryId > 0L

    val filteredCategories = remember(categories, type) {
        val isExpense = type == "EXPENSE"
        categories.filter { it.isExpense == isExpense }
    }

    // Jika kategori terpilih saat ini tidak cocok dengan tipe baru, pilih kategori pertama yang cocok
    val currentSelectedValid = filteredCategories.any { it.id == selectedCategoryId }
    if (!currentSelectedValid && filteredCategories.isNotEmpty()) {
        selectedCategoryId = filteredCategories.first().id
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = if (isDark) SurfaceElevatedDark else SurfaceElevated,
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
                // 1. Header & Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Ubah Transaksi ✏️",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) TextPrimaryDark else DeepBerryDark
                    )
                    Text(
                        text = "Dicatat pada ${DateTimeUtil.formatTransactionDate(transaction.timestamp)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onDismissRequest) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Tutup",
                        tint = if (isDark) TextPrimaryDark else DeepBerryDark
                    )
                }
            }

            // Banner peringatan jika transaksi lebih dari 24 jam
            if (isOlderThan24h) {
                Surface(
                    color = Color(0xFFFFF3CD),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFFFEEBA)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.WarningAmber,
                            contentDescription = null,
                            tint = Color(0xFF856404),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Transaksi ini tercatat > 24 jam lalu. Perubahan akan memengaruhi histori saldo.",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF856404)
                        )
                    }
                }
            }

            // Segmented Control Pemasukan / Pengeluaran
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = if (isDark) SurfaceCardDark else SurfaceCard
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val isExpense = type == "EXPENSE"
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { type = "EXPENSE" },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isExpense) PrimarySakuraPink else Color.Transparent
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

                    val isIncome = type == "INCOME"
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { type = "INCOME" },
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

            // 2. Display Nominal
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .softShadow(elevation = 3.dp, shape = RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                color = if (isDark) SurfaceCardDark else SurfaceCard,
                border = BorderStroke(1.dp, if (isDark) Color(0xFF4B2E52) else Color(0xFFF7E6EE))
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
                        text = CurrencyFormatter.formatRupiah(amountValue),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (type == "EXPENSE") {
                            if (isDark) PrimarySakuraPink else DeepBerryDark
                        } else {
                            if (isDark) SuccessMintGreen else Color(0xFF2E7D32)
                        },
                        textAlign = TextAlign.Center
                    )
                }
            }

            // 3. Pilihan Kategori
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Kategori",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) TextPrimaryDark else DeepBerryDark
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filteredCategories.forEach { category ->
                        val isSelected = category.id == selectedCategoryId
                        val scaleAnim by animateFloatAsState(
                            targetValue = if (isSelected) 1.05f else 1.0f,
                            animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
                            label = "CategoryScale"
                        )
                        val borderColor by animateColorAsState(
                            targetValue = if (isSelected) PrimarySakuraPink else Color.Transparent,
                            label = "CategoryBorder"
                        )
                        val chipBg = when {
                            isSelected -> if (isDark) Color(0xFF532457) else PrimarySakuraPinkContainer
                            isDark -> SurfaceCardDark
                            else -> SurfaceCard
                        }

                        Surface(
                            modifier = Modifier
                                .scale(scaleAnim)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { selectedCategoryId = category.id },
                            shape = RoundedCornerShape(14.dp),
                            color = chipBg,
                            border = BorderStroke(1.5.dp, borderColor)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val iconTint = runCatching { Color(android.graphics.Color.parseColor(category.tintColorHex)) }
                                    .getOrDefault(PrimarySakuraPink)

                                Icon(
                                    imageVector = getIconVector(category.iconResName),
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
                }
            }

            // 4. Catatan
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "Catatan transaksi (opsional)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimarySakuraPink,
                    unfocusedBorderColor = if (isDark) Color(0xFF4B2E52) else Color(0xFFF3D9E4),
                    focusedContainerColor = if (isDark) SurfaceCardDark else Color.White,
                    unfocusedContainerColor = if (isDark) SurfaceCardDark else Color.White
                )
            )

            // 5. Pastel Numpad
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
                                    .defaultMinSize(minHeight = 48.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable {
                                        when (item) {
                                            "DEL" -> {
                                                if (rawAmount.length > 1) {
                                                    rawAmount = rawAmount.dropLast(1)
                                                } else {
                                                    rawAmount = "0"
                                                }
                                            }
                                            "000" -> {
                                                if (rawAmount != "0" && rawAmount.length <= 8) {
                                                    rawAmount += "000"
                                                }
                                            }
                                            else -> {
                                                if (rawAmount == "0") {
                                                    rawAmount = item
                                                } else if (rawAmount.length < 10) {
                                                    rawAmount += item
                                                }
                                            }
                                        }
                                    },
                                shape = RoundedCornerShape(16.dp),
                                color = if (isDark) SurfaceCardDark else SurfaceCard
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (item == "DEL") {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Rounded.Backspace,
                                            contentDescription = "Hapus",
                                            tint = if (isDark) TextPrimaryDark else DeepBerryDark,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    } else {
                                        Text(
                                            text = item,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isDark) TextPrimaryDark else DeepBerryDark
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

                Spacer(modifier = Modifier.height(4.dp))
            }

            // 6. Action Buttons: Hapus & Simpan Perubahan (Sticky Footer - Selalu tampak utuh, tidak pernah terpotong!)
            Surface(
                color = Color.Transparent,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDelete,
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = ErrorSoftRed
                        ),
                        border = BorderStroke(1.dp, ErrorSoftRed.copy(alpha = 0.5f)),
                        modifier = Modifier.height(50.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = "Hapus transaksi",
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Button(
                        onClick = {
                            val updated = transaction.copy(
                                amount = amountValue,
                                type = type,
                                categoryId = selectedCategoryId,
                                note = note.trim()
                            )
                            onSave(updated)
                        },
                        enabled = canSave,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .softShadow(
                                elevation = if (canSave) 6.dp else 0.dp,
                                shape = RoundedCornerShape(18.dp),
                                shadowColor = PrimarySakuraPink.copy(alpha = 0.35f)
                            ),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimarySakuraPink,
                            disabledContainerColor = if (isDark) Color(0xFF422847) else Color(0xFFE8DCE2)
                        )
                    ) {
                        Text(
                            text = "Simpan Perubahan 🌸",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (canSave) Color.White else Color(0xFF9E8B95)
                        )
                    }
                }
            }
        }
    }
}
