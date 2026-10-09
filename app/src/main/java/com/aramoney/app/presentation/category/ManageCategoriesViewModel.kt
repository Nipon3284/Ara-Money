package com.aramoney.app.presentation.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aramoney.app.data.local.entity.CategoryEntity
import com.aramoney.app.domain.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class CategoryTabFilter(val label: String) {
    ALL("Semua"),
    EXPENSE("Pengeluaran"),
    INCOME("Pemasukan")
}

data class CategoryFormState(
    val categoryId: Long? = null,
    val initialName: String = "",
    val initialIcon: String = "sparkles",
    val initialColor: String = "#F48FB1",
    val initialIsExpense: Boolean = true,
    val isEdit: Boolean = false
)

data class ManageCategoriesUiState(
    val categories: List<CategoryEntity> = emptyList(),
    val selectedTab: CategoryTabFilter = CategoryTabFilter.ALL,
    val categoryFormState: CategoryFormState? = null,
    val categoryToDelete: CategoryEntity? = null,
    val feedbackMessage: String? = null
)

private data class ManageCategoriesDialogState(
    val selectedTab: CategoryTabFilter = CategoryTabFilter.ALL,
    val categoryFormState: CategoryFormState? = null,
    val categoryToDelete: CategoryEntity? = null,
    val feedbackMessage: String? = null
)

@HiltViewModel
class ManageCategoriesViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _dialogState = MutableStateFlow(ManageCategoriesDialogState())

    val uiState: StateFlow<ManageCategoriesUiState> = combine(
        categoryRepository.getAllCategories(),
        _dialogState
    ) { categories, dialogs ->
        ManageCategoriesUiState(
            categories = categories,
            selectedTab = dialogs.selectedTab,
            categoryFormState = dialogs.categoryFormState,
            categoryToDelete = dialogs.categoryToDelete,
            feedbackMessage = dialogs.feedbackMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ManageCategoriesUiState()
    )

    fun setTabFilter(tab: CategoryTabFilter) {
        _dialogState.update { it.copy(selectedTab = tab) }
    }

    fun openAddCategoryDialog() {
        val isExpense = _dialogState.value.selectedTab != CategoryTabFilter.INCOME
        _dialogState.update {
            it.copy(
                categoryFormState = CategoryFormState(
                    categoryId = null,
                    initialName = "",
                    initialIcon = if (isExpense) "sparkles" else "payments",
                    initialColor = if (isExpense) "#F48FB1" else "#81C784",
                    initialIsExpense = isExpense,
                    isEdit = false
                )
            )
        }
    }

    fun openEditCategoryDialog(category: CategoryEntity) {
        _dialogState.update {
            it.copy(
                categoryFormState = CategoryFormState(
                    categoryId = category.id,
                    initialName = category.name,
                    initialIcon = category.iconResName,
                    initialColor = category.tintColorHex,
                    initialIsExpense = category.isExpense,
                    isEdit = true
                )
            )
        }
    }

    fun closeCategoryDialog() {
        _dialogState.update { it.copy(categoryFormState = null) }
    }

    fun saveCategory(
        id: Long?,
        name: String,
        icon: String,
        color: String,
        isExpense: Boolean
    ) {
        if (name.isBlank()) return
        viewModelScope.launch {
            if (id == null) {
                val newCategory = CategoryEntity(
                    name = name.trim(),
                    iconResName = icon,
                    tintColorHex = color,
                    isExpense = isExpense,
                    isDefault = false,
                    sortOrder = 99
                )
                categoryRepository.insertCategory(newCategory)
                _dialogState.update {
                    it.copy(
                        categoryFormState = null,
                        feedbackMessage = "Kategori '${name.trim()}' berhasil ditambahkan"
                    )
                }
            } else {
                val existing = categoryRepository.getCategoryByIdSync(id)
                val updatedCategory = (existing ?: CategoryEntity(
                    id = id,
                    name = name.trim(),
                    iconResName = icon,
                    tintColorHex = color,
                    isExpense = isExpense,
                    isDefault = false
                )).copy(
                    name = name.trim(),
                    iconResName = icon,
                    tintColorHex = color,
                    isExpense = isExpense
                )
                categoryRepository.updateCategory(updatedCategory)
                _dialogState.update {
                    it.copy(
                        categoryFormState = null,
                        feedbackMessage = "Kategori '${name.trim()}' berhasil diperbarui"
                    )
                }
            }
        }
    }

    fun openDeleteConfirmation(category: CategoryEntity) {
        _dialogState.update { it.copy(categoryToDelete = category) }
    }

    fun closeDeleteConfirmation() {
        _dialogState.update { it.copy(categoryToDelete = null) }
    }

    fun confirmDeleteCategory() {
        val category = _dialogState.value.categoryToDelete ?: return
        viewModelScope.launch {
            val success = categoryRepository.deleteCategory(category)
            _dialogState.update {
                it.copy(
                    categoryToDelete = null,
                    feedbackMessage = if (success) {
                        "Kategori '${category.name}' berhasil dihapus"
                    } else {
                        "Gagal menghapus kategori. Minimal harus tersisa 1 kategori untuk ${if (category.isExpense) "Pengeluaran" else "Pemasukan"}."
                    }
                )
            }
        }
    }

    fun dismissFeedback() {
        _dialogState.update { it.copy(feedbackMessage = null) }
    }
}
