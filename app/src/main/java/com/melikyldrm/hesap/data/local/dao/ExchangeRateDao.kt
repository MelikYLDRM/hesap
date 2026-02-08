package com.melikyldrm.hesap.data.local.dao

import androidx.room.*
import com.melikyldrm.hesap.data.local.entity.ExchangeRateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExchangeRateDao {

    @Query("SELECT * FROM exchange_rates")
    fun getAllRates(): Flow<List<ExchangeRateEntity>>

    @Query("SELECT * FROM exchange_rates")
    suspend fun getAllRatesSync(): List<ExchangeRateEntity>

    @Query("SELECT * FROM exchange_rates WHERE baseCurrency = :baseCurrency")
    fun getRatesForBase(baseCurrency: String): Flow<List<ExchangeRateEntity>>

    @Query("SELECT * FROM exchange_rates WHERE currencyPair = :pair")
    suspend fun getRate(pair: String): ExchangeRateEntity?

    @Query("SELECT * FROM exchange_rates WHERE baseCurrency = :base AND targetCurrency = :target")
    suspend fun getRate(base: String, target: String): ExchangeRateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rates: List<ExchangeRateEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rate: ExchangeRateEntity)

    @Query("DELETE FROM exchange_rates")
    suspend fun clearAll()

    @Query("SELECT MAX(lastUpdated) FROM exchange_rates")
    suspend fun getLastUpdateTime(): Long?
}

