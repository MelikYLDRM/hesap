package com.melikyldrm.hesap.domain.repository

import com.melikyldrm.hesap.domain.model.ExchangeRate
import kotlinx.coroutines.flow.Flow

interface ExchangeRepository {
    fun getAllCachedRates(): Flow<List<ExchangeRate>>
    fun getRatesForBase(baseCurrency: String): Flow<List<ExchangeRate>>
    suspend fun getExchangeRate(baseCurrency: String, targetCurrency: String): ExchangeRate?
    suspend fun fetchLatestRates(baseCurrency: String, targetCurrencies: List<String>? = null): Result<List<ExchangeRate>>
    suspend fun convertCurrency(amount: Double, fromCurrency: String, toCurrency: String): Result<Double>
    suspend fun isDataStale(): Boolean
    suspend fun getLastUpdateTime(): Long?
}

