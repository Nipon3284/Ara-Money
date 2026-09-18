package com.aramoney.app.domain.usecase

import com.aramoney.app.domain.model.SafeToSpendState
import com.aramoney.app.util.CurrencyFormatter
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

/**
 * UseCase untuk menghitung batas nominal jajan harian yang aman ("Safe-To-Spend")
 * bagi mahasiswi berdasarkan saldo berjalan dan sisa hari menuju kiriman berikutnya.
 */
class CalculateSafeToSpendUseCase @Inject constructor() {

    companion object {
        const val COMFORT_THRESHOLD = 20_000.0  // Di atas Rp20.000 tergolong "Aman"
        const val DANGER_THRESHOLD = 5_000.0    // Di bawah Rp5.000 tergolong "Bahaya"
    }

    operator fun invoke(
        saldoSaatIni: Double,
        tanggalKirimanBerikutnya: LocalDate?,
        today: LocalDate = LocalDate.now()
    ): SafeToSpendState {
        // Edge Case 1: Pengguna baru belum menentukan tanggal kiriman
        if (tanggalKirimanBerikutnya == null) {
            return SafeToSpendState.NeedsSetup()
        }

        // Edge Case 2: Tanggal kiriman telah lewat dari hari ini
        if (today.isAfter(tanggalKirimanBerikutnya)) {
            return SafeToSpendState.NeedsDateUpdate(totalBalance = saldoSaatIni)
        }

        // Hitung sisa hari (inklusif minimal 1 hari jika hari ini adalah hari kiriman)
        val remainingDays = ChronoUnit.DAYS.between(today, tanggalKirimanBerikutnya).coerceAtLeast(1)

        // Edge Case 3: Saldo awal kosong tepat Rp0
        if (saldoSaatIni == 0.0) {
            return SafeToSpendState.EmptyBalance(remainingDays = remainingDays)
        }

        // Edge Case 4: Saldo defisit / minus
        if (saldoSaatIni < 0.0) {
            return SafeToSpendState.Bahaya(
                dailyBudget = 0.0,
                formattedDailyBudget = CurrencyFormatter.formatRupiah(0.0),
                remainingDays = remainingDays,
                totalBalance = saldoSaatIni,
                message = "Waduh, dompet menipis Kak! Yuk rem belanja dulu 😢💸"
            )
        }

        // Perhitungan normal: saldo dibagi sisa hari
        val dailyBudget = saldoSaatIni / remainingDays
        val formatted = CurrencyFormatter.formatRupiah(dailyBudget)

        return when {
            dailyBudget < DANGER_THRESHOLD -> {
                SafeToSpendState.Bahaya(
                    dailyBudget = dailyBudget,
                    formattedDailyBudget = formatted,
                    remainingDays = remainingDays,
                    totalBalance = saldoSaatIni,
                    message = "Waduh, dompet menipis Kak! Yuk rem belanja dulu 😢💸"
                )
            }
            dailyBudget <= COMFORT_THRESHOLD -> {
                SafeToSpendState.Waspada(
                    dailyBudget = dailyBudget,
                    formattedDailyBudget = formatted,
                    remainingDays = remainingDays,
                    totalBalance = saldoSaatIni,
                    message = "Pelan-pelan ya, sisa $formatted/hari. Kurangi boba dulu~ 🍵"
                )
            }
            else -> {
                SafeToSpendState.Aman(
                    dailyBudget = dailyBudget,
                    formattedDailyBudget = formatted,
                    remainingDays = remainingDays,
                    totalBalance = saldoSaatIni,
                    message = "Hari ini jajan santai sampai $formatted ya, Kak! 🌸"
                )
            }
        }
    }
}
