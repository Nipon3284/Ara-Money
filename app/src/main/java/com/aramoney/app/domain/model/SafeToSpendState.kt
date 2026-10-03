package com.aramoney.app.domain.model

/**
 * State hasil perhitungan Safe-To-Spend harian, rata-rata pengeluaran, dan financial runway.
 */
sealed class SafeToSpendState {

    data class Aman(
        val dailyBudget: Double,
        val formattedDailyBudget: String,
        val remainingDays: Long,
        val totalBalance: Double,
        val message: String,
        val dailyAverageExpense: Double = 0.0,
        val todayExpense: Double = 0.0,
        val remainingTodayBudget: Double = 0.0,
        val dailyTargetBudget: Double = 0.0
    ) : SafeToSpendState()

    data class Waspada(
        val dailyBudget: Double,
        val formattedDailyBudget: String,
        val remainingDays: Long,
        val totalBalance: Double,
        val message: String,
        val dailyAverageExpense: Double = 0.0,
        val todayExpense: Double = 0.0,
        val remainingTodayBudget: Double = 0.0,
        val dailyTargetBudget: Double = 0.0
    ) : SafeToSpendState()

    data class Bahaya(
        val dailyBudget: Double,
        val formattedDailyBudget: String,
        val remainingDays: Long,
        val totalBalance: Double,
        val message: String,
        val dailyAverageExpense: Double = 0.0,
        val todayExpense: Double = 0.0,
        val remainingTodayBudget: Double = 0.0,
        val dailyTargetBudget: Double = 0.0
    ) : SafeToSpendState()

    data class NeedsDateUpdate(
        val totalBalance: Double,
        val message: String = "Tanggal kiriman sudah lewat nih, yuk update tanggal baru! 📅"
    ) : SafeToSpendState()

    data class NeedsSetup(
        val message: String = "Yuk atur target jajan harianmu agar daya tahan saldo bisa dihitung! 🌸"
    ) : SafeToSpendState()

    data class EmptyBalance(
        val remainingDays: Long = 0L,
        val totalBalance: Double = 0.0,
        val dailyAverageExpense: Double = 0.0,
        val remainingTodayBudget: Double = 0.0,
        val message: String = "Belum ada saldo tercatat. Yuk catat kiriman atau saldo awalmu! 🎀"
    ) : SafeToSpendState()
}
