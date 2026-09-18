package com.aramoney.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entitas kategori transaksi (bawaan sistem maupun buatan pengguna).
 */
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val iconResName: String,     // Nama identifier icon atau emoji cute
    val tintColorHex: String,    // Kode warna hex (misal "#F48FB1")
    val isExpense: Boolean,      // true jika kategori pengeluaran, false jika pemasukan
    val isDefault: Boolean = true, // Kategori bawaan sistem vs kategori kustom buatan mahasiswi
    val sortOrder: Int = 0       // Urutan tampilan di UI
)
