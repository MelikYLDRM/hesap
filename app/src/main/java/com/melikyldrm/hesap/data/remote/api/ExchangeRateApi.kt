package com.melikyldrm.hesap.data.remote.api

import com.melikyldrm.hesap.data.remote.dto.ExchangeRateResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ExchangeRateApi {

    /**
     * Get latest exchange rates
     * Example: GET /latest?from=EUR&to=USD,GBP,TRY
     */
    @GET("latest")
    suspend fun getLatestRates(
        @Query("from") baseCurrency: String,
        @Query("to") targetCurrencies: String? = null
    ): ExchangeRateResponse

    /**
     * Get exchange rates for a specific date
     * Example: GET /2024-01-15?from=EUR&to=USD,GBP
     */
    @GET("{date}")
    suspend fun getRatesForDate(
        @Path("date") date: String,
        @Query("from") baseCurrency: String,
        @Query("to") targetCurrencies: String? = null
    ): ExchangeRateResponse

    /**
     * Get list of available currencies
     */
    @GET("currencies")
    suspend fun getAvailableCurrencies(): Map<String, String>

    companion object {
        const val BASE_URL = "https://api.frankfurter.app/"
    }
}

