package com.melikyldrm.hesap.ui.screens.finance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.melikyldrm.hesap.domain.engine.FinanceCalculator
import com.melikyldrm.hesap.domain.model.*
import com.melikyldrm.hesap.domain.repository.HistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FinanceUiState(
    val selectedTab: FinanceTab = FinanceTab.KDV,
    // KDV
    val kdvAmount: String = "",
    val kdvRate: KdvRate = KdvRate.RATE_18,
    val isKdvIncluded: Boolean = false,
    val kdvResult: KdvResult? = null,
    // Tevkifat
    val tevkifatAmount: String = "",
    val tevkifatKdvRate: KdvRate = KdvRate.RATE_18,
    val tevkifatRate: TevkifatRate = TevkifatRate.RATE_5_10,
    val tevkifatResult: TevkifatResult? = null,
    // Faiz
    val faizPrincipal: String = "",
    val faizRate: String = "",
    val faizTime: String = "",
    val faizFrequency: CompoundFrequency = CompoundFrequency.MONTHLY,
    val isCompoundInterest: Boolean = false,
    val faizResult: FaizResult? = null,
    // Kar/Zarar
    val karCostPrice: String = "",
    val karSellingPrice: String = "",
    val karResult: KarZararResult? = null,
    // Error
    val errorMessage: String? = null
)

enum class FinanceTab(val title: String) {
    KDV("KDV"),
    TEVKIFAT("Tevkifat"),
    FAIZ("Faiz"),
    KAR_ZARAR("Kâr/Zarar")
}

