package com.aramoney.app.di

import android.content.Context
import com.aramoney.app.data.local.AraDatabase
import com.aramoney.app.data.local.dao.CategoryDao
import com.aramoney.app.data.local.dao.SplitBillDao
import com.aramoney.app.data.local.dao.TransactionDao
import com.aramoney.app.data.repository.CategoryRepositoryImpl
import com.aramoney.app.data.repository.SplitBillRepositoryImpl
import com.aramoney.app.data.repository.TransactionRepositoryImpl
import com.aramoney.app.domain.repository.CategoryRepository
import com.aramoney.app.domain.repository.SplitBillRepository
import com.aramoney.app.domain.repository.TransactionRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AraDatabase {
        return AraDatabase.buildDatabase(context)
    }

    @Provides
    fun provideTransactionDao(database: AraDatabase): TransactionDao {
        return database.transactionDao()
    }

    @Provides
    fun provideCategoryDao(database: AraDatabase): CategoryDao {
        return database.categoryDao()
    }

    @Provides
    fun provideSplitBillDao(database: AraDatabase): SplitBillDao {
        return database.splitBillDao()
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(
        impl: TransactionRepositoryImpl
    ): TransactionRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(
        impl: CategoryRepositoryImpl
    ): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindSplitBillRepository(
        impl: SplitBillRepositoryImpl
    ): SplitBillRepository
}
