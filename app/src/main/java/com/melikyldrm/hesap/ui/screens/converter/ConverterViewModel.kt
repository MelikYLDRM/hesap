package com.melikyldrm.hesap.ui.screens.converter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.melikyldrm.hesap.domain.engine.UnitConverter
import com.melikyldrm.hesap.domain.model.*
import com.melikyldrm.hesap.domain.repository.ExchangeRepository
import com.melikyldrm.hesap.domain.repository.HistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ConverterUiState(
    val selectedCategory: UnitCategory = UnitCategory.LENGTH,
    val inputValue: String = "",
    val fromUnit: String = "",
    val toUnit: String = "",
    val result: String = "",
    val availableUnits: List<Pair<String, String>> = emptyList(),
    // Currency specific
    val exchangeRates: List<ExchangeRate> = emptyList(),
    val isLoadingRates: Boolean = false,
    val lastRateUpdate: Long? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class ConverterViewModel @Inject constructor(
    private val unitConverter: UnitConverter,
    private val exchangeRepository: ExchangeRepository,
    private val historyRepository: HistoryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ConverterUiState())
    val state: StateFlow<ConverterUiState> = _state.asStateFlow()

    init {
        selectCategory(UnitCategory.LENGTH)
    }

    fun selectCategory(category: UnitCategory) {
        val units = unitConverter.getUnitsForCategory(category)
        _state.update {
            it.copy(
                selectedCategory = category,
                availableUnits = units,
                fromUnit = units.firstOrNull()?.first ?: "",
                toUnit = units.getOrNull(1)?.first ?: units.firstOrNull()?.first ?: "",
                inputValue = "",
                result = "",
                errorMessage = null
            )
        }
    }

    fun updateInputValue(value: String) {
        _state.update { it.copy(inputValue = value.filter { c -> c.isDigit() || c == '.' || c == '-' }) }
        calculateConversion()
    }

    fun updateFromUnit(unit: String) {
        _state.update { it.copy(fromUnit = unit) }
        calculateConversion()
    }

    fun updateToUnit(unit: String) {
        _state.update { it.copy(toUnit = unit) }
        calculateConversion()
    }

    fun swapUnits() {
        _state.update {
            it.copy(
                fromUnit = it.toUnit,
                toUnit = it.fromUnit
            )
        }
        calculateConversion()
    }

    private fun calculateConversion() {
        val currentState = _state.value
        val inputValue = currentState.inputValue.toDoubleOrNull()

        if (inputValue == null) {
            _state.update { it.copy(result = "", errorMessage = null) }
            return
        }

        try {
            val conversionResult = unitConverter.convert(
                value = inputValue,
                fromUnit = currentState.fromUnit,
                toUnit = currentState.toUnit,
                category = currentState.selectedCategory
            )

            val formattedResult = unitConverter.formatResult(conversionResult.toValue)
            _state.update { it.copy(result = formattedResult, errorMessage = null) }
        } catch (e: Exception) {
            _state.update { it.copy(result = "", errorMessage = e.message ?: "Dönüşüm hatası") }
        }
    }

    fun saveConversion() {
        val currentState = _state.value
        if (currentState.result.isEmpty()) return

        viewModelScope.launch {
            historyRepository.saveHistory(
                CalculationHistory(
                    expression = "${currentState.inputValue} ${currentState.fromUnit}",
                    result = "${currentState.result} ${currentState.toUnit}",
                    type = CalculationType.CONVERTER,
                    subType = currentState.selectedCategory.displayName
                )
            )
        }
    }

    // Currency conversion with API
    fun loadExchangeRates() {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingRates = true) }

            val targetCurrencies = listOf("USD", "EUR", "GBP", "JPY", "CHF", "CAD", "AUD", "TRY")
            exchangeRepository.fetchLatestRates("EUR", targetCurrencies).fold(
                onSuccess = { rates ->
                    _state.update {
                        it.copy(
                            exchangeRates = rates,
                            isLoadingRates = false,
                            lastRateUpdate = System.currentTimeMillis(),
                            errorMessage = null
                        )
                    }
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(
                            isLoadingRates = false,
                            errorMessage = "Döviz kurları alınamadı: ${error.message}"
                        )
                    }
                }
            )
        }
    }

    fun convertCurrency(amount: Double, fromCurrency: String, toCurrency: String) {
        viewModelScope.launch {
            exchangeRepository.convertCurrency(amount, fromCurrency, toCurrency).fold(
                onSuccess = { result ->
                    _state.update {
                        it.copy(
                            result = unitConverter.formatResult(result),
                            errorMessage = null
                        )
                    }
                },
                onFailure = { error ->
                    _state.update { it.copy(errorMessage = error.message) }
                }
            )
        }
    }
}
