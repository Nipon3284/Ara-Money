package com.aramoney.app.domain.model

import com.aramoney.app.data.local.entity.SplitBillDebtEntity

/**
 * Pengelompokan data hutang-piutang berdasarkan nama orang.
 */
data class PersonDebtGroup(
    val friendName: String,
    val direction: String,
    val totalUnsettledAmount: Double,
    val totalSettledAmount: Double,
    val unsettledCount: Int,
    val settledCount: Int,
    val debts: List<SplitBillDebtEntity>,
    val latestDate: Long
)
