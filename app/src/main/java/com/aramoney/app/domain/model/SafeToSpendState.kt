package com.aramoney.app.domain.model

/**
 * State hasil perhitungan Safe-To-Spend harian.
 */
sealed class SafeToSpendState {

    data class Aman(
        val dailyBudget: Double,
        val formattedDailyBudget: String,
        val remainingDays: Long,
        val totalBalance: Double,
        val message: String
    ) : SafeToSpendState()

    data class Waspada(
        val dailyBudget: Double,
        val formattedDailyBudget: String,
        val remainingDays: Long,
        val totalBalance: Double,
        val message: String
    ) : SafeToSpendState()

    data class Bahaya(
        val dailyBudget: Double,
        val formattedDailyBudget: String,
        val remainingDays: Long,
        val totalBalance: Double,
        val message: String
    ) : SafeToSpendState()

    data class NeedsDateUpdate(
        val totalBalance: Double,
        val message: String = "Tanggal kiriman sudah lewat nih, yuk update tanggal baru! 📅"
    ) : SafeToSpendState()

    data class NeedsSetup(
        val message: String = "Yuk atur tanggal kiriman berikutnya agar batas jajan harian bisa dihitung! 🌸"
    ) : SafeToSpendState()

    data class EmptyBalance(
        val remainingDays: Long,
        val message: String = "Belum ada saldo tercatat. Yuk catat kiriman atau saldo awalmu! 🎀"
    ) : SafeToSpendState()
}
