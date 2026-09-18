package com.aramoney.app.domain.model

import com.aramoney.app.data.local.entity.CategorySpendSum
import java.time.YearMonth

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

/**
 * Data rangkuman laporan keuangan bulanan.
 */
data class MonthlyReportData(
    val yearMonth: YearMonth,
    val totalExpense: Double = 0.0,
    val totalIncome: Double = 0.0,
    val netSavings: Double = 0.0,
    val categoryBreakdown: List<CategoryReportItem> = emptyList()
)
