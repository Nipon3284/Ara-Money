package com.aramoney.app.util

import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToLong

/**
 * Utility untuk memformat angka nominal mata uang Rupiah Indonesia (IDR).
 * Contoh: 35000.0 -> "Rp35.000"
 */
object CurrencyFormatter {

    private val indonesianLocale = Locale("id", "ID")

    fun formatRupiah(amount: Double): String {
        val rounded = amount.roundToLong()
        val format = NumberFormat.getNumberInstance(indonesianLocale)
        return "Rp${format.format(rounded)}"
    }

    fun formatRupiahWithoutSymbol(amount: Double): String {
        val rounded = amount.roundToLong()
        val format = NumberFormat.getNumberInstance(indonesianLocale)
        return format.format(rounded)
    }

    /**
     * Memformat input angka dari keyboard string menjadi angka nominal Double yang aman.
     */
    fun parseRupiahInput(input: String): Double {
        val cleanString = input.filter { it.isDigit() }
        return cleanString.toDoubleOrNull() ?: 0.0
    }
}
