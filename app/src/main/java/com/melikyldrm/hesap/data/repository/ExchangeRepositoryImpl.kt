package com.melikyldrm.hesap.data.repository

import com.melikyldrm.hesap.data.local.dao.ExchangeRateDao
import com.melikyldrm.hesap.data.local.entity.ExchangeRateEntity
import com.melikyldrm.hesap.data.remote.TcmbXmlParser
import com.melikyldrm.hesap.data.remote.api.ExchangeRateApi
import com.melikyldrm.hesap.data.remote.api.TcmbApi
import com.melikyldrm.hesap.domain.model.ExchangeRate
import com.melikyldrm.hesap.domain.repository.ExchangeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExchangeRepositoryImpl @Inject constructor(
    private val exchangeRateApi: ExchangeRateApi,
    private val tcmbApi: TcmbApi,
    private val tcmbXmlParser: TcmbXmlParser,
    private val exchangeRateDao: ExchangeRateDao
) : ExchangeRepository {

    companion object {
        // Cache duration: 1 hour
        private const val CACHE_DURATION_MS = 60 * 60 * 1000L
    }

    override fun getAllCachedRates(): Flow<List<ExchangeRate>> {
        return exchangeRateDao.getAllRates().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getRatesForBase(baseCurrency: String): Flow<List<ExchangeRate>> {
        return exchangeRateDao.getRatesForBase(baseCurrency).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getExchangeRate(baseCurrency: String, targetCurrency: String): ExchangeRate? {
        // First try to get from cache
        val cached = exchangeRateDao.getRate(baseCurrency, targetCurrency)

        // If cache is fresh, return it
        if (cached != null && !isStale(cached.lastUpdated)) {
            return cached.toDomain()
        }

        // Otherwise fetch from TCMB API
        return try {
            fetchFromTcmb()
            exchangeRateDao.getRate(baseCurrency, targetCurrency)?.toDomain()
        } catch (_: Exception) {
            // If TCMB fails, try Frankfurter API as fallback
            try {
                val response = exchangeRateApi.getLatestRates(baseCurrency, targetCurrency)
                val rate = response.rates[targetCurrency] ?: return cached?.toDomain()

                val entity = ExchangeRateEntity(
                    currencyPair = "${baseCurrency}_${targetCurrency}",
                    rate = rate,
                    baseCurrency = baseCurrency,
                    targetCurrency = targetCurrency,
                    lastUpdated = System.currentTimeMillis()
                )
                exchangeRateDao.insert(entity)
                entity.toDomain()
            } catch (_: Exception) {
                // If both APIs fail, return stale cache if available
                cached?.toDomain()
            }
        }
    }

    override suspend fun fetchLatestRates(
        baseCurrency: String,
        targetCurrencies: List<String>?
    ): Result<List<ExchangeRate>> {
        return try {
            // Primarily use TCMB API
            val rates = fetchFromTcmb()
            Result.success(rates)
        } catch (_: Exception) {
            // Fallback to Frankfurter API
            try {
                val targetString = targetCurrencies?.joinToString(",")
                val response = exchangeRateApi.getLatestRates(baseCurrency, targetString)

                val entities = response.rates.map { (currency, rate) ->
                    ExchangeRateEntity(
                        currencyPair = "${baseCurrency}_${currency}",
                        rate = rate,
                        baseCurrency = baseCurrency,
                        targetCurrency = currency,
                        lastUpdated = System.currentTimeMillis()
                    )
                }

                exchangeRateDao.insertAll(entities)
                Result.success(entities.map { it.toDomain() })
            } catch (e2: Exception) {
                Result.failure(e2)
            }
        }
    }

    private suspend fun fetchFromTcmb(): List<ExchangeRate> {
        val xmlData = tcmbApi.getTodayRates()
        val rates = tcmbXmlParser.parseRates(xmlData)

        if (rates.isEmpty()) {
            throw Exception("TCMB'den kur bilgisi alınamadı")
        }

        // Save to database
        val entities = rates.map { rate ->
            ExchangeRateEntity(
                currencyPair = "${rate.baseCurrency}_${rate.targetCurrency}",
                rate = rate.rate,
                baseCurrency = rate.baseCurrency,
                targetCurrency = rate.targetCurrency,
                lastUpdated = rate.timestamp
            )
        }

        // Also add reverse rates (TRY to foreign currency)
        val reverseEntities = rates.mapNotNull { rate ->
            if (rate.rate > 0) {
                ExchangeRateEntity(
                    currencyPair = "TRY_${rate.baseCurrency}",
                    rate = 1.0 / rate.rate,
                    baseCurrency = "TRY",
                    targetCurrency = rate.baseCurrency,
                    lastUpdated = rate.timestamp
                )
            } else null
        }

        exchangeRateDao.insertAll(entities + reverseEntities)
        return rates
    }

    override suspend fun convertCurrency(
        amount: Double,
        fromCurrency: String,
        toCurrency: String
    ): Result<Double> {
        return try {
            if (fromCurrency == toCurrency) {
                return Result.success(amount)
            }

            // Try direct rate first
            val rate = getExchangeRate(fromCurrency, toCurrency)

            if (rate != null) {
                return Result.success(amount * rate.rate)
            }

            // Try cross rate calculation via TRY
            val allRates = exchangeRateDao.getAllRatesSync().map { it.toDomain() }
            val crossRate = tcmbXmlParser.calculateCrossRate(allRates, fromCurrency, toCurrency)

            if (crossRate != null) {
                return Result.success(amount * crossRate)
            }

            Result.failure(Exception("Döviz kuru bulunamadı"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun isDataStale(): Boolean {
        val lastUpdate = exchangeRateDao.getLastUpdateTime() ?: return true
        return isStale(lastUpdate)
    }

    override suspend fun getLastUpdateTime(): Long? {
        return exchangeRateDao.getLastUpdateTime()
    }

    private fun isStale(timestamp: Long): Boolean {
        return System.currentTimeMillis() - timestamp > CACHE_DURATION_MS
    }

    private fun ExchangeRateEntity.toDomain(): ExchangeRate {
        return ExchangeRate(
            baseCurrency = baseCurrency,
            targetCurrency = targetCurrency,
            rate = rate,
            timestamp = lastUpdated
        )
    }
}

