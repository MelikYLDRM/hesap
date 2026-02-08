package com.melikyldrm.hesap.domain.model

data class CalculationHistory(
    val id: Long = 0,
    val expression: String,
    val result: String,
    val type: CalculationType,
    val subType: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
)

