package com.melikyldrm.hesap.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.melikyldrm.hesap.data.local.HesapDatabase
import com.melikyldrm.hesap.data.local.dao.ExchangeRateDao
import com.melikyldrm.hesap.data.local.dao.HistoryDao
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
    fun provideHesapDatabase(
        @ApplicationContext context: Context
    ): HesapDatabase {
        return Room.databaseBuilder(
            context,
            HesapDatabase::class.java,
            "hesap_database"
        )
            // Production'da destructive migration kullanıcı verilerini siler!
            // Yeni sürümlerde Migration nesneleri tanımlanmalı.
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideHistoryDao(database: HesapDatabase): HistoryDao {
        return database.historyDao()
    }

    @Provides
    @Singleton
    fun provideExchangeRateDao(database: HesapDatabase): ExchangeRateDao {
        return database.exchangeRateDao()
    }
}

