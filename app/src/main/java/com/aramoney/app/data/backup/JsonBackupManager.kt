package com.aramoney.app.data.backup

import android.content.Context
import android.net.Uri
import com.aramoney.app.data.local.AraDatabase
import com.aramoney.app.data.local.entity.CategoryEntity
import com.aramoney.app.data.local.entity.SplitBillDebtEntity
import com.aramoney.app.data.local.entity.TransactionEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import javax.inject.Inject
import javax.inject.Singleton

data class BackupPreview(
    val transactionCount: Int,
    val categoryCount: Int,
    val splitBillDebtCount: Int,
    val exportedAt: Long
)

@Singleton
class JsonBackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: AraDatabase
) {

    /**
     * Ekspor seluruh data lokal ke berkas JSON menggunakan Android Storage Access Framework (SAF).
     * Tidak membutuhkan permission android.permission.WRITE_EXTERNAL_STORAGE.
     */
    suspend fun exportDataToJsonUri(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val transactions = database.transactionDao().getAllTransactions().first()
            val categories = database.categoryDao().getAllCategories().first()
            val debts = database.splitBillDao().getAllDebts().first()

            val rootJson = JSONObject().apply {
                put("app", "Ara Money")
                put("version", 1)
                put("exportedAt", System.currentTimeMillis())

                // 1. Array Categories
                val categoriesArray = JSONArray()
                categories.forEach { cat ->
                    val catObj = JSONObject().apply {
                        put("id", cat.id)
                        put("name", cat.name)
                        put("iconResName", cat.iconResName)
                        put("tintColorHex", cat.tintColorHex)
                        put("isExpense", cat.isExpense)
                        put("isDefault", cat.isDefault)
                        put("sortOrder", cat.sortOrder)
                    }
                    categoriesArray.put(catObj)
                }
                put("categories", categoriesArray)

                // 2. Array Transactions
                val transactionsArray = JSONArray()
                transactions.forEach { trans ->
                    val transObj = JSONObject().apply {
                        put("id", trans.id)
                        put("amount", trans.amount)
                        put("type", trans.type)
                        put("categoryId", trans.categoryId)
                        put("timestamp", trans.timestamp)
                        put("note", trans.note ?: "")
                        put("isSplitBillRelated", trans.isSplitBillRelated)
                    }
                    transactionsArray.put(transObj)
                }
                put("transactions", transactionsArray)

                // 3. Array Split Bill Debts
                val debtsArray = JSONArray()
                debts.forEach { debt ->
                    val debtObj = JSONObject().apply {
                        put("id", debt.id)
                        put("friendName", debt.friendName)
                        put("amount", debt.amount)
                        put("note", debt.note)
                        put("isSettled", debt.isSettled)
                        put("createdAt", debt.createdAt)
                        put("settledAt", debt.settledAt ?: -1L)
                        put("direction", debt.direction)
                    }
                    debtsArray.put(debtObj)
                }
                put("splitBillDebts", debtsArray)
            }

            // Tulis string JSON ke OutputStream SAF
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream, Charsets.UTF_8).use { writer ->
                    writer.write(rootJson.toString(2))
                }
            } ?: throw IllegalStateException("Tidak dapat membuka file penyimpanan")
        }
    }

    /**
     * Membaca dan memvalidasi berkas cadangan JSON sebelum dipulihkan.
     */
    suspend fun inspectBackupFile(uri: Uri): Result<BackupPreview> = withContext(Dispatchers.IO) {
        runCatching {
            val jsonString = readStringFromUri(uri)
            val root = JSONObject(jsonString)

            if (!root.has("app") || root.getString("app") != "Ara Money") {
                throw IllegalArgumentException("Format file tidak sesuai. Bukan cadangan resmi Ara Money.")
            }

            val transArray = root.optJSONArray("transactions") ?: JSONArray()
            val catArray = root.optJSONArray("categories") ?: JSONArray()
            val debtsArray = root.optJSONArray("splitBillDebts") ?: JSONArray()
            val exportedAt = root.optLong("exportedAt", System.currentTimeMillis())

            BackupPreview(
                transactionCount = transArray.length(),
                categoryCount = catArray.length(),
                splitBillDebtCount = debtsArray.length(),
                exportedAt = exportedAt
            )
        }
    }

    /**
     * Memulihkan (Restore) data dari file JSON ke basis data SQLite.
     */
    suspend fun restoreDataFromJsonUri(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val jsonString = readStringFromUri(uri)
            val root = JSONObject(jsonString)

            if (root.optString("app") != "Ara Money") {
                throw IllegalArgumentException("Berkas tidak valid.")
            }

            // Parse Categories
            val catArray = root.optJSONArray("categories") ?: JSONArray()
            val categoriesList = mutableListOf<CategoryEntity>()
            for (i in 0 until catArray.length()) {
                val obj = catArray.getJSONObject(i)
                categoriesList.add(
                    CategoryEntity(
                        id = obj.getLong("id"),
                        name = obj.getString("name"),
                        iconResName = obj.getString("iconResName"),
                        tintColorHex = obj.getString("tintColorHex"),
                        isExpense = obj.getBoolean("isExpense"),
                        isDefault = obj.optBoolean("isDefault", true),
                        sortOrder = obj.optInt("sortOrder", 0)
                    )
                )
            }

            // Parse Transactions
            val transArray = root.optJSONArray("transactions") ?: JSONArray()
            val transactionsList = mutableListOf<TransactionEntity>()
            for (i in 0 until transArray.length()) {
                val obj = transArray.getJSONObject(i)
                val noteStr = obj.optString("note", "")
                transactionsList.add(
                    TransactionEntity(
                        id = obj.getLong("id"),
                        amount = obj.getDouble("amount"),
                        type = obj.getString("type"),
                        categoryId = obj.getLong("categoryId"),
                        timestamp = obj.getLong("timestamp"),
                        note = noteStr.ifBlank { null },
                        isSplitBillRelated = obj.optBoolean("isSplitBillRelated", false)
                    )
                )
            }

            // Parse Debts
            val debtsArray = root.optJSONArray("splitBillDebts") ?: JSONArray()
            val debtsList = mutableListOf<SplitBillDebtEntity>()
            for (i in 0 until debtsArray.length()) {
                val obj = debtsArray.getJSONObject(i)
                val settledAtLong = obj.optLong("settledAt", -1L)
                debtsList.add(
                    SplitBillDebtEntity(
                        id = obj.getLong("id"),
                        friendName = obj.getString("friendName"),
                        amount = obj.getDouble("amount"),
                        note = obj.optString("note", ""),
                        isSettled = obj.getBoolean("isSettled"),
                        createdAt = obj.getLong("createdAt"),
                        settledAt = if (settledAtLong > 0) settledAtLong else null,
                        direction = obj.getString("direction")
                    )
                )
            }

            // Masukkan data ke Room secara aman
            if (categoriesList.isNotEmpty()) {
                database.categoryDao().insertCategories(categoriesList)
            }
            if (transactionsList.isNotEmpty()) {
                database.transactionDao().insertTransactions(transactionsList)
            }
            if (debtsList.isNotEmpty()) {
                database.splitBillDao().insertDebts(debtsList)
            }
        }
    }

    private fun readStringFromUri(uri: Uri): String {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader ->
                return reader.readText()
            }
        } ?: throw IllegalStateException("Tidak dapat membaca berkas cadangan")
    }
}
