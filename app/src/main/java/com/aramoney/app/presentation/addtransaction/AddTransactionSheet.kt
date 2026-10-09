package com.aramoney.app.presentation.addtransaction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aramoney.app.presentation.components.AmountDisplay
import com.aramoney.app.presentation.components.AraInlineBanner
import com.aramoney.app.presentation.components.AraNumpad
import com.aramoney.app.presentation.components.AraPrimaryButton
import com.aramoney.app.presentation.components.CategoryBubblePicker
import com.aramoney.app.presentation.components.QuickAmountChips
import com.aramoney.app.presentation.components.TransactionDateSelector
import com.aramoney.app.presentation.components.TransactionKind
import com.aramoney.app.presentation.components.TransactionKindToggle
import com.aramoney.app.presentation.components.araTextFieldColors
import com.aramoney.app.presentation.theme.AraShape
import com.aramoney.app.presentation.theme.AraTheme
import com.aramoney.app.util.DateTimeUtil
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    onSaved: (message: String) -> Unit = {},
    viewModel: AddTransactionViewModel = hiltViewModel(),
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current
    val focusManager = LocalFocusManager.current

    // ViewModel hidup sepanjang Activity: pastikan tanggal default selalu "hari ini" setiap sheet dibuka
    LaunchedEffect(Unit) {
        viewModel.onDateSelected(LocalDate.now())
    }

    // Jika berhasil tersimpan, tampilkan feedback, tutup modal bottom sheet dan reset
    LaunchedEffect(uiState.isSavedSuccess) {
        if (uiState.isSavedSuccess) {
            val label = TransactionKind.from(uiState.type).label
            val dateSuffix = if (uiState.date == LocalDate.now()) "" else " untuk ${DateTimeUtil.formatShortLocalDate(uiState.date)}"
            onSaved("$label ${uiState.formattedAmount} tersimpan$dateSuffix")
            viewModel.resetState()
            onDismissRequest()
        }
    }

    val close = {
        viewModel.resetState()
        onDismissRequest()
    }

    ModalBottomSheet(
        onDismissRequest = close,
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
            // Konten form yang dapat di-scroll
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
                    Text(
                        text = "Catat Transaksi",
                        style = MaterialTheme.typography.titleLarge,
                        color = AraTheme.colors.textStrong,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = close) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Tutup",
                            tint = AraTheme.colors.textStrong
                        )
                    }
                }

                TransactionKindToggle(
                    selected = TransactionKind.from(uiState.type),
                    onSelected = { viewModel.setTransactionType(it.value) }
                )

                // 2. Nominal + validasi langsung
                AmountDisplay(
                    rawAmount = uiState.rawAmountString,
                    kind = TransactionKind.from(uiState.type)
                )

                QuickAmountChips(onQuickAdd = { viewModel.onQuickAmountAdd(it) })

                if (uiState.errorMessage != null) {
                    AraInlineBanner(text = uiState.errorMessage!!, isError = true)
                }

                // 3. Kategori
                CategoryBubblePicker(
                    categories = uiState.categories,
                    selectedId = uiState.selectedCategoryId,
                    onSelected = { viewModel.onCategorySelected(it) }
                )

                // 4. Tanggal (bisa backdate)
                TransactionDateSelector(
                    selectedDate = uiState.date,
                    onDateSelected = { viewModel.onDateSelected(it) }
                )

                // 5. Catatan opsional
                OutlinedTextField(
                    value = uiState.note,
                    onValueChange = { viewModel.onNoteChanged(it) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Catatan (opsional)") },
                    placeholder = { Text("Mis. boba taro") },
                    supportingText = {
                        Text("${uiState.note.length}/${AddTransactionViewModel.MAX_NOTE_LENGTH}")
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    shape = AraShape.button,
                    colors = araTextFieldColors()
                )

                // 6. Numpad
                AraNumpad(
                    onKey = { viewModel.onDigitPressed(it) },
                    onBackspace = { viewModel.onBackspacePressed() },
                    onClear = { viewModel.onClearPressed() }
                )

                Spacer(modifier = Modifier.height(4.dp))
            }

            // 7. Tombol Simpan (sticky footer)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AraPrimaryButton(
                    text = "Simpan Transaksi",
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.saveTransaction()
                    },
                    enabled = uiState.canSave,
                    isLoading = uiState.isSaving,
                    modifier = Modifier.fillMaxWidth()
                )
                uiState.disabledReason?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
