package com.aramoney.app.domain.usecase

import com.aramoney.app.data.local.entity.TransactionEntity
import com.aramoney.app.domain.repository.TransactionRepository
import javax.inject.Inject

class AddTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    companion object {
        const val MAX_AMOUNT = 100_000_000.0 // Maksimum 100 juta rupiah
    }

    suspend operator fun invoke(
        amount: Double,
        type: String,
        categoryId: Long,
        note: String? = null,
        timestamp: Long = System.currentTimeMillis(),
        isSplitBillRelated: Boolean = false
    ): Result<Long> {
        if (amount <= 0.0) {
            return Result.failure(IllegalArgumentException("Nominal harus lebih dari Rp 0 ya, Kak! 🌸"))
        }

        if (amount > MAX_AMOUNT) {
            return Result.failure(IllegalArgumentException("Nominal melebihi batas maksimal Rp100.000.000 ya, Kak! 🌸"))
        }

        if (categoryId <= 0) {
            return Result.failure(IllegalArgumentException("Pilih salah satu kategori dulu ya, Kak! 🎀"))
        }

        val transaction = TransactionEntity(
            amount = amount,
            type = type,
            categoryId = categoryId,
            timestamp = timestamp,
            note = note?.trim()?.ifBlank { null },
            isSplitBillRelated = isSplitBillRelated
        )

        val id = transactionRepository.insertTransaction(transaction)
        return Result.success(id)
    }
}
