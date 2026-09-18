package com.aramoney.app.domain.model

import com.aramoney.app.data.local.entity.TransactionEntity
import java.time.LocalDate

enum class ReportPeriodType(val label: String) {
    MONTHLY("Bulanan"),
    DAILY("Harian"),
    CUSTOM_RANGE("Rentang Tanggal")
}

/**
 * Data rangkuman laporan keuangan untuk periode tertentu (Harian, Bulanan, atau Rentang Tanggal).
 */
data class PeriodReportData(
    val periodType: ReportPeriodType = ReportPeriodType.MONTHLY,
    val startDate: LocalDate = LocalDate.now(),
    val endDate: LocalDate = LocalDate.now(),
    val totalExpense: Double = 0.0,
    val totalIncome: Double = 0.0,
    val netSavings: Double = 0.0,
    val categoryBreakdown: List<CategoryReportItem> = emptyList(),
    val transactions: List<TransactionEntity> = emptyList()
)
