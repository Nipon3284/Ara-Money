package com.aramoney.app.presentation.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aramoney.app.data.local.entity.CategoryEntity
import com.aramoney.app.data.local.entity.TransactionEntity
import com.aramoney.app.domain.model.PeriodReportData
import com.aramoney.app.domain.model.ReportPeriodType
import com.aramoney.app.domain.repository.CategoryRepository
import com.aramoney.app.domain.repository.TransactionRepository
import com.aramoney.app.domain.usecase.GetPeriodReportUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

data class ReportUiState(
    val periodType: ReportPeriodType = ReportPeriodType.MONTHLY,
    val selectedYearMonth: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate = LocalDate.now(),
    val customStartDate: LocalDate = LocalDate.now().minusDays(30),
    val customEndDate: LocalDate = LocalDate.now(),
    val reportData: PeriodReportData = PeriodReportData(),
    val categories: List<CategoryEntity> = emptyList(),
    val selectedCategoryId: Long? = null,
    val filteredTransactions: List<TransactionEntity> = emptyList(),
    val transactionToDelete: TransactionEntity? = null,
    val transactionToEdit: TransactionEntity? = null,
    val oldTransactionWarning: TransactionEntity? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ReportViewModel @Inject constructor(
    private val getPeriodReportUseCase: GetPeriodReportUseCase,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _periodType = MutableStateFlow(ReportPeriodType.MONTHLY)
    private val _selectedYearMonth = MutableStateFlow(YearMonth.now())
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    private val _customStartDate = MutableStateFlow(LocalDate.now().minusDays(30))
    private val _customEndDate = MutableStateFlow(LocalDate.now())
    private val _selectedCategoryId = MutableStateFlow<Long?>(null)
    private val _transactionToDelete = MutableStateFlow<TransactionEntity?>(null)
    private val _transactionToEdit = MutableStateFlow<TransactionEntity?>(null)
    private val _oldTransactionWarning = MutableStateFlow<TransactionEntity?>(null)

    private val reportDataFlow = combine(
        _periodType,
        _selectedYearMonth,
        _selectedDate,
        _customStartDate,
        _customEndDate
    ) { type, yearMonth, date, startRange, endRange ->
        val (start, end) = when (type) {
            ReportPeriodType.MONTHLY -> yearMonth.atDay(1) to yearMonth.atEndOfMonth()
            ReportPeriodType.DAILY -> date to date
            ReportPeriodType.CUSTOM_RANGE -> startRange to endRange
        }
        Triple(type, start, end)
    }.flatMapLatest { (type, start, end) ->
        getPeriodReportUseCase(type, start, end)
    }

    val uiState: StateFlow<ReportUiState> = combine(
        reportDataFlow,
        categoryRepository.getAllCategories(),
        _periodType,
        _selectedYearMonth,
        _selectedDate,
        _customStartDate,
        _customEndDate,
        _selectedCategoryId,
        _transactionToDelete,
        _transactionToEdit,
        _oldTransactionWarning
    ) { params ->
        val reportData = params[0] as PeriodReportData
        @Suppress("UNCHECKED_CAST")
        val categories = params[1] as List<CategoryEntity>
        val periodType = params[2] as ReportPeriodType
        val yearMonth = params[3] as YearMonth
        val date = params[4] as LocalDate
        val startRange = params[5] as LocalDate
        val endRange = params[6] as LocalDate
        val selectedCategory = params[7] as Long?
        val pendingDelete = params[8] as TransactionEntity?
        val pendingEdit = params[9] as TransactionEntity?
        val warningTx = params[10] as TransactionEntity?

        val filteredTransactions = if (selectedCategory != null) {
            reportData.transactions.filter { it.categoryId == selectedCategory }
        } else {
            reportData.transactions
        }

        ReportUiState(
            periodType = periodType,
            selectedYearMonth = yearMonth,
            selectedDate = date,
            customStartDate = startRange,
            customEndDate = endRange,
            reportData = reportData,
            categories = categories,
            selectedCategoryId = selectedCategory,
            filteredTransactions = filteredTransactions,
            transactionToDelete = pendingDelete,
            transactionToEdit = pendingEdit,
            oldTransactionWarning = warningTx
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ReportUiState()
    )

    fun setPeriodType(periodType: ReportPeriodType) {
        _periodType.value = periodType
        _selectedCategoryId.value = null
    }

    fun onPreviousMonth() {
        _selectedYearMonth.update { it.minusMonths(1) }
    }

    fun onNextMonth() {
        val next = _selectedYearMonth.value.plusMonths(1)
        if (!next.isAfter(YearMonth.now())) {
            _selectedYearMonth.value = next
        }
    }

    fun onPreviousDay() {
        _selectedDate.update { it.minusDays(1) }
    }

    fun onNextDay() {
        val next = _selectedDate.value.plusDays(1)
        if (!next.isAfter(LocalDate.now())) {
            _selectedDate.value = next
        }
    }

    fun onDateSelected(date: LocalDate) {
        _selectedDate.value = date
    }

    fun onDateRangeSelected(start: LocalDate, end: LocalDate) {
        val validStart = if (start.isAfter(end)) end else start
        val validEnd = if (end.isBefore(start)) start else end
        _customStartDate.value = validStart
        _customEndDate.value = validEnd
    }

    fun onCategorySelected(categoryId: Long) {
        if (_selectedCategoryId.value == categoryId) {
            _selectedCategoryId.value = null
        } else {
            _selectedCategoryId.value = categoryId
        }
    }

    fun onTransactionClick(transaction: TransactionEntity) {
        val isOlderThan24h = com.aramoney.app.util.DateTimeUtil.isOlderThan24h(transaction.timestamp)
        if (isOlderThan24h) {
            _oldTransactionWarning.value = transaction
        } else {
            _transactionToEdit.value = transaction
        }
    }

    fun confirmEditOldTransaction() {
        val tx = _oldTransactionWarning.value
        _oldTransactionWarning.value = null
        _transactionToEdit.value = tx
    }

    fun dismissOldTransactionWarning() {
        _oldTransactionWarning.value = null
    }

    fun dismissEditTransaction() {
        _transactionToEdit.value = null
    }

    fun updateTransaction(transaction: TransactionEntity, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            transactionRepository.updateTransaction(transaction)
            _transactionToEdit.value = null
            onDone()
        }
    }

    fun requestDeleteTransaction(transaction: TransactionEntity) {
        _transactionToDelete.value = transaction
    }

    fun dismissDeleteDialog() {
        _transactionToDelete.value = null
    }

    fun confirmDeleteTransaction(onDeleted: (TransactionEntity) -> Unit = {}) {
        val transaction = _transactionToDelete.value ?: return
        viewModelScope.launch {
            transactionRepository.deleteTransaction(transaction)
            _transactionToDelete.value = null
            onDeleted(transaction)
        }
    }

    /** Masukkan kembali transaksi yang baru dihapus (aksi Undo dari Snackbar). */
    fun restoreTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            transactionRepository.insertTransaction(transaction)
        }
    }
}
