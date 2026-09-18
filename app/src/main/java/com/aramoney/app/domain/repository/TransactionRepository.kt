package com.aramoney.app.domain.repository

import com.aramoney.app.data.local.entity.CategorySpendSum
import com.aramoney.app.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun getAllTransactions(): Flow<List<TransactionEntity>>
    fun getRecentTransactions(limit: Int = 20): Flow<List<TransactionEntity>>
    fun getTransactionsBetween(start: Long, end: Long): Flow<List<TransactionEntity>>
    fun getTotalExpenseBetween(start: Long, end: Long): Flow<Double>
    fun getTotalIncomeBetween(start: Long, end: Long): Flow<Double>
    fun getTotalExpenseAll(): Flow<Double>
    fun getTotalIncomeAll(): Flow<Double>
    fun getExpenseGroupedByCategory(start: Long, end: Long): Flow<List<CategorySpendSum>>
    suspend fun getTransactionById(id: Long): TransactionEntity?
    suspend fun insertTransaction(transaction: TransactionEntity): Long
    suspend fun updateTransaction(transaction: TransactionEntity)
    suspend fun deleteTransaction(transaction: TransactionEntity)
    suspend fun deleteTransactionById(id: Long)
}
