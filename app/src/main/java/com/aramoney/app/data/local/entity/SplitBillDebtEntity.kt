package com.aramoney.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entitas pencatatan talangan bersama teman (Split Bill).
 * Mendukung arah:
 * - "I_PAID_FOR_FRIEND": Pengguna menalangi teman (piutang)
 * - "FRIEND_PAID_FOR_ME": Teman menalangi pengguna (utang)
 */
@Entity(tableName = "split_bill_debts")
data class SplitBillDebtEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val friendName: String,
    val amount: Double,
    val note: String,
    val isSettled: Boolean = false,
    val createdAt: Long,
    val settledAt: Long? = null,
    val direction: String       // "I_PAID_FOR_FRIEND" | "FRIEND_PAID_FOR_ME"
)
