package com.aramoney.app.data.repository

import com.aramoney.app.data.local.dao.SplitBillDao
import com.aramoney.app.data.local.entity.SplitBillDebtEntity
import com.aramoney.app.domain.repository.SplitBillRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SplitBillRepositoryImpl @Inject constructor(
    private val splitBillDao: SplitBillDao
) : SplitBillRepository {

    override fun getAllDebts(): Flow<List<SplitBillDebtEntity>> =
        splitBillDao.getAllDebts()

    override fun getDebtsByDirection(direction: String): Flow<List<SplitBillDebtEntity>> =
        splitBillDao.getDebtsByDirection(direction)

    override fun getUnsettledDebts(): Flow<List<SplitBillDebtEntity>> =
        splitBillDao.getUnsettledDebts()

    override suspend fun getDebtById(id: Long): SplitBillDebtEntity? =
        splitBillDao.getDebtById(id)

    override suspend fun insertDebt(debt: SplitBillDebtEntity): Long =
        splitBillDao.insertDebt(debt)

    override suspend fun updateDebt(debt: SplitBillDebtEntity) =
        splitBillDao.updateDebt(debt)

    override suspend fun deleteDebt(debt: SplitBillDebtEntity) =
        splitBillDao.deleteDebt(debt)

    override suspend fun settleDebt(id: Long, settledAt: Long) =
        splitBillDao.settleDebt(id, settledAt)
}
