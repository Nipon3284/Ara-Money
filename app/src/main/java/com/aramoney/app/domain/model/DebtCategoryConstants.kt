package com.aramoney.app.domain.model

/**
 * Konstanta ID kategori sistem untuk transaksi hutang/piutang.
 * Kategori ini di-seed otomatis ke database dan tidak dapat diubah/dihapus oleh pengguna.
 */
object DebtCategoryConstants {
    /** ID kategori Hutang (Pengeluaran) — saat pengguna membayar hutang ke teman */
    const val CATEGORY_ID_HUTANG = 100L

    /** ID kategori Piutang (Pemasukan) — saat teman membayar piutang ke pengguna */
    const val CATEGORY_ID_PIUTANG = 101L

    /** Set ID kategori yang dikunci sistem (tidak dapat diubah/dihapus) */
    val SYSTEM_LOCKED_IDS = setOf(CATEGORY_ID_HUTANG, CATEGORY_ID_PIUTANG)

    /** Cek apakah suatu ID kategori merupakan kategori sistem yang terkunci */
    fun isSystemLocked(categoryId: Long): Boolean = categoryId in SYSTEM_LOCKED_IDS
}
