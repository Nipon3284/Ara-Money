package com.aramoney.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.aramoney.app.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Query("SELECT * FROM categories ORDER BY sortOrder ASC, id ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE isExpense = :isExpense ORDER BY sortOrder ASC, id ASC")
    fun getCategoriesByType(isExpense: Boolean): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id LIMIT 1")
    fun getCategoryById(id: Long): Flow<CategoryEntity?>

    @Query("SELECT * FROM categories WHERE id = :id LIMIT 1")
    suspend fun getCategoryByIdSync(id: Long): CategoryEntity?

    @Query("SELECT COUNT(*) FROM transactions WHERE categoryId = :categoryId")
    suspend fun countCategoryUsage(categoryId: Long): Int

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun countAll(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Query("SELECT COUNT(*) FROM categories WHERE isExpense = :isExpense")
    suspend fun countCategoriesByType(isExpense: Boolean): Int

    @Query("SELECT * FROM categories WHERE isExpense = :isExpense AND id != :excludeId ORDER BY sortOrder ASC, id ASC LIMIT 1")
    suspend fun getFallbackCategory(isExpense: Boolean, excludeId: Long): CategoryEntity?

    @Query("UPDATE transactions SET categoryId = :fallbackCategoryId WHERE categoryId = :deletedCategoryId")
    suspend fun reassignCategoryTransactions(deletedCategoryId: Long, fallbackCategoryId: Long)

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)
}
