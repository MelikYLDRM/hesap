package com.melikyldrm.hesap.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calculation_history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val expression: String,
    val result: String,
    val type: String, // BASIC, SCIENTIFIC, FINANCE, CONVERTER
    val subType: String? = null, // KDV, TEVKIFAT, FAIZ, etc.
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
)

