package com.aramoney.app.data.repository

import com.aramoney.app.data.local.dao.CategoryDao
import com.aramoney.app.data.local.entity.CategoryEntity
import com.aramoney.app.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao
) : CategoryRepository {

    override fun getAllCategories(): Flow<List<CategoryEntity>> =
        categoryDao.getAllCategories()

    override fun getCategoriesByType(isExpense: Boolean): Flow<List<CategoryEntity>> =
        categoryDao.getCategoriesByType(isExpense)

    override fun getCategoryById(id: Long): Flow<CategoryEntity?> =
        categoryDao.getCategoryById(id)

    override suspend fun getCategoryByIdSync(id: Long): CategoryEntity? =
        categoryDao.getCategoryByIdSync(id)

    override suspend fun countCategoryUsage(categoryId: Long): Int =
        categoryDao.countCategoryUsage(categoryId)

    override suspend fun insertCategory(category: CategoryEntity): Long =
        categoryDao.insertCategory(category)

    override suspend fun updateCategory(category: CategoryEntity) =
        categoryDao.updateCategory(category)

    override suspend fun deleteCategory(category: CategoryEntity): Boolean {
        // Cek apakah masih ada kategori lain dari tipe yang sama
        val countSameType = categoryDao.countCategoriesByType(category.isExpense)
        if (countSameType <= 1) {
            return false
        }

        val usageCount = categoryDao.countCategoryUsage(category.id)
        if (usageCount > 0) {
            val fallback = categoryDao.getFallbackCategory(category.isExpense, category.id)
            if (fallback != null) {
                categoryDao.reassignCategoryTransactions(category.id, fallback.id)
            } else {
                return false
            }
        }

        categoryDao.deleteCategory(category)
        return true
    }
}
