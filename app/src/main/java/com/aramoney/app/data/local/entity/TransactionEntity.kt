package com.aramoney.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entitas data transaksi harian (pemasukan atau pengeluaran).
 */
@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index("categoryId"),
        Index("timestamp")
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val type: String,                         // "EXPENSE" | "INCOME"
    val categoryId: Long,
    val timestamp: Long,                      // Waktu transaksi dalam epoch millis
    val note: String? = null,
    val isSplitBillRelated: Boolean = false   // Ditandai true jika berasal dari pelunasan split bill
)
