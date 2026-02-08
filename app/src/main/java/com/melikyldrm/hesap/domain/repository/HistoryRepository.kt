package com.melikyldrm.hesap.domain.repository

import com.melikyldrm.hesap.domain.model.CalculationHistory
import com.melikyldrm.hesap.domain.model.CalculationType
import kotlinx.coroutines.flow.Flow

interface HistoryRepository {
    fun getAllHistory(): Flow<List<CalculationHistory>>
    fun getHistoryByType(type: CalculationType): Flow<List<CalculationHistory>>
    fun getFavorites(): Flow<List<CalculationHistory>>
    fun searchHistory(query: String): Flow<List<CalculationHistory>>
    fun getRecentHistory(limit: Int): Flow<List<CalculationHistory>>
    suspend fun getHistoryById(id: Long): CalculationHistory?
    suspend fun saveHistory(history: CalculationHistory): Long
    suspend fun toggleFavorite(id: Long)
    suspend fun deleteHistory(id: Long)
    suspend fun clearNonFavorites()
    suspend fun clearAll()
}

