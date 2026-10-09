package com.aramoney.app.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.aramoney.app.data.local.entity.CategoryEntity
import com.aramoney.app.data.local.entity.TransactionEntity
import com.aramoney.app.domain.usecase.AddTransactionUseCase
import com.aramoney.app.presentation.addtransaction.AddTransactionViewModel
import com.aramoney.app.presentation.theme.AraShape
import com.aramoney.app.presentation.theme.AraTheme
import com.aramoney.app.util.AmountInput
import com.aramoney.app.util.DateTimeUtil

/**
 * Bottom sheet ubah transaksi. Memakai komponen form yang sama dengan Tambah Transaksi
 * (numpad, kategori, tanggal) agar perilakunya identik.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionSheet(
    transaction: TransactionEntity,
    categories: List<CategoryEntity>,
    onDismissRequest: () -> Unit,
    onSave: (TransactionEntity) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val haptic = LocalHapticFeedback.current
    val focusManager = LocalFocusManager.current

    var kind by remember(transaction) { mutableStateOf(TransactionKind.from(transaction.type)) }
    var rawAmount by remember(transaction) { mutableStateOf(transaction.amount.toLong().toString()) }
    var selectedCategoryId by remember(transaction) { mutableLongStateOf(transaction.categoryId) }
    var note by remember(transaction) { mutableStateOf(transaction.note ?: "") }
    var date by remember(transaction) { mutableStateOf(DateTimeUtil.epochMillisToLocalDate(transaction.timestamp)) }

    val isOlderThan24h = remember(transaction) { DateTimeUtil.isOlderThan24h(transaction.timestamp) }

    val filteredCategories = remember(categories, kind) {
        categories.filter { it.isExpense == (kind == TransactionKind.EXPENSE) }
    }

    // Jika kategori terpilih tidak cocok dengan tipe baru, pilih kategori pertama yang cocok
    if (filteredCategories.none { it.id == selectedCategoryId } && filteredCategories.isNotEmpty()) {
        selectedCategoryId = filteredCategories.first().id
    }

    val amountValue = AmountInput.toAmount(rawAmount)
    val newTimestamp = if (date == DateTimeUtil.epochMillisToLocalDate(transaction.timestamp)) {
        transaction.timestamp
    } else {
        DateTimeUtil.timestampFor(date, transaction.timestamp)
    }
    val validationError = AddTransactionUseCase.validate(amountValue, selectedCategoryId, newTimestamp)
    val canSave = validationError == null

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        shape = AraShape.sheet,
        containerColor = AraTheme.colors.surfaceElevated,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 22.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Ubah Transaksi",
                            style = MaterialTheme.typography.titleLarge,
                            color = AraTheme.colors.textStrong
                        )
                        Text(
                            text = "Dicatat pada ${DateTimeUtil.formatTransactionDate(transaction.timestamp)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismissRequest) {
                        Icon(Icons.Rounded.Close, contentDescription = "Tutup", tint = AraTheme.colors.textStrong)
                    }
                }

                if (isOlderThan24h) {
                    AraInlineBanner(text = "Transaksi ini dicatat lebih dari 24 jam lalu. Perubahan akan memengaruhi riwayat saldo.")
                }

                TransactionKindToggle(selected = kind, onSelected = { kind = it })

                AmountDisplay(rawAmount = rawAmount, kind = kind)

                CategoryBubblePicker(
                    categories = filteredCategories,
                    selectedId = selectedCategoryId,
                    onSelected = { selectedCategoryId = it }
                )

                TransactionDateSelector(selectedDate = date, onDateSelected = { date = it })

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it.take(AddTransactionViewModel.MAX_NOTE_LENGTH) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Catatan (opsional)") },
                    supportingText = { Text("${note.length}/${AddTransactionViewModel.MAX_NOTE_LENGTH}") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    shape = AraShape.button,
                    colors = araTextFieldColors()
                )

                AraNumpad(
                    onKey = { rawAmount = AmountInput.append(rawAmount, it) },
                    onBackspace = { rawAmount = AmountInput.backspace(rawAmount) },
                    onClear = { rawAmount = "" }
                )

                Spacer(modifier = Modifier.height(4.dp))
            }

            // Sticky footer: Hapus & Simpan
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDelete,
                        shape = AraShape.button,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AraTheme.colors.danger),
                        border = BorderStroke(1.dp, AraTheme.colors.danger.copy(alpha = 0.6f)),
                        modifier = Modifier.heightIn(min = 52.dp)
                    ) {
                        Icon(Icons.Rounded.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.size(6.dp))
                        Text("Hapus")
                    }

                    AraPrimaryButton(
                        text = "Simpan",
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onSave(
                                transaction.copy(
                                    amount = amountValue,
                                    type = kind.value,
                                    categoryId = selectedCategoryId,
                                    note = AddTransactionUseCase.normalizeNote(note),
                                    timestamp = newTimestamp
                                )
                            )
                        },
                        enabled = canSave,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (validationError != null) {
                    Text(
                        text = validationError,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
