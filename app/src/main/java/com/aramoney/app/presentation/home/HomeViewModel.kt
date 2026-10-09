package com.aramoney.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aramoney.app.data.datastore.UserPreferencesRepository
import com.aramoney.app.data.local.entity.CategoryEntity
import com.aramoney.app.data.local.entity.TransactionEntity
import com.aramoney.app.domain.model.SafeToSpendState
import com.aramoney.app.domain.model.UserPreferences
import com.aramoney.app.domain.repository.CategoryRepository
import com.aramoney.app.domain.repository.TransactionRepository
import com.aramoney.app.domain.usecase.CalculateSafeToSpendUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val userName: String = "Kakak Mahasiswi",
    val avatarPresetId: String = "sakura_girl",
    val profilePhotoPath: String? = null,
    val safeToSpendState: SafeToSpendState = SafeToSpendState.NeedsSetup(),
    val categories: List<CategoryEntity> = emptyList(),
    val selectedCategoryId: Long? = null,
    val recentTransactions: List<TransactionEntity> = emptyList(),
    val totalBalance: Double = 0.0,
    val todayExpenseTotal: Double = 0.0,
    val todayIncomeTotal: Double = 0.0,
    val todayTransactionCount: Int = 0,
    /** Kategori yang muncul di transaksi hari ini (untuk chip filter). */
    val filterCategories: List<CategoryEntity> = emptyList(),
    /** Tanggal kiriman tersimpan sudah lewat → tampilkan ajakan memperbarui di Hero Card. */
    val needsAllowanceDateUpdate: Boolean = false,
    val transactionToDelete: TransactionEntity? = null,
    val transactionToEdit: TransactionEntity? = null,
    val oldTransactionWarning: TransactionEntity? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val calculateSafeToSpendUseCase: CalculateSafeToSpendUseCase
) : ViewModel() {

    private val _selectedCategoryId = MutableStateFlow<Long?>(null)
    private val _transactionToDelete = MutableStateFlow<TransactionEntity?>(null)
    private val _transactionToEdit = MutableStateFlow<TransactionEntity?>(null)
    private val _oldTransactionWarning = MutableStateFlow<TransactionEntity?>(null)

    val uiState: StateFlow<HomeUiState> = combine(
        transactionRepository.getAllTransactions(),
        categoryRepository.getAllCategories(),
        userPreferencesRepository.userPreferences,
        _selectedCategoryId,
        _transactionToDelete,
        _transactionToEdit,
        _oldTransactionWarning
    ) { params ->
        @Suppress("UNCHECKED_CAST")
        val transactions = params[0] as List<TransactionEntity>
        @Suppress("UNCHECKED_CAST")
        val categories = params[1] as List<CategoryEntity>
        val preferences = params[2] as UserPreferences
        val selectedCategory = params[3] as Long?
        val pendingDelete = params[4] as TransactionEntity?
        val pendingEdit = params[5] as TransactionEntity?
        val warningTx = params[6] as TransactionEntity?

        // Hitung total saldo = saldo awal + total pemasukan - total pengeluaran
        val totalIncome = transactions.filter { it.type == "INCOME" }.sumOf { it.amount }
        val totalExpense = transactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
        val currentBalance = preferences.initialBalance + totalIncome - totalExpense

        // Filter transaksi khusus HARI INI
        val today = LocalDate.now()
        val zone = ZoneId.systemDefault()
        val todayStartMillis = today.atStartOfDay(zone).toInstant().toEpochMilli()
        val todayEndMillis = today.atTime(23, 59, 59, 999_000_000).atZone(zone).toInstant().toEpochMilli()

        val todayTransactions = transactions.filter { it.timestamp in todayStartMillis..todayEndMillis }
        val todayExpense = todayTransactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
        val todayIncome = todayTransactions.filter { it.type == "INCOME" }.sumOf { it.amount }

        // Hitung pengeluaran bulan berjalan & rata-rata belanja per hari
        val startOfMonthMillis = today.withDayOfMonth(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val currentMonthExpenses = transactions.filter {
            it.type == "EXPENSE" && it.timestamp in startOfMonthMillis..todayEndMillis
        }.sumOf { it.amount }
        val daysElapsedInMonth = today.dayOfMonth.coerceAtLeast(1)
        val dailyAverageExpense = currentMonthExpenses / daysElapsedInMonth

        // Hitung Safe-to-Spend harian (Rata-Rata Pengeluaran & Financial Runway)
        val safeToSpend = calculateSafeToSpendUseCase(
            saldoSaatIni = currentBalance,
            dailyTargetBudget = preferences.dailyTargetBudget,
            dailyAverageExpense = dailyAverageExpense,
            todayExpense = todayExpense,
            tanggalKirimanBerikutnya = preferences.nextAllowanceDate,
            today = today
        )

        // Filter transaksi hari ini berdasarkan kategori jika ada yang dipilih
        val filteredTodayTransactions = if (selectedCategory != null) {
            todayTransactions.filter { it.categoryId == selectedCategory }
        } else {
            todayTransactions
        }

        HomeUiState(
            isLoading = false,
            userName = preferences.userName,
            avatarPresetId = preferences.avatarPresetId,
            profilePhotoPath = preferences.profilePhotoPath,
            safeToSpendState = safeToSpend,
            categories = categories,
            selectedCategoryId = selectedCategory,
            recentTransactions = filteredTodayTransactions,
            totalBalance = currentBalance,
            todayExpenseTotal = todayExpense,
            todayIncomeTotal = todayIncome,
            todayTransactionCount = todayTransactions.size,
            needsAllowanceDateUpdate = CalculateSafeToSpendUseCase.isAllowanceDatePassed(preferences.nextAllowanceDate, today),
            filterCategories = todayTransactions.map { it.categoryId }.distinct()
                .mapNotNull { id -> categories.find { it.id == id } }
                .sortedWith(compareByDescending<CategoryEntity> { it.isExpense }.thenBy { it.sortOrder }),
            transactionToDelete = pendingDelete,
            transactionToEdit = pendingEdit,
            oldTransactionWarning = warningTx
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    /** Perbarui tanggal kiriman berikutnya dari Beranda (saat status NeedsDateUpdate). */
    fun updateNextAllowanceDate(date: LocalDate) {
        viewModelScope.launch { userPreferencesRepository.setNextAllowanceDate(date) }
    }

    fun onCategorySelected(categoryId: Long?) {
        if (categoryId == null || _selectedCategoryId.value == categoryId) {
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

    /** Hapus transaksi lalu kembalikan salinannya agar UI bisa menawarkan "Urungkan". */
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
            // Kategori bisa saja sudah dihapus selama Snackbar tampil (FK RESTRICT) → jangan crash
            runCatching { transactionRepository.insertTransaction(transaction) }
        }
    }
}
