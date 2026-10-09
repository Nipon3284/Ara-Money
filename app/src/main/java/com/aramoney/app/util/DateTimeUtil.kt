package com.aramoney.app.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Utility konversi dan pemformatan tanggal waktu untuk Ara Money (locale id-ID).
 */
object DateTimeUtil {

    private val indonesianLocale = Locale("id", "ID")

    private val transactionFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", indonesianLocale)
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", indonesianLocale)
    private val shortFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", indonesianLocale)
    private val longFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", indonesianLocale)
    private val dayHeaderFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", indonesianLocale)
    private val monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", indonesianLocale)
    private val dayFormatter = DateTimeFormatter.ofPattern("EEE, d MMM yyyy", indonesianLocale)
    private val rangeFormatter = DateTimeFormatter.ofPattern("d MMM yy", indonesianLocale)

    private val zone: ZoneId get() = ZoneId.systemDefault()

    fun formatTransactionDate(epochMillis: Long): String =
        Instant.ofEpochMilli(epochMillis).atZone(zone).format(transactionFormatter)

    fun formatTime(epochMillis: Long): String =
        Instant.ofEpochMilli(epochMillis).atZone(zone).format(timeFormatter)

    fun formatShortLocalDate(date: LocalDate): String = date.format(shortFormatter)

    fun formatLocalDate(date: LocalDate): String = date.format(longFormatter)

    fun formatDayHeader(date: LocalDate): String = date.format(dayHeaderFormatter)

    fun formatMonth(yearMonth: java.time.YearMonth): String = yearMonth.format(monthFormatter)

    fun formatDay(date: LocalDate): String = date.format(dayFormatter)

    fun formatRangeDate(date: LocalDate): String = date.format(rangeFormatter)

    fun epochMillisToLocalDate(epochMillis: Long): LocalDate =
        Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalDate()

    /** DatePicker Material bekerja dalam UTC tengah malam. */
    fun utcMillisToLocalDate(utcMillis: Long): LocalDate =
        Instant.ofEpochMilli(utcMillis).atZone(ZoneOffset.UTC).toLocalDate()

    fun localDateToUtcMillis(date: LocalDate): Long =
        date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

    /**
     * Timestamp untuk transaksi pada [date]. Untuk hari ini memakai jam sekarang; untuk tanggal lain
     * mempertahankan jam dari [timeSource] (default: jam sekarang) agar urutan tetap natural.
     */
    fun timestampFor(date: LocalDate, timeSource: Long = System.currentTimeMillis()): Long {
        val time: LocalTime = Instant.ofEpochMilli(timeSource).atZone(zone).toLocalTime()
        return date.atTime(time).atZone(zone).toInstant().toEpochMilli()
    }

    /** True jika transaksi dicatat lebih dari 24 jam yang lalu. */
    fun isOlderThan24h(epochMillis: Long, now: Long = System.currentTimeMillis()): Boolean =
        now - epochMillis > 24 * 60 * 60 * 1000L
}
