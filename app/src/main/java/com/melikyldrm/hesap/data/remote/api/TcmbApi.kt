package com.melikyldrm.hesap.data.remote.api

import retrofit2.http.GET

/**
 * TCMB (Türkiye Cumhuriyet Merkez Bankası) Döviz Kurları API
 * Günlük döviz kurlarını XML formatında sağlar
 */
interface TcmbApi {

    /**
     * Bugünkü döviz kurlarını al
     * XML formatında döner
     */
    @GET("kurlar/today.xml")
    suspend fun getTodayRates(): String

    /**
     * Belirli bir tarihin döviz kurlarını al
     * Format: YYYYMM/DDMMYYYY.xml (örn: 202402/07022026.xml)
     */
    @GET("kurlar/{yearMonth}/{date}.xml")
    suspend fun getRatesForDate(
        @retrofit2.http.Path("yearMonth") yearMonth: String,
        @retrofit2.http.Path("date") date: String
    ): String

    companion object {
        const val BASE_URL = "https://www.tcmb.gov.tr/"
    }
}

