package com.aramoney.app.domain.usecase

import com.aramoney.app.domain.model.SafeToSpendState
import com.aramoney.app.util.CurrencyFormatter
import java.time.LocalDate
import javax.inject.Inject

/**
 * UseCase untuk menghitung batas nominal jajan harian yang aman ("Safe-To-Spend")
 * dan daya tahan dompet (Financial Runway) bagi mahasiswi berdasarkan saldo berjalan
 * dan standar gaya hidup / target jajan harian.
 */
class CalculateSafeToSpendUseCase @Inject constructor() {

    companion object {
        const val DEFAULT_DAILY_TARGET = 30_000.0
        const val RUNWAY_SAFE_DAYS = 7L     // >= 7 hari tergolong "Aman"
        const val RUNWAY_WARNING_DAYS = 3L  // 3-6 hari tergolong "Waspada", < 3 hari tergolong "Bahaya"
    }

    operator fun invoke(
        saldoSaatIni: Double,
        dailyTargetBudget: Double = DEFAULT_DAILY_TARGET,
        tanggalKirimanBerikutnya: LocalDate? = null,
        today: LocalDate = LocalDate.now()
    ): SafeToSpendState {
        // Edge Case 1: Saldo tepat Rp0
        if (saldoSaatIni == 0.0) {
            return SafeToSpendState.EmptyBalance(remainingDays = 0L)
        }

        // Edge Case 2: Saldo defisit / minus
        if (saldoSaatIni < 0.0) {
            return SafeToSpendState.Bahaya(
                dailyBudget = 0.0,
                formattedDailyBudget = CurrencyFormatter.formatRupiah(0.0),
                remainingDays = 0L,
                totalBalance = saldoSaatIni,
                message = "Waduh, dompet minus Kak! Yuk rem belanja dulu 😢💸"
            )
        }

        // Pastikan target harian valid (> 0)
        val effectiveTarget = if (dailyTargetBudget > 0.0) dailyTargetBudget else DEFAULT_DAILY_TARGET

        // Hitung Daya Tahan (Financial Runway) dalam hari
        val remainingDays = (saldoSaatIni / effectiveTarget).toLong()

        // Batas aman jajan hari ini adalah target harian, namun tidak melebihi sisa total saldo
        val dailyBudget = minOf(effectiveTarget, saldoSaatIni)
        val formatted = CurrencyFormatter.formatRupiah(dailyBudget)

        return when {
            remainingDays < RUNWAY_WARNING_DAYS -> {
                val dayStr = if (remainingDays <= 0L) "< 1" else "$remainingDays"
                SafeToSpendState.Bahaya(
                    dailyBudget = dailyBudget,
                    formattedDailyBudget = formatted,
                    remainingDays = remainingDays,
                    totalBalance = saldoSaatIni,
                    message = "Waduh, dompet menipis Kak! Saldo hanya cukup untuk ~$dayStr hari. Rem belanja dulu yuk! 😢💸"
                )
            }
            remainingDays < RUNWAY_SAFE_DAYS -> {
                SafeToSpendState.Waspada(
                    dailyBudget = dailyBudget,
                    formattedDailyBudget = formatted,
                    remainingDays = remainingDays,
                    totalBalance = saldoSaatIni,
                    message = "Pelan-pelan ya Kak, saldo tersisa untuk $remainingDays hari (~$formatted/hari). Kurangi boba dulu~ 🍵"
                )
            }
            else -> {
                SafeToSpendState.Aman(
                    dailyBudget = dailyBudget,
                    formattedDailyBudget = formatted,
                    remainingDays = remainingDays,
                    totalBalance = saldoSaatIni,
                    message = "Saldo aman untuk $remainingDays hari ke depan! Jajan santai $formatted hari ini ya~ 🌸"
                )
            }
        }
    }
}
