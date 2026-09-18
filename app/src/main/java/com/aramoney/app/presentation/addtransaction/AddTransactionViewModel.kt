package com.aramoney.app.presentation.addtransaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aramoney.app.data.local.entity.CategoryEntity
import com.aramoney.app.domain.repository.CategoryRepository
import com.aramoney.app.domain.usecase.AddTransactionUseCase
import com.aramoney.app.util.CurrencyFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddTransactionUiState(
    val type: String = "EXPENSE", // "EXPENSE" atau "INCOME"
    val rawAmountString: String = "",
    val categories: List<CategoryEntity> = emptyList(),
    val selectedCategoryId: Long? = null,
    val note: String = "",
    val errorMessage: String? = null,
    val isSavedSuccess: Boolean = false,
    val isSaving: Boolean = false
) {
    val amount: Double
        get() = rawAmountString.toDoubleOrNull() ?: 0.0

    val formattedAmount: String
        get() = if (amount == 0.0) "Rp 0" else CurrencyFormatter.formatRupiah(amount)

    val canSave: Boolean
        get() = amount > 0.0 && selectedCategoryId != null && !isSaving
}

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val addTransactionUseCase: AddTransactionUseCase
) : ViewModel() {

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
        val isExpense = _uiState.value.type == "EXPENSE"
        val filtered = allCategoriesCache.filter { it.isExpense == isExpense }
        val defaultCategory = filtered.firstOrNull()?.id
        _uiState.update {
            it.copy(
                categories = filtered,
                selectedCategoryId = it.selectedCategoryId ?: defaultCategory
            )
        }
    }

    fun onDigitPressed(digit: String) {
        val current = _uiState.value.rawAmountString

        // Cegah input terlalu panjang (maksimum 9 digit / 999 juta)
        if (current.length >= 9) return

        // Cegah awalan nol berulang
        if (current.isEmpty() && digit == "0") return

        val newString = current + digit
        _uiState.update { it.copy(rawAmountString = newString, errorMessage = null) }
    }

    fun onBackspacePressed() {
        val current = _uiState.value.rawAmountString
        if (current.isNotEmpty()) {
            _uiState.update { it.copy(rawAmountString = current.dropLast(1), errorMessage = null) }
        }
    }

    fun onClearPressed() {
        _uiState.update { it.copy(rawAmountString = "", errorMessage = null) }
    }

    fun onCategorySelected(categoryId: Long) {
        _uiState.update { it.copy(selectedCategoryId = categoryId, errorMessage = null) }
    }

    fun onNoteChanged(newNote: String) {
        _uiState.update { it.copy(note = newNote) }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun saveTransaction() {
        val state = _uiState.value
        val amount = state.amount
        val categoryId = state.selectedCategoryId ?: return

        _uiState.update { it.copy(isSaving = true, errorMessage = null) }

        viewModelScope.launch {
            val result = addTransactionUseCase(
                amount = amount,
                type = state.type,
                categoryId = categoryId,
                note = state.note
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
                type = "EXPENSE",
                rawAmountString = "",
                categories = allCategoriesCache.filter { it.isExpense },
                selectedCategoryId = allCategoriesCache.firstOrNull { it.isExpense }?.id,
                note = "",
                errorMessage = null,
                isSavedSuccess = false,
                isSaving = false
            )
        }
    }
}
