package com.aramoney.app.data.repository

import com.aramoney.app.data.local.dao.TransactionDao
import com.aramoney.app.data.local.entity.CategorySpendSum
import com.aramoney.app.data.local.entity.TransactionEntity
import com.aramoney.app.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao
) : TransactionRepository {

    override fun getAllTransactions(): Flow<List<TransactionEntity>> =
        transactionDao.getAllTransactions()

    override fun getRecentTransactions(limit: Int): Flow<List<TransactionEntity>> =
        transactionDao.getRecentTransactions(limit)

    override fun getTransactionsBetween(start: Long, end: Long): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsBetween(start, end)

    override fun getTotalExpenseBetween(start: Long, end: Long): Flow<Double> =
        transactionDao.getTotalExpenseBetween(start, end)

    override fun getTotalIncomeBetween(start: Long, end: Long): Flow<Double> =
        transactionDao.getTotalIncomeBetween(start, end)

    override fun getTotalExpenseAll(): Flow<Double> =
        transactionDao.getTotalExpenseAll()

    override fun getTotalIncomeAll(): Flow<Double> =
        transactionDao.getTotalIncomeAll()

    override fun getExpenseGroupedByCategory(start: Long, end: Long): Flow<List<CategorySpendSum>> =
        transactionDao.getExpenseGroupedByCategory(start, end)

    override suspend fun getTransactionById(id: Long): TransactionEntity? =
        transactionDao.getTransactionById(id)

    override suspend fun insertTransaction(transaction: TransactionEntity): Long =
        transactionDao.insertTransaction(transaction)

    override suspend fun updateTransaction(transaction: TransactionEntity) =
        transactionDao.updateTransaction(transaction)

    override suspend fun deleteTransaction(transaction: TransactionEntity) =
        transactionDao.deleteTransaction(transaction)

    override suspend fun deleteTransactionById(id: Long) =
        transactionDao.deleteTransactionById(id)
}
