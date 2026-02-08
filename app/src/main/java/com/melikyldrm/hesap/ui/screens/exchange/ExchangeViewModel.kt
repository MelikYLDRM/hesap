package com.melikyldrm.hesap.ui.screens.exchange

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.melikyldrm.hesap.domain.model.Currency
import com.melikyldrm.hesap.domain.model.ExchangeRate
import com.melikyldrm.hesap.domain.repository.ExchangeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExchangeUiState(
    val amount: String = "1",
    val fromCurrency: Currency = Currency.USD,
    val toCurrency: Currency = Currency.TRY,
    val result: String = "",
    val exchangeRate: Double? = null,
    val popularRates: List<ExchangeRate> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val lastUpdateTime: Long? = null
)

@HiltViewModel
class ExchangeViewModel @Inject constructor(
    private val exchangeRepository: ExchangeRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ExchangeUiState())
    val state: StateFlow<ExchangeUiState> = _state.asStateFlow()

    private val popularCurrencies = listOf("USD", "EUR", "GBP", "CHF", "JPY", "SAR", "AUD", "CAD")

    init {
        loadRates()
        observeRates()
    }

    private fun observeRates() {
        viewModelScope.launch {
            exchangeRepository.getAllCachedRates().collect { rates ->
                val popular = rates
                    .filter { it.baseCurrency in popularCurrencies && it.targetCurrency == "TRY" }
                    .sortedBy { popularCurrencies.indexOf(it.baseCurrency) }
                _state.update { it.copy(popularRates = popular) }
            }
        }
    }

    fun loadRates() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            exchangeRepository.fetchLatestRates("TRY", popularCurrencies)
                .onSuccess { _ ->
                    val lastUpdate = exchangeRepository.getLastUpdateTime()
                    _state.update {
                        it.copy(
                            isLoading = false,
                            lastUpdateTime = lastUpdate
                        )
                    }
                    calculateResult()
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Kurlar yüklenemedi: ${error.message}"
                        )
                    }
                }
        }
    }

    fun refreshRates() {
        loadRates()
    }

    fun updateAmount(amount: String) {
        // Sadece sayı ve nokta/virgül kabul et
        val cleanAmount = amount.replace(",", ".")
        if (cleanAmount.isEmpty() || cleanAmount.matches(Regex("^\\d*\\.?\\d*$"))) {
            _state.update { it.copy(amount = cleanAmount) }
            calculateResult()
        }
    }

    fun updateFromCurrency(currency: Currency) {
        _state.update { it.copy(fromCurrency = currency) }
        calculateResult()
    }

    fun updateToCurrency(currency: Currency) {
        _state.update { it.copy(toCurrency = currency) }
        calculateResult()
    }

    fun swapCurrencies() {
        _state.update {
            it.copy(
                fromCurrency = it.toCurrency,
                toCurrency = it.fromCurrency
            )
        }
        calculateResult()
    }

    fun selectCurrencyPair(from: String, to: String) {
        val fromCurrency = Currency.entries.find { it.code == from } ?: return
        val toCurrency = Currency.entries.find { it.code == to } ?: return
        _state.update {
            it.copy(
                fromCurrency = fromCurrency,
                toCurrency = toCurrency
            )
        }
        calculateResult()
    }

    private fun calculateResult() {
        val amount = _state.value.amount.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            _state.update { it.copy(result = "", exchangeRate = null) }
            return
        }

        val from = _state.value.fromCurrency.code
        val to = _state.value.toCurrency.code

        viewModelScope.launch {
            exchangeRepository.convertCurrency(amount, from, to)
                .onSuccess { result ->
                    val rate = if (amount != 0.0) result / amount else null
                    _state.update {
                        it.copy(
                            result = formatResult(result),
                            exchangeRate = rate,
                            errorMessage = null
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            result = "",
                            exchangeRate = null,
                            errorMessage = "Dönüştürme hatası: ${error.message}"
                        )
                    }
                }
        }
    }

    private fun formatResult(value: Double): String {
        return when {
            value >= 1_000_000 -> String.format("%,.0f", value)
            value >= 1000 -> String.format("%,.2f", value)
            value >= 1 -> String.format("%.4f", value)
            value >= 0.01 -> String.format("%.6f", value)
            else -> String.format("%.8f", value)
        }
    }
}

