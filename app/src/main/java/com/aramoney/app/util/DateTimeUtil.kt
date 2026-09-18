package com.aramoney.app.util

import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

/**
 * Utility konversi dan pemformatan tanggal waktu untuk Ara Money.
 */
object DateTimeUtil {

    private val indonesianLocale = Locale("id", "ID")

    fun formatTransactionDate(epochMillis: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", indonesianLocale)
        return sdf.format(Date(epochMillis))
    }

    fun formatShortDate(epochMillis: Long): String {
        val sdf = SimpleDateFormat("dd MMMM yyyy", indonesianLocale)
        return sdf.format(Date(epochMillis))
    }

    fun formatMonthYear(epochMillis: Long): String {
        val sdf = SimpleDateFormat("MMMM yyyy", indonesianLocale)
        return sdf.format(Date(epochMillis))
    }

    fun formatLocalDate(date: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", indonesianLocale)
        return date.format(formatter)
    }

    fun epochMillisToLocalDate(epochMillis: Long): LocalDate {
        return Instant.ofEpochMilli(epochMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    }
}
