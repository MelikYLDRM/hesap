package com.melikyldrm.hesap.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exchange_rates")
data class ExchangeRateEntity(
    @PrimaryKey
    val currencyPair: String, // e.g., "USD_TRY"
    val rate: Double,
    val baseCurrency: String,
    val targetCurrency: String,
    val lastUpdated: Long = System.currentTimeMillis()
)

