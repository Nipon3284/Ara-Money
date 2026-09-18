package com.aramoney.app.domain.repository

import com.aramoney.app.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getAllCategories(): Flow<List<CategoryEntity>>
    fun getCategoriesByType(isExpense: Boolean): Flow<List<CategoryEntity>>
    fun getCategoryById(id: Long): Flow<CategoryEntity?>
    suspend fun getCategoryByIdSync(id: Long): CategoryEntity?
    suspend fun countCategoryUsage(categoryId: Long): Int
    suspend fun insertCategory(category: CategoryEntity): Long
    suspend fun updateCategory(category: CategoryEntity)
    suspend fun deleteCategory(category: CategoryEntity): Boolean
}
