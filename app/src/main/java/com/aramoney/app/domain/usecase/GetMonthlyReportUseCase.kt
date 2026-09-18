package com.aramoney.app.domain.usecase

import com.aramoney.app.domain.model.CategoryReportItem
import com.aramoney.app.domain.model.MonthlyReportData
import com.aramoney.app.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject

class GetMonthlyReportUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    operator fun invoke(yearMonth: YearMonth): Flow<MonthlyReportData> {
        val zone = ZoneId.systemDefault()
        val startMillis = yearMonth.atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val endMillis = yearMonth.atEndOfMonth().atTime(23, 59, 59, 999_000_000).atZone(zone).toInstant().toEpochMilli()

        return combine(
            transactionRepository.getTotalExpenseBetween(startMillis, endMillis),
            transactionRepository.getTotalIncomeBetween(startMillis, endMillis),
            transactionRepository.getExpenseGroupedByCategory(startMillis, endMillis)
        ) { totalExpense, totalIncome, categoryGrouped ->

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

            MonthlyReportData(
                yearMonth = yearMonth,
                totalExpense = totalExpense,
                totalIncome = totalIncome,
                netSavings = totalIncome - totalExpense,
                categoryBreakdown = items
            )
        }
    }
}
