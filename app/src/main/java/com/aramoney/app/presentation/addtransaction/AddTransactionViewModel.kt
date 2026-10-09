package com.aramoney.app.presentation.addtransaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aramoney.app.data.local.entity.CategoryEntity
import com.aramoney.app.domain.repository.CategoryRepository
import com.aramoney.app.domain.usecase.AddTransactionUseCase
import com.aramoney.app.presentation.components.TransactionKind
import com.aramoney.app.util.AmountInput
import com.aramoney.app.util.CurrencyFormatter
import com.aramoney.app.util.DateTimeUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class AddTransactionUiState(
    val type: String = TransactionKind.EXPENSE.value, // "EXPENSE" atau "INCOME"
    val rawAmountString: String = "",
    val categories: List<CategoryEntity> = emptyList(),
    val selectedCategoryId: Long? = null,
    val note: String = "",
    val date: LocalDate = LocalDate.now(),
    val errorMessage: String? = null,
    val isSavedSuccess: Boolean = false,
    val isSaving: Boolean = false
) {
    val amount: Double
        get() = AmountInput.toAmount(rawAmountString)

    val formattedAmount: String
        get() = CurrencyFormatter.formatRupiah(amount)

    val canSave: Boolean
        get() = amount > 0.0 && !AmountInput.isOverLimit(rawAmountString) && selectedCategoryId != null && !isSaving

    /** Alasan tombol Simpan nonaktif, ditampilkan di bawah tombol. */
    val disabledReason: String?
        get() = when {
            isSaving -> null
            amount <= 0.0 -> "Masukkan nominal dulu ya, Kak"
            AmountInput.isOverLimit(rawAmountString) -> AmountInput.validationMessage(rawAmountString)
            selectedCategoryId == null -> "Pilih kategori dulu ya, Kak"
            else -> null
        }
}

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val addTransactionUseCase: AddTransactionUseCase
) : ViewModel() {

    companion object {
        const val MAX_NOTE_LENGTH = 80
    }

    private val _uiState = MutableStateFlow(AddTransactionUiState())
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

    private var allCategoriesCache: List<CategoryEntity> = emptyList()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            categoryRepository.getAllCategories().collect { list ->
                allCategoriesCache = list
                updateCategoriesForCurrentType()
            }
        }
    }

    fun setTransactionType(type: String) {
        if (_uiState.value.type != type) {
            _uiState.update { it.copy(type = type, selectedCategoryId = null) }
            updateCategoriesForCurrentType()
        }
    }

    private fun updateCategoriesForCurrentType() {
        val isExpense = _uiState.value.type == TransactionKind.EXPENSE.value
        val filtered = allCategoriesCache.filter { it.isExpense == isExpense }
        val defaultCategory = filtered.firstOrNull()?.id
        _uiState.update { state ->
            state.copy(
                categories = filtered,
                selectedCategoryId = state.selectedCategoryId?.takeIf { id -> filtered.any { it.id == id } }
                    ?: defaultCategory
            )
        }
    }

    fun onDigitPressed(digit: String) {
        _uiState.update { it.copy(rawAmountString = AmountInput.append(it.rawAmountString, digit), errorMessage = null) }
    }

    fun onBackspacePressed() {
        _uiState.update { it.copy(rawAmountString = AmountInput.backspace(it.rawAmountString), errorMessage = null) }
    }

    fun onClearPressed() {
        _uiState.update { it.copy(rawAmountString = "", errorMessage = null) }
    }

    fun onQuickAmountAdd(addAmount: Long) {
        _uiState.update { it.copy(rawAmountString = AmountInput.addQuick(it.rawAmountString, addAmount), errorMessage = null) }
    }

    fun onCategorySelected(categoryId: Long) {
        _uiState.update { it.copy(selectedCategoryId = categoryId, errorMessage = null) }
    }

    fun onNoteChanged(newNote: String) {
        _uiState.update { it.copy(note = newNote.take(MAX_NOTE_LENGTH)) }
    }

    fun onDateSelected(date: LocalDate) {
        _uiState.update { it.copy(date = date) }
    }

    fun saveTransaction() {
        val state = _uiState.value
        val categoryId = state.selectedCategoryId ?: return

        _uiState.update { it.copy(isSaving = true, errorMessage = null) }

        viewModelScope.launch {
            val result = addTransactionUseCase(
                amount = state.amount,
                type = state.type,
                categoryId = categoryId,
                note = state.note,
                timestamp = DateTimeUtil.timestampFor(state.date)
            )

            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isSaving = false, isSavedSuccess = true) }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = error.message ?: "Terjadi kesalahan saat menyimpan transaksi"
                        )
                    }
                }
            )
        }
    }

    fun resetState() {
        _uiState.update {
            AddTransactionUiState(
                categories = allCategoriesCache.filter { c -> c.isExpense },
                selectedCategoryId = allCategoriesCache.firstOrNull { c -> c.isExpense }?.id
            )
        }
    }
}
