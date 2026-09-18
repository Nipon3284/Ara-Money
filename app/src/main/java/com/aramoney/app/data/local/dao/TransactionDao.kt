package com.aramoney.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.aramoney.app.data.local.entity.CategorySpendSum
import com.aramoney.app.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentTransactions(limit: Int = 20): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE timestamp BETWEEN :start AND :end ORDER BY timestamp DESC")
    fun getTransactionsBetween(start: Long, end: Long): Flow<List<TransactionEntity>>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM transactions WHERE type = 'EXPENSE' AND timestamp BETWEEN :start AND :end")
    fun getTotalExpenseBetween(start: Long, end: Long): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM transactions WHERE type = 'INCOME' AND timestamp BETWEEN :start AND :end")
    fun getTotalIncomeBetween(start: Long, end: Long): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM transactions WHERE type = 'EXPENSE'")
    fun getTotalExpenseAll(): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM transactions WHERE type = 'INCOME'")
    fun getTotalIncomeAll(): Flow<Double>

    @Query("""
        SELECT 
            c.id AS categoryId,
            c.name AS categoryName,
            c.iconResName AS iconResName,
            c.tintColorHex AS tintColorHex,
            COALESCE(SUM(t.amount), 0.0) AS totalAmount,
            COUNT(t.id) AS transactionCount
        FROM transactions t
        INNER JOIN categories c ON t.categoryId = c.id
        WHERE t.type = 'EXPENSE' AND t.timestamp BETWEEN :start AND :end
        GROUP BY c.id
        ORDER BY totalAmount DESC
    """)
    fun getExpenseGroupedByCategory(start: Long, end: Long): Flow<List<CategorySpendSum>>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: Long): TransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Long)

    @Query("DELETE FROM transactions")
    suspend fun clearAll()
}
