package com.aramoney.app.presentation.splitbill

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aramoney.app.data.local.entity.SplitBillDebtEntity
import com.aramoney.app.domain.model.PersonDebtGroup
import com.aramoney.app.domain.repository.SplitBillRepository
import com.aramoney.app.domain.usecase.SettleSplitBillUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SplitBillUiState(
    val isLoading: Boolean = true,
    val selectedTab: String = "I_PAID_FOR_FRIEND", // "I_PAID_FOR_FRIEND" (Piutang) | "FRIEND_PAID_FOR_ME" (Utang)
    val personGroups: List<PersonDebtGroup> = emptyList(),
    val totalUnsettledAmount: Double = 0.0,
    val activeDebtorsCount: Int = 0,
    val selectedPersonGroup: PersonDebtGroup? = null,
    val isAddDialogOpen: Boolean = false,
    val initialFriendNameForAdd: String = "",
    val debtToEdit: SplitBillDebtEntity? = null,
    val debtToSettle: SplitBillDebtEntity? = null,
    val debtToDelete: SplitBillDebtEntity? = null,
    val isAutoCreateTransactionChecked: Boolean = true
)

@HiltViewModel
class SplitBillViewModel @Inject constructor(
    private val splitBillRepository: SplitBillRepository,
    private val settleSplitBillUseCase: SettleSplitBillUseCase
) : ViewModel() {

    private val _selectedTab = MutableStateFlow("I_PAID_FOR_FRIEND")
    private val _selectedPersonName = MutableStateFlow<String?>(null)
    private val _isAddDialogOpen = MutableStateFlow(false)
    private val _initialFriendNameForAdd = MutableStateFlow("")
    private val _debtToEdit = MutableStateFlow<SplitBillDebtEntity?>(null)
    private val _debtToSettle = MutableStateFlow<SplitBillDebtEntity?>(null)
    private val _debtToDelete = MutableStateFlow<SplitBillDebtEntity?>(null)
    private val _isAutoCreateTransactionChecked = MutableStateFlow(true)

    val uiState: StateFlow<SplitBillUiState> = combine(
        splitBillRepository.getAllDebts(),
        _selectedTab,
        _selectedPersonName,
        _isAddDialogOpen,
        _initialFriendNameForAdd,
        _debtToEdit,
        _debtToSettle,
        _debtToDelete,
        _isAutoCreateTransactionChecked
    ) { params ->
        @Suppress("UNCHECKED_CAST")
        val allDebts = params[0] as List<SplitBillDebtEntity>
        val selectedTab = params[1] as String
        val selectedPersonName = params[2] as String?
        val isAddOpen = params[3] as Boolean
        val initialName = params[4] as String
        val debtToEdit = params[5] as SplitBillDebtEntity?
        val debtToSettle = params[6] as SplitBillDebtEntity?
        val debtToDelete = params[7] as SplitBillDebtEntity?
        val autoCreate = params[8] as Boolean

        // Filter berdasarkan arah hutang/piutang yang dipilih
        val filteredDebts = allDebts.filter { it.direction == selectedTab }

        // Kelompokkan data berdasarkan nama orang (case-insensitive)
        val groupedPersons = filteredDebts
            .groupBy { it.friendName.trim().lowercase() }
            .map { (_, list) ->
                val name = list.first().friendName.trim()
                val unsettledList = list.filter { !it.isSettled }
                val settledList = list.filter { it.isSettled }
                val latest = list.maxOfOrNull { it.createdAt } ?: 0L

                PersonDebtGroup(
                    friendName = name,
                    direction = selectedTab,
                    totalUnsettledAmount = unsettledList.sumOf { it.amount },
                    totalSettledAmount = settledList.sumOf { it.amount },
                    unsettledCount = unsettledList.size,
                    settledCount = settledList.size,
                    debts = list.sortedByDescending { it.createdAt },
                    latestDate = latest
                )
            }
            .sortedWith(
                compareByDescending<PersonDebtGroup> { it.unsettledCount > 0 }
                    .thenByDescending { it.totalUnsettledAmount }
                    .thenByDescending { it.latestDate }
            )

        val totalUnsettled = filteredDebts.filter { !it.isSettled }.sumOf { it.amount }
        val activeCount = groupedPersons.count { it.unsettledCount > 0 }

        val activeGroup = if (selectedPersonName != null) {
            groupedPersons.find { it.friendName.equals(selectedPersonName, ignoreCase = true) }
        } else {
            null
        }

        SplitBillUiState(
            isLoading = false,
            selectedTab = selectedTab,
            personGroups = groupedPersons,
            totalUnsettledAmount = totalUnsettled,
            activeDebtorsCount = activeCount,
            selectedPersonGroup = activeGroup,
            isAddDialogOpen = isAddOpen,
            initialFriendNameForAdd = initialName,
            debtToEdit = debtToEdit,
            debtToSettle = debtToSettle,
            debtToDelete = debtToDelete,
            isAutoCreateTransactionChecked = autoCreate
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SplitBillUiState()
    )

    fun onTabSelected(direction: String) {
        _selectedTab.value = direction
        _selectedPersonName.value = null
    }

    fun selectPerson(person: PersonDebtGroup) {
        _selectedPersonName.value = person.friendName
    }

    fun clearSelectedPerson() {
        _selectedPersonName.value = null
    }

    fun openAddDialog(prefilledFriendName: String = "") {
        _initialFriendNameForAdd.value = prefilledFriendName
        _isAddDialogOpen.value = true
    }

    fun closeAddDialog() {
        _isAddDialogOpen.value = false
        _initialFriendNameForAdd.value = ""
    }

    fun addDebt(
        friendName: String,
        amount: Double,
        note: String,
        direction: String
    ) {
        if (friendName.isBlank() || amount <= 0.0) return

        viewModelScope.launch {
            val debt = SplitBillDebtEntity(
                friendName = friendName.trim(),
                amount = amount,
                note = note.trim(),
                isSettled = false,
                createdAt = System.currentTimeMillis(),
                direction = direction
            )
            splitBillRepository.insertDebt(debt)
            _isAddDialogOpen.value = false
            _initialFriendNameForAdd.value = ""
        }
    }

    fun openEditDialog(debt: SplitBillDebtEntity) {
        _debtToEdit.value = debt
    }

    fun closeEditDialog() {
        _debtToEdit.value = null
    }

    fun updateDebt(debt: SplitBillDebtEntity) {
        viewModelScope.launch {
            splitBillRepository.updateDebt(debt)
            _debtToEdit.value = null
        }
    }

    fun requestSettleDebt(debt: SplitBillDebtEntity) {
        _debtToSettle.value = debt
        _isAutoCreateTransactionChecked.value = true
    }

    fun dismissSettleDialog() {
        _debtToSettle.value = null
    }

    fun setAutoCreateTransaction(checked: Boolean) {
        _isAutoCreateTransactionChecked.value = checked
    }

    fun confirmSettleDebt(onSettled: (SplitBillDebtEntity) -> Unit = {}) {
        val debt = _debtToSettle.value ?: return
        val autoCreate = _isAutoCreateTransactionChecked.value

        viewModelScope.launch {
            settleSplitBillUseCase(debt.id, autoCreate)
            _debtToSettle.value = null
            onSettled(debt)
        }
    }

    fun requestDeleteDebt(debt: SplitBillDebtEntity) {
        _debtToDelete.value = debt
    }

    fun dismissDeleteDialog() {
        _debtToDelete.value = null
    }

    fun confirmDeleteDebt(onDeleted: (SplitBillDebtEntity) -> Unit = {}) {
        val debt = _debtToDelete.value ?: return
        viewModelScope.launch {
            splitBillRepository.deleteDebt(debt)
            _debtToDelete.value = null
            onDeleted(debt)
        }
    }

    /** Kembalikan catatan yang baru dihapus (aksi Undo Snackbar). */
    fun restoreDebt(debt: SplitBillDebtEntity) {
        viewModelScope.launch {
            splitBillRepository.insertDebt(debt)
        }
    }
}
