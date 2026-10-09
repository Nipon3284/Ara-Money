package com.aramoney.app.domain.usecase

import com.aramoney.app.data.local.entity.TransactionEntity
import com.aramoney.app.domain.repository.TransactionRepository
import com.aramoney.app.util.AmountInput
import com.aramoney.app.util.CurrencyFormatter
import javax.inject.Inject

class AddTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    companion object {
        const val MAX_AMOUNT = AmountInput.MAX_AMOUNT.toDouble()

        /**
         * Validasi bersama untuk tambah & ubah transaksi.
         * @return pesan error, atau null jika valid.
         */
        fun validate(amount: Double, categoryId: Long, timestamp: Long, now: Long = System.currentTimeMillis()): String? = when {
            amount <= 0.0 -> "Nominal harus lebih dari Rp0 ya, Kak"
            amount > MAX_AMOUNT -> "Nominal melebihi batas maksimal ${CurrencyFormatter.formatRupiah(MAX_AMOUNT)} ya, Kak"
            categoryId <= 0 -> "Pilih salah satu kategori dulu ya, Kak"
            // Toleransi 1 menit untuk perbedaan jam
            timestamp > now + 60_000L -> "Tanggal transaksi tidak boleh di masa depan"
            else -> null
        }

        fun normalizeNote(note: String?): String? = note?.trim()?.ifBlank { null }
    }

    suspend operator fun invoke(
        amount: Double,
        type: String,
        categoryId: Long,
        note: String? = null,
        timestamp: Long = System.currentTimeMillis(),
        isSplitBillRelated: Boolean = false
    ): Result<Long> {
        validate(amount, categoryId, timestamp)?.let {
            return Result.failure(IllegalArgumentException(it))
        }

        val transaction = TransactionEntity(
            amount = amount,
            type = type,
            categoryId = categoryId,
            timestamp = timestamp,
            note = normalizeNote(note),
            isSplitBillRelated = isSplitBillRelated
        )

        val id = transactionRepository.insertTransaction(transaction)
        return Result.success(id)
    }
}
