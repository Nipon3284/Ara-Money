package com.aramoney.app.domain.usecase

import com.aramoney.app.data.local.entity.TransactionEntity
import com.aramoney.app.domain.model.DebtCategoryConstants
import com.aramoney.app.domain.repository.SplitBillRepository
import com.aramoney.app.domain.repository.TransactionRepository
import javax.inject.Inject

/**
 * Use case untuk menyelesaikan (melunasi) catatan hutang/piutang.
 * Menyediakan opsi otomatis untuk membuat transaksi pemasukan/pengeluaran
 * dengan kategori khusus sistem (Hutang/Piutang) yang terkunci.
 */
class SettleSplitBillUseCase @Inject constructor(
    private val splitBillRepository: SplitBillRepository,
    private val transactionRepository: TransactionRepository
) {

    suspend operator fun invoke(
        debtId: Long,
        createTransaction: Boolean
    ): Result<Unit> {
        val debt = splitBillRepository.getDebtById(debtId)
            ?: return Result.failure(IllegalArgumentException("Catatan hutang tidak ditemukan"))

        // 1. Tandai lunas di tabel split bill
        splitBillRepository.settleDebt(debtId, System.currentTimeMillis())

        // 2. Jika dipilih untuk dicatat ke riwayat transaksi keuangan
        if (createTransaction) {
            val isFriendPaidBack = debt.direction == "I_PAID_FOR_FRIEND"

            // Gunakan kategori sistem yang tepat
            val categoryId: Long
            val type: String
            val notePrefix: String

            if (isFriendPaidBack) {
                // Teman bayar ke kita = Pemasukan (INCOME) → Kategori Piutang
                categoryId = DebtCategoryConstants.CATEGORY_ID_PIUTANG
                type = "INCOME"
                notePrefix = "Pelunasan piutang dari ${debt.friendName}"
            } else {
                // Kita bayar ke teman = Pengeluaran (EXPENSE) → Kategori Hutang
                categoryId = DebtCategoryConstants.CATEGORY_ID_HUTANG
                type = "EXPENSE"
                notePrefix = "Pembayaran hutang ke ${debt.friendName}"
            }

            val fullNote = if (debt.note.isNotBlank()) "$notePrefix (${debt.note})" else notePrefix

            val transaction = TransactionEntity(
                amount = debt.amount,
                type = type,
                categoryId = categoryId,
                timestamp = System.currentTimeMillis(),
                note = fullNote,
                isSplitBillRelated = true
            )

            transactionRepository.insertTransaction(transaction)
        }

        return Result.success(Unit)
    }
}
