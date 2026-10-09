package com.aramoney.app.util

import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToLong

/**
 * Utility untuk memformat angka nominal mata uang Rupiah Indonesia (IDR).
 * Contoh: 35000.0 -> "Rp35.000", -5000.0 -> "−Rp5.000"
 */
object CurrencyFormatter {

    private val indonesianLocale = Locale("id", "ID")

    /** Tanda minus tipografis (U+2212) agar sejajar dengan tanda plus. */
    const val MINUS = "\u2212"

    fun formatRupiah(amount: Double): String {
        val rounded = amount.roundToLong()
        val format = NumberFormat.getNumberInstance(indonesianLocale)
        val body = "Rp${format.format(abs(rounded))}"
        return if (rounded < 0) "$MINUS$body" else body
    }

    /**
     * Format nominal dengan tanda eksplisit: "+Rp10.000" (pemasukan) / "−Rp10.000" (pengeluaran).
     */
    fun formatSigned(amount: Double, isIncome: Boolean): String {
        val body = formatRupiah(abs(amount))
        return if (isIncome) "+$body" else "$MINUS$body"
    }

    /** Hanya angka berformat id-ID tanpa simbol, mis. "35.000". */
    fun formatNumber(amount: Long): String =
        NumberFormat.getNumberInstance(indonesianLocale).format(amount)

    /** Persentase berformat Indonesia, mis. 12.5f -> "12,5%". */
    fun formatPercent(value: Float, decimals: Int = 1): String {
        val format = NumberFormat.getNumberInstance(indonesianLocale).apply {
            maximumFractionDigits = decimals
            minimumFractionDigits = 0
        }
        return "${format.format(value)}%"
    }
}
