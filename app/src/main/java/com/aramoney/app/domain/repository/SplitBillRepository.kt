package com.aramoney.app.domain.repository

import com.aramoney.app.data.local.entity.SplitBillDebtEntity
import kotlinx.coroutines.flow.Flow

interface SplitBillRepository {
    fun getAllDebts(): Flow<List<SplitBillDebtEntity>>
    fun getDebtsByDirection(direction: String): Flow<List<SplitBillDebtEntity>>
    fun getUnsettledDebts(): Flow<List<SplitBillDebtEntity>>
    suspend fun getDebtById(id: Long): SplitBillDebtEntity?
    suspend fun insertDebt(debt: SplitBillDebtEntity): Long
    suspend fun updateDebt(debt: SplitBillDebtEntity)
    suspend fun deleteDebt(debt: SplitBillDebtEntity)
    suspend fun settleDebt(id: Long, settledAt: Long = System.currentTimeMillis())
}
