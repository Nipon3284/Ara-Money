package com.aramoney.app.domain.usecase

import com.aramoney.app.domain.model.SafeToSpendState
import com.aramoney.app.util.CurrencyFormatter
import java.time.LocalDate
import javax.inject.Inject

/**
 * UseCase untuk mengevaluasi kesehatan finansial harian mahasiswi:
 * - Menghitung pengeluaran rata-rata per hari bulan berjalan
 * - Menghitung sisa kuota jajan hari ini (Target - Belanja Hari Ini)
 * - Menghitung daya tahan dompet (Financial Runway)
 */
class CalculateSafeToSpendUseCase @Inject constructor() {

    companion object {
        const val DEFAULT_DAILY_TARGET = 30_000.0
        const val RUNWAY_SAFE_DAYS = 7L     // >= 7 hari tergolong "Aman"
        const val RUNWAY_WARNING_DAYS = 3L  // 3-6 hari tergolong "Waspada", < 3 hari tergolong "Bahaya"

        /** True jika tanggal kiriman tersimpan sudah lewat sehingga perlu diperbarui pengguna. */
        fun isAllowanceDatePassed(nextAllowanceDate: LocalDate?, today: LocalDate = LocalDate.now()): Boolean =
            nextAllowanceDate != null && nextAllowanceDate.isBefore(today)
    }

    operator fun invoke(
        saldoSaatIni: Double,
        dailyTargetBudget: Double = DEFAULT_DAILY_TARGET,
        dailyAverageExpense: Double = 0.0,
        todayExpense: Double = 0.0,
        tanggalKirimanBerikutnya: LocalDate? = null,
        today: LocalDate = LocalDate.now()
    ): SafeToSpendState {
        val effectiveTarget = if (dailyTargetBudget > 0.0) dailyTargetBudget else DEFAULT_DAILY_TARGET
        val remainingTodayBudget = maxOf(0.0, effectiveTarget - todayExpense)
        val formattedTodayQuota = CurrencyFormatter.formatRupiah(remainingTodayBudget)
        val formattedAvg = CurrencyFormatter.formatRupiah(dailyAverageExpense)
        val formattedTarget = CurrencyFormatter.formatRupiah(effectiveTarget)

        // Edge Case 1: Saldo tepat Rp0
        if (saldoSaatIni == 0.0) {
            return SafeToSpendState.EmptyBalance(
                remainingDays = 0L,
                totalBalance = 0.0,
                dailyAverageExpense = dailyAverageExpense,
                remainingTodayBudget = remainingTodayBudget
            )
        }

        // Edge Case 2: Saldo defisit / minus
        if (saldoSaatIni < 0.0) {
            return SafeToSpendState.Bahaya(
                dailyBudget = 0.0,
                formattedDailyBudget = CurrencyFormatter.formatRupiah(0.0),
                remainingDays = 0L,
                totalBalance = saldoSaatIni,
                message = "Waduh, dompet minus Kak! Yuk rem belanja dulu.",
                dailyAverageExpense = dailyAverageExpense,
                todayExpense = todayExpense,
                remainingTodayBudget = 0.0,
                dailyTargetBudget = effectiveTarget
            )
        }

        // Catatan: tanggal kiriman yang sudah lewat TIDAK menggantikan perhitungan di sini, agar angka
        // jatah & daya tahan tetap tampil. Beranda menandainya lewat [isAllowanceDatePassed].

        // Hitung Daya Tahan (Financial Runway) dalam hari
        val remainingDays = (saldoSaatIni / effectiveTarget).toLong()
        val dailyBudget = minOf(effectiveTarget, saldoSaatIni)
        val formattedBudget = CurrencyFormatter.formatRupiah(dailyBudget)
        val dayStr = if (remainingDays <= 0L) "< 1" else "$remainingDays"

        return when {
            // Kondisi Kritis: Runway di bawah 3 hari
            remainingDays < RUNWAY_WARNING_DAYS -> {
                SafeToSpendState.Bahaya(
                    dailyBudget = dailyBudget,
                    formattedDailyBudget = formattedBudget,
                    remainingDays = remainingDays,
                    totalBalance = saldoSaatIni,
                    message = "Waduh, dompet menipis Kak! Saldo hanya cukup untuk ~$dayStr hari. Rem belanja dulu yuk! 😢💸",
                    dailyAverageExpense = dailyAverageExpense,
                    todayExpense = todayExpense,
                    remainingTodayBudget = remainingTodayBudget,
                    dailyTargetBudget = effectiveTarget
                )
            }

            // Kondisi Waspada 1: Rata-rata belanja melampaui target gaya hidup
            dailyAverageExpense > effectiveTarget -> {
                SafeToSpendState.Waspada(
                    dailyBudget = dailyBudget,
                    formattedDailyBudget = formattedBudget,
                    remainingDays = remainingDays,
                    totalBalance = saldoSaatIni,
                    message = "Waspada Kak! Rata-rata jajan ($formattedAvg/hari) melampaui target ($formattedTarget/hari). Rem belanja dulu yuk! 🍵",
                    dailyAverageExpense = dailyAverageExpense,
                    todayExpense = todayExpense,
                    remainingTodayBudget = remainingTodayBudget,
                    dailyTargetBudget = effectiveTarget
                )
            }

            // Kondisi Waspada 2: Runway terbatas (3 - 6 hari)
            remainingDays < RUNWAY_SAFE_DAYS -> {
                SafeToSpendState.Waspada(
                    dailyBudget = dailyBudget,
                    formattedDailyBudget = formattedBudget,
                    remainingDays = remainingDays,
                    totalBalance = saldoSaatIni,
                    message = "Pelan-pelan ya Kak, sisa $remainingDays hari. Sisa kuota hari ini: $formattedTodayQuota 🍵",
                    dailyAverageExpense = dailyAverageExpense,
                    todayExpense = todayExpense,
                    remainingTodayBudget = remainingTodayBudget,
                    dailyTargetBudget = effectiveTarget
                )
            }

            // Kondisi Aman: Runway mencukupi & rata-rata pengeluaran terkendali
            else -> {
                val safeMessage = if (dailyAverageExpense == 0.0) {
                    "Belum ada belanja bulan ini! Kuota hari ini: $formattedTodayQuota ya, Kak~ 🌸"
                } else {
                    "Keren! Rata-rata belanja ($formattedAvg/hari) lebih hemat dari target. Sisa kuota hari ini: $formattedTodayQuota 🌸"
                }

                SafeToSpendState.Aman(
                    dailyBudget = dailyBudget,
                    formattedDailyBudget = formattedBudget,
                    remainingDays = remainingDays,
                    totalBalance = saldoSaatIni,
                    message = safeMessage,
                    dailyAverageExpense = dailyAverageExpense,
                    todayExpense = todayExpense,
                    remainingTodayBudget = remainingTodayBudget,
                    dailyTargetBudget = effectiveTarget
                )
            }
        }
    }
}
