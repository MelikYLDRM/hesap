package com.melikyldrm.hesap.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.melikyldrm.hesap.domain.model.CalculationHistory
import com.melikyldrm.hesap.domain.model.CalculationType
import com.melikyldrm.hesap.domain.repository.HistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryUiState(
    val allHistory: List<CalculationHistory> = emptyList(),
    val filteredHistory: List<CalculationHistory> = emptyList(),
    val selectedFilter: HistoryFilter = HistoryFilter.ALL,
    val searchQuery: String = "",
    val isLoading: Boolean = true
)

enum class HistoryFilter(val title: String) {
    ALL("Tümü"),
    FAVORITES("Favoriler"),
    BASIC("Temel"),
    SCIENTIFIC("Bilimsel"),
    FINANCE("Finans"),
    CONVERTER("Dönüştürücü")
}

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val historyRepository: HistoryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HistoryUiState())
    val state: StateFlow<HistoryUiState> = _state.asStateFlow()

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            historyRepository.getAllHistory()
                .collect { history ->
                    _state.update { it.copy(allHistory = history, isLoading = false) }
                    applyFilter()
                }
        }
    }

    fun setFilter(filter: HistoryFilter) {
        _state.update { it.copy(selectedFilter = filter) }
        applyFilter()
    }

    fun setSearchQuery(query: String) {
        _state.update { it.copy(searchQuery = query) }
        applyFilter()
    }

    private fun applyFilter() {
        _state.update { currentState ->
            var filtered = currentState.allHistory

            // Apply type filter
            filtered = when (currentState.selectedFilter) {
                HistoryFilter.ALL -> filtered
                HistoryFilter.FAVORITES -> filtered.filter { it.isFavorite }
                HistoryFilter.BASIC -> filtered.filter { it.type == CalculationType.BASIC }
                HistoryFilter.SCIENTIFIC -> filtered.filter { it.type == CalculationType.SCIENTIFIC }
                HistoryFilter.FINANCE -> filtered.filter { it.type == CalculationType.FINANCE }
                HistoryFilter.CONVERTER -> filtered.filter { it.type == CalculationType.CONVERTER }
            }

            // Apply search query
            if (currentState.searchQuery.isNotBlank()) {
                val query = currentState.searchQuery.lowercase()
                filtered = filtered.filter {
                    it.expression.lowercase().contains(query) ||
                    it.result.lowercase().contains(query)
                }
            }

            currentState.copy(filteredHistory = filtered)
        }
    }

    fun toggleFavorite(id: Long) {
        viewModelScope.launch {
            historyRepository.toggleFavorite(id)
        }
    }

    fun deleteHistory(id: Long) {
        viewModelScope.launch {
            historyRepository.deleteHistory(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            historyRepository.clearAll()
        }
    }

    fun clearNonFavorites() {
        viewModelScope.launch {
            historyRepository.clearNonFavorites()
        }
    }
}
