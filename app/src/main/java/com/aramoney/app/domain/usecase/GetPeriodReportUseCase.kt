package com.aramoney.app.domain.usecase

import com.aramoney.app.domain.model.CategoryReportItem
import com.aramoney.app.domain.model.PeriodReportData
import com.aramoney.app.domain.model.ReportPeriodType
import com.aramoney.app.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class GetPeriodReportUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    operator fun invoke(
        periodType: ReportPeriodType,
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<PeriodReportData> {
        val zone = ZoneId.systemDefault()
        val startMillis = startDate.atStartOfDay(zone).toInstant().toEpochMilli()
        val endMillis = endDate.atTime(23, 59, 59, 999_000_000).atZone(zone).toInstant().toEpochMilli()

        return combine(
            transactionRepository.getTotalExpenseBetween(startMillis, endMillis),
            transactionRepository.getTotalIncomeBetween(startMillis, endMillis),
            transactionRepository.getExpenseGroupedByCategory(startMillis, endMillis),
            transactionRepository.getTransactionsBetween(startMillis, endMillis)
        ) { totalExpense, totalIncome, categoryGrouped, transactions ->

            val items = categoryGrouped.map { item ->
                val percentage = if (totalExpense > 0.0) {
                    ((item.totalAmount / totalExpense) * 100.0).toFloat()
                } else 0f

                CategoryReportItem(
                    categoryId = item.categoryId,
                    categoryName = item.categoryName,
                    iconResName = item.iconResName,
                    tintColorHex = item.tintColorHex,
                    totalAmount = item.totalAmount,
                    percentage = percentage,
                    transactionCount = item.transactionCount
                )
            }

            PeriodReportData(
                periodType = periodType,
                startDate = startDate,
                endDate = endDate,
                totalExpense = totalExpense,
                totalIncome = totalIncome,
                netSavings = totalIncome - totalExpense,
                categoryBreakdown = items,
                transactions = transactions
            )
        }
    }
}