@HiltViewModel
class FinanceViewModel @Inject constructor(
    private val financeCalculator: FinanceCalculator,
    private val historyRepository: HistoryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(FinanceUiState())
    val state: StateFlow<FinanceUiState> = _state.asStateFlow()

    fun selectTab(tab: FinanceTab) {
        _state.value = _state.value.copy(selectedTab = tab, errorMessage = null)
    }

    // ========== KDV ==========
    fun updateKdvAmount(amount: String) {
        _state.value = _state.value.copy(kdvAmount = amount.filter { it.isDigit() || it == '.' })
    }

    fun updateKdvRate(rate: KdvRate) {
        _state.value = _state.value.copy(kdvRate = rate)
    }

    fun toggleKdvIncluded() {
        _state.value = _state.value.copy(isKdvIncluded = !_state.value.isKdvIncluded)
    }

    fun calculateKdv() {
        val amount = _state.value.kdvAmount.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            _state.value = _state.value.copy(errorMessage = "Geçerli bir tutar girin")
            return
        }

        val result = financeCalculator.calculateKdv(
            amount = amount,
            kdvRate = _state.value.kdvRate,
            isKdvIncluded = _state.value.isKdvIncluded
        )

        _state.value = _state.value.copy(kdvResult = result, errorMessage = null)

        // Save to history
        val expression = if (_state.value.isKdvIncluded) {
            "${financeCalculator.formatCurrency(amount)} (KDV Dahil) @ %${(_state.value.kdvRate.rate * 100).toInt()}"
        } else {
            "${financeCalculator.formatCurrency(amount)} + %${(_state.value.kdvRate.rate * 100).toInt()} KDV"
        }
        saveToHistory(expression, financeCalculator.formatCurrency(result.totalAmount), "KDV")
    }

    // ========== TEVKIFAT ==========
    fun updateTevkifatAmount(amount: String) {
        _state.value = _state.value.copy(tevkifatAmount = amount.filter { it.isDigit() || it == '.' })
    }

    fun updateTevkifatKdvRate(rate: KdvRate) {
        _state.value = _state.value.copy(tevkifatKdvRate = rate)
    }

    fun updateTevkifatRate(rate: TevkifatRate) {
        _state.value = _state.value.copy(tevkifatRate = rate)
    }

    fun calculateTevkifat() {
        val amount = _state.value.tevkifatAmount.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            _state.value = _state.value.copy(errorMessage = "Geçerli bir tutar girin")
            return
        }

        val result = financeCalculator.calculateTevkifat(
            baseAmount = amount,
            kdvRate = _state.value.tevkifatKdvRate,
            tevkifatRate = _state.value.tevkifatRate
        )

        _state.value = _state.value.copy(tevkifatResult = result, errorMessage = null)

        val expression = "${financeCalculator.formatCurrency(amount)} @ %${(_state.value.tevkifatKdvRate.rate * 100).toInt()} KDV, ${_state.value.tevkifatRate.displayName} Tevkifat"
        saveToHistory(expression, financeCalculator.formatCurrency(result.totalAmount), "TEVKIFAT")
    }

    // ========== FAİZ ==========
    fun updateFaizPrincipal(value: String) {
        _state.value = _state.value.copy(faizPrincipal = value.filter { it.isDigit() || it == '.' })
    }

    fun updateFaizRate(value: String) {
        _state.value = _state.value.copy(faizRate = value.filter { it.isDigit() || it == '.' })
    }

    fun updateFaizTime(value: String) {
        _state.value = _state.value.copy(faizTime = value.filter { it.isDigit() || it == '.' })
    }

    fun updateFaizFrequency(frequency: CompoundFrequency) {
        _state.value = _state.value.copy(faizFrequency = frequency)
    }

    fun toggleCompoundInterest() {
        _state.value = _state.value.copy(isCompoundInterest = !_state.value.isCompoundInterest)
    }

    fun calculateFaiz() {
        val principal = _state.value.faizPrincipal.toDoubleOrNull()
        val rate = _state.value.faizRate.toDoubleOrNull()
        val time = _state.value.faizTime.toDoubleOrNull()

        if (principal == null || rate == null || time == null ||
            principal <= 0 || rate < 0 || time <= 0) {
            _state.value = _state.value.copy(errorMessage = "Geçerli değerler girin")
            return
        }

        val result = if (_state.value.isCompoundInterest) {
            financeCalculator.calculateCompoundInterest(
                principal = principal,
                annualRate = rate,
                timeInYears = time,
                frequency = _state.value.faizFrequency
            )
        } else {
            financeCalculator.calculateSimpleInterest(
                principal = principal,
                annualRate = rate,
                timeInYears = time
            )
        }

        _state.value = _state.value.copy(faizResult = result, errorMessage = null)

        val type = if (_state.value.isCompoundInterest) "Bileşik" else "Basit"
        val expression = "${financeCalculator.formatCurrency(principal)} @ %$rate Faiz, $time Yıl ($type)"
        saveToHistory(expression, financeCalculator.formatCurrency(result.totalAmount), "FAIZ")
    }

    // ========== KÂR/ZARAR ==========
    fun updateKarCostPrice(value: String) {
        _state.value = _state.value.copy(karCostPrice = value.filter { it.isDigit() || it == '.' })
    }

    fun updateKarSellingPrice(value: String) {
        _state.value = _state.value.copy(karSellingPrice = value.filter { it.isDigit() || it == '.' })
    }

    fun calculateKarZarar() {
        val cost = _state.value.karCostPrice.toDoubleOrNull()
        val selling = _state.value.karSellingPrice.toDoubleOrNull()

        if (cost == null || selling == null || cost < 0 || selling < 0) {
            _state.value = _state.value.copy(errorMessage = "Geçerli fiyatlar girin")
            return
        }

        val result = financeCalculator.calculateProfitLoss(cost, selling)
        _state.value = _state.value.copy(karResult = result, errorMessage = null)

        val type = if (result.isProfit) "Kâr" else "Zarar"
        val expression = "Maliyet: ${financeCalculator.formatCurrency(cost)}, Satış: ${financeCalculator.formatCurrency(selling)}"
        saveToHistory(expression, "$type: ${financeCalculator.formatPercentage(result.percentage)}", "KAR_ZARAR")
    }

    fun clearResults() {
        _state.value = _state.value.copy(
            kdvResult = null,
            tevkifatResult = null,
            faizResult = null,
            karResult = null,
            errorMessage = null
        )
    }

    private fun saveToHistory(expression: String, result: String, subType: String) {
        viewModelScope.launch {
            historyRepository.saveHistory(
                CalculationHistory(
                    expression = expression,
                    result = result,
                    type = CalculationType.FINANCE,
                    subType = subType
                )
            )
        }
    }
}

