package com.melikyldrm.hesap.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.melikyldrm.hesap.data.local.dao.ExchangeRateDao
import com.melikyldrm.hesap.data.local.dao.HistoryDao
import com.melikyldrm.hesap.data.local.entity.ExchangeRateEntity
import com.melikyldrm.hesap.data.local.entity.HistoryEntity

@Database(
    entities = [
        HistoryEntity::class,
        ExchangeRateEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class HesapDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao
    abstract fun exchangeRateDao(): ExchangeRateDao
}

