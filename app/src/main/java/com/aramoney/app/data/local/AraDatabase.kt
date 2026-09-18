package com.aramoney.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.aramoney.app.data.local.dao.CategoryDao
import com.aramoney.app.data.local.dao.SplitBillDao
import com.aramoney.app.data.local.dao.TransactionDao
import com.aramoney.app.data.local.entity.CategoryEntity
import com.aramoney.app.data.local.entity.SplitBillDebtEntity
import com.aramoney.app.data.local.entity.TransactionEntity
import com.aramoney.app.domain.model.DebtCategoryConstants

@Database(
    entities = [
        TransactionEntity::class,
        CategoryEntity::class,
        SplitBillDebtEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AraDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun splitBillDao(): SplitBillDao

    companion object {
        const val DATABASE_NAME = "ara_money.db"

        /**
         * Contoh objek migrasi resmi dari skema v1 ke v2 (misal penambahan kolom target / indeks opsional).
         * Menjamin data mahasiswi tidak hilang (anti-destructive migration di production).
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Contoh jika ke depan ingin menambah indeks atau tabel pendukung tanpa menghapus data yang ada:
                db.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_type ON transactions(type)")
            }
        }

        fun buildDatabase(context: Context): AraDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AraDatabase::class.java,
                DATABASE_NAME
            )
                .addMigrations(MIGRATION_1_2)
                .addCallback(SeedCategoryCallback())
                .build()
        }
    }

    /**
     * Callback database untuk menyemai (seed) kategori bawaan ramah mahasiswi
     * saat pertama kali database SQLite dibuat di perangkat.
     */
    private class SeedCategoryCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            seedInitialCategories(db)
        }

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            val cursor = db.query("SELECT COUNT(*) FROM categories")
            val count = if (cursor.moveToFirst()) cursor.getInt(0) else 0
            cursor.close()
            if (count == 0) {
                seedInitialCategories(db)
            }
            // Pastikan kategori sistem Hutang & Piutang selalu ada
            ensureDebtCategories(db)
        }

        private fun seedInitialCategories(db: SupportSQLiteDatabase) {
            val defaultCategories = listOf(
                // Kategori Pengeluaran Khusus Mahasiswi
                CategoryEntity(1, "Skincare & Make-up", "sparkles", "#F48FB1", isExpense = true, isDefault = true, sortOrder = 1),
                CategoryEntity(2, "Kopi & Boba", "beverage", "#CE93D8", isExpense = true, isDefault = true, sortOrder = 2),
                CategoryEntity(3, "Makan Cantik / Warteg", "food", "#FFCCBC", isExpense = true, isDefault = true, sortOrder = 3),
                CategoryEntity(4, "Ojek Online", "motorcycle", "#81C784", isExpense = true, isDefault = true, sortOrder = 4),
                CategoryEntity(5, "Fotokopi & Kuliah", "book", "#64B5F6", isExpense = true, isDefault = true, sortOrder = 5),
                CategoryEntity(6, "Self Reward", "gift", "#BA68C8", isExpense = true, isDefault = true, sortOrder = 6),
                CategoryEntity(7, "Transportasi Lain", "directions_bus", "#4DB6AC", isExpense = true, isDefault = true, sortOrder = 7),
                CategoryEntity(8, "Lainnya", "category", "#B0BEC5", isExpense = true, isDefault = true, sortOrder = 8),

                // Kategori Pemasukan
                CategoryEntity(9, "Kiriman Ortu", "account_balance_wallet", "#81C784", isExpense = false, isDefault = true, sortOrder = 1),
                CategoryEntity(10, "Beasiswa", "school", "#64B5F6", isExpense = false, isDefault = true, sortOrder = 2),
                CategoryEntity(11, "Kerja Part-Time", "work", "#FFB74D", isExpense = false, isDefault = true, sortOrder = 3),
                CategoryEntity(12, "Lainnya", "payments", "#B0BEC5", isExpense = false, isDefault = true, sortOrder = 4),

                // Kategori Sistem Hutang & Piutang (terkunci, tidak bisa diubah/dihapus)
                CategoryEntity(DebtCategoryConstants.CATEGORY_ID_HUTANG, "Hutang", "debt_expense", "#E57373", isExpense = true, isDefault = true, sortOrder = 99),
                CategoryEntity(DebtCategoryConstants.CATEGORY_ID_PIUTANG, "Piutang", "debt_income", "#66BB6A", isExpense = false, isDefault = true, sortOrder = 99)
            )

            defaultCategories.forEach { cat ->
                val isExpenseInt = if (cat.isExpense) 1 else 0
                val isDefaultInt = if (cat.isDefault) 1 else 0
                db.execSQL(
                    """
                    INSERT OR IGNORE INTO categories (id, name, iconResName, tintColorHex, isExpense, isDefault, sortOrder)
                    VALUES (${cat.id}, '${cat.name}', '${cat.iconResName}', '${cat.tintColorHex}', $isExpenseInt, $isDefaultInt, ${cat.sortOrder})
                    """.trimIndent()
                )
            }
        }

        /**
         * Memastikan kategori sistem Hutang & Piutang selalu ada di database,
         * termasuk untuk pengguna lama yang sudah memiliki data sebelumnya.
         */
        private fun ensureDebtCategories(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                INSERT OR IGNORE INTO categories (id, name, iconResName, tintColorHex, isExpense, isDefault, sortOrder)
                VALUES (${DebtCategoryConstants.CATEGORY_ID_HUTANG}, 'Hutang', 'debt_expense', '#E57373', 1, 1, 99)
                """.trimIndent()
            )
            db.execSQL(
                """
                INSERT OR IGNORE INTO categories (id, name, iconResName, tintColorHex, isExpense, isDefault, sortOrder)
                VALUES (${DebtCategoryConstants.CATEGORY_ID_PIUTANG}, 'Piutang', 'debt_income', '#66BB6A', 0, 1, 99)
                """.trimIndent()
            )

            // Migrasi transaksi pelunasan lama yang sebelumnya salah masuk ke kategori lain
            db.execSQL(
                """
                UPDATE transactions 
                SET categoryId = ${DebtCategoryConstants.CATEGORY_ID_PIUTANG} 
                WHERE isSplitBillRelated = 1 AND type = 'INCOME' AND categoryId != ${DebtCategoryConstants.CATEGORY_ID_PIUTANG}
                """.trimIndent()
            )
            db.execSQL(
                """
                UPDATE transactions 
                SET categoryId = ${DebtCategoryConstants.CATEGORY_ID_HUTANG} 
                WHERE isSplitBillRelated = 1 AND type = 'EXPENSE' AND categoryId != ${DebtCategoryConstants.CATEGORY_ID_HUTANG}
                """.trimIndent()
            )
        }
    }
}
