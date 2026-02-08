package com.melikyldrm.hesap.data.repository

import com.melikyldrm.hesap.data.local.dao.HistoryDao
import com.melikyldrm.hesap.data.local.entity.HistoryEntity
import com.melikyldrm.hesap.domain.model.CalculationHistory
import com.melikyldrm.hesap.domain.model.CalculationType
import com.melikyldrm.hesap.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HistoryRepositoryImpl @Inject constructor(
    private val historyDao: HistoryDao
) : HistoryRepository {

    override fun getAllHistory(): Flow<List<CalculationHistory>> {
        return historyDao.getAllHistory().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getHistoryByType(type: CalculationType): Flow<List<CalculationHistory>> {
        return historyDao.getHistoryByType(type.name).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getFavorites(): Flow<List<CalculationHistory>> {
        return historyDao.getFavorites().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun searchHistory(query: String): Flow<List<CalculationHistory>> {
        return historyDao.searchHistory(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getRecentHistory(limit: Int): Flow<List<CalculationHistory>> {
        return historyDao.getRecentHistory(limit).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getHistoryById(id: Long): CalculationHistory? {
        return historyDao.getHistoryById(id)?.toDomain()
    }

    override suspend fun saveHistory(history: CalculationHistory): Long {
        return historyDao.insert(history.toEntity())
    }

    override suspend fun toggleFavorite(id: Long) {
        historyDao.toggleFavorite(id)
    }

    override suspend fun deleteHistory(id: Long) {
        historyDao.deleteById(id)
    }

    override suspend fun clearNonFavorites() {
        historyDao.clearNonFavorites()
    }

    override suspend fun clearAll() {
        historyDao.clearAll()
    }

    // Extension functions for mapping
    private fun HistoryEntity.toDomain(): CalculationHistory {
        return CalculationHistory(
            id = id,
            expression = expression,
            result = result,
            type = CalculationType.valueOf(type),
            subType = subType,
            timestamp = timestamp,
            isFavorite = isFavorite
        )
    }

    private fun CalculationHistory.toEntity(): HistoryEntity {
        return HistoryEntity(
            id = id,
            expression = expression,
            result = result,
            type = type.name,
            subType = subType,
            timestamp = timestamp,
            isFavorite = isFavorite
        )
    }
}

