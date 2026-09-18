package com.aramoney.app.data.local.entity

/**
 * Model proyeksi query untuk agregasi pengeluaran per kategori.
 */
data class CategorySpendSum(
    val categoryId: Long,
    val categoryName: String,
    val iconResName: String,
    val tintColorHex: String,
    val totalAmount: Double,
    val transactionCount: Int
)
