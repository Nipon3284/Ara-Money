package com.aramoney.app.domain.model

/**
 * Item laporan pengeluaran per kategori yang diperkaya dengan persentase.
 */
data class CategoryReportItem(
    val categoryId: Long,
    val categoryName: String,
    val iconResName: String,
    val tintColorHex: String,
    val totalAmount: Double,
    val percentage: Float, // Persentase terhadap total pengeluaran (0..100)
    val transactionCount: Int
)
