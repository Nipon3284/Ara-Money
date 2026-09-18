package com.aramoney.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.aramoney.app.data.local.entity.SplitBillDebtEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SplitBillDao {

    @Query("SELECT * FROM split_bill_debts ORDER BY isSettled ASC, createdAt DESC")
    fun getAllDebts(): Flow<List<SplitBillDebtEntity>>

    @Query("SELECT * FROM split_bill_debts WHERE direction = :direction ORDER BY isSettled ASC, createdAt DESC")
    fun getDebtsByDirection(direction: String): Flow<List<SplitBillDebtEntity>>

    @Query("SELECT * FROM split_bill_debts WHERE isSettled = 0 ORDER BY createdAt DESC")
    fun getUnsettledDebts(): Flow<List<SplitBillDebtEntity>>

    @Query("SELECT * FROM split_bill_debts WHERE id = :id LIMIT 1")
    suspend fun getDebtById(id: Long): SplitBillDebtEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebt(debt: SplitBillDebtEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebts(debts: List<SplitBillDebtEntity>)

    @Update
    suspend fun updateDebt(debt: SplitBillDebtEntity)

    @Delete
    suspend fun deleteDebt(debt: SplitBillDebtEntity)

    @Query("UPDATE split_bill_debts SET isSettled = 1, settledAt = :settledAt WHERE id = :id")
    suspend fun settleDebt(id: Long, settledAt: Long)
}
