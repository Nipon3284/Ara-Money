package com.aramoney.app.util

/**
 * Aturan input nominal bersama untuk numpad (Tambah & Ubah transaksi) dan kolom nominal lain.
 * Fungsi murni agar mudah diuji unit test.
 */
object AmountInput {
    /** Batas maksimal nominal satu transaksi / catatan utang. */
    const val MAX_AMOUNT: Long = 100_000_000L

    /** Panjang digit maksimal yang boleh diketik (cukup untuk MAX_AMOUNT). */
    const val MAX_DIGITS: Int = 9

    /**
     * Tambahkan [key] ("0".."9" atau "000") ke [current] (string digit tanpa format).
     * Mengembalikan nilai lama jika hasilnya tidak valid (nol di depan / terlalu panjang).
     */
    fun append(current: String, key: String): String {
        require(key.isNotEmpty() && key.all { it.isDigit() })
        if (current.isEmpty() && key.all { it == '0' }) return current
        if (current.length + key.length > MAX_DIGITS) return current
        return current + key
    }

    /** Hapus satu digit terakhir. */
    fun backspace(current: String): String = current.dropLast(1)

    /** Tambahkan nominal cepat (mis. +10rb). Tidak melebihi [MAX_AMOUNT]. */
    fun addQuick(current: String, add: Long): String {
        val next = (current.toLongOrNull() ?: 0L) + add
        return if (next in 1..MAX_AMOUNT) next.toString() else current
    }

    /** Saring teks bebas (mis. dari keyboard) menjadi digit valid saja. */
    fun sanitize(input: String): String =
        input.filter { it.isDigit() }.trimStart('0').take(MAX_DIGITS)

    fun toAmount(raw: String): Double = raw.toDoubleOrNull() ?: 0.0

    fun isOverLimit(raw: String): Boolean = (raw.toLongOrNull() ?: 0L) > MAX_AMOUNT

    /** Pesan validasi untuk ditampilkan langsung di bawah input, atau null jika valid. */
    fun validationMessage(raw: String): String? =
        if (isOverLimit(raw)) "Maksimal ${CurrencyFormatter.formatRupiah(MAX_AMOUNT.toDouble())} per catatan ya, Kak" else null
}
