package com.melikyldrm.hesap.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ExchangeRateResponse(
    @SerializedName("amount")
    val amount: Double = 1.0,

    @SerializedName("base")
    val base: String,

    @SerializedName("date")
    val date: String,

    @SerializedName("rates")
    val rates: Map<String, Double>
)

