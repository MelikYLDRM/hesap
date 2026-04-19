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
    // Kredi
    val krediAmount: String = "",
    val krediRate: String = "",
    val krediMonths: String = "",
    val krediResult: KrediResult? = null,
    // BMI
    val bmiWeight: String = "",
    val bmiHeight: String = "",
    val bmiResult: BmiResult? = null,
    // Bahşiş
    val tipBill: String = "",
    val tipPercentage: String = "15",
    val tipPeople: String = "1",
    val tipResult: TipResult? = null,
    // Error
    val errorMessage: String? = null
)

enum class FinanceTab(val title: String) {
    KDV("KDV"),
    TEVKIFAT("Tevkifat"),
    FAIZ("Faiz"),
    KAR_ZARAR("Kâr/Zarar"),
    KREDI("Kredi"),
    BMI("BMI"),
    BAHSIS("Bahşiş")
}

@HiltViewModel
class FinanceViewModel @Inject constructor(
    private val financeCalculator: FinanceCalculator,
    private val historyRepository: HistoryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(FinanceUiState())
    val state: StateFlow<FinanceUiState> = _state.asStateFlow()

    fun selectTab(tab: FinanceTab) {
        _state.update { it.copy(selectedTab = tab, errorMessage = null) }
    }

    // ========== KDV ==========
    fun updateKdvAmount(amount: String) {
        _state.update { it.copy(kdvAmount = amount.filter { c -> c.isDigit() || c == '.' }) }
    }

    fun updateKdvRate(rate: KdvRate) {
        _state.update { it.copy(kdvRate = rate) }
    }

    fun toggleKdvIncluded() {
        _state.update { it.copy(isKdvIncluded = !it.isKdvIncluded) }
    }

    fun calculateKdv() {
        val currentState = _state.value
        val amount = currentState.kdvAmount.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            _state.update { it.copy(errorMessage = "Gecerli bir tutar girin") }
            return
        }

        val result = financeCalculator.calculateKdv(
            amount = amount,
            kdvRate = currentState.kdvRate,
            isKdvIncluded = currentState.isKdvIncluded
        )

        _state.update { it.copy(kdvResult = result, errorMessage = null) }

        val expression = if (currentState.isKdvIncluded) {
            "${financeCalculator.formatCurrency(amount)} (KDV Dahil) @ %${(currentState.kdvRate.rate * 100).toInt()}"
        } else {
            "${financeCalculator.formatCurrency(amount)} + %${(currentState.kdvRate.rate * 100).toInt()} KDV"
        }
        saveToHistory(expression, financeCalculator.formatCurrency(result.totalAmount), "KDV")
    }

    // ========== TEVKIFAT ==========
    fun updateTevkifatAmount(amount: String) {
        _state.update { it.copy(tevkifatAmount = amount.filter { c -> c.isDigit() || c == '.' }) }
    }

    fun updateTevkifatKdvRate(rate: KdvRate) {
        _state.update { it.copy(tevkifatKdvRate = rate) }
    }

    fun updateTevkifatRate(rate: TevkifatRate) {
        _state.update { it.copy(tevkifatRate = rate) }
    }

    fun calculateTevkifat() {
        val currentState = _state.value
        val amount = currentState.tevkifatAmount.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            _state.update { it.copy(errorMessage = "Gecerli bir tutar girin") }
            return
        }

        val result = financeCalculator.calculateTevkifat(
            baseAmount = amount,
            kdvRate = currentState.tevkifatKdvRate,
            tevkifatRate = currentState.tevkifatRate
        )

        _state.update { it.copy(tevkifatResult = result, errorMessage = null) }

        val expression = "${financeCalculator.formatCurrency(amount)} @ %${(currentState.tevkifatKdvRate.rate * 100).toInt()} KDV, ${currentState.tevkifatRate.displayName} Tevkifat"
        saveToHistory(expression, financeCalculator.formatCurrency(result.totalAmount), "TEVKIFAT")
    }

    // ========== FAİZ ==========
    fun updateFaizPrincipal(value: String) {
        _state.update { it.copy(faizPrincipal = value.filter { c -> c.isDigit() || c == '.' }) }
    }

    fun updateFaizRate(value: String) {
        _state.update { it.copy(faizRate = value.filter { c -> c.isDigit() || c == '.' }) }
    }

    fun updateFaizTime(value: String) {
        _state.update { it.copy(faizTime = value.filter { c -> c.isDigit() || c == '.' }) }
    }

    fun updateFaizFrequency(frequency: CompoundFrequency) {
        _state.update { it.copy(faizFrequency = frequency) }
    }

    fun toggleCompoundInterest() {
        _state.update { it.copy(isCompoundInterest = !it.isCompoundInterest) }
    }

    fun calculateFaiz() {
        val currentState = _state.value
        val principal = currentState.faizPrincipal.toDoubleOrNull()
        val rate = currentState.faizRate.toDoubleOrNull()
        val time = currentState.faizTime.toDoubleOrNull()

        if (principal == null || rate == null || time == null ||
            principal <= 0 || rate < 0 || time <= 0) {
            _state.update { it.copy(errorMessage = "Gecerli degerler girin") }
            return
        }

        val result = if (currentState.isCompoundInterest) {
            financeCalculator.calculateCompoundInterest(
                principal = principal,
                annualRate = rate,
                timeInYears = time,
                frequency = currentState.faizFrequency
            )
        } else {
            financeCalculator.calculateSimpleInterest(
                principal = principal,
                annualRate = rate,
                timeInYears = time
            )
        }

        _state.update { it.copy(faizResult = result, errorMessage = null) }

        val type = if (currentState.isCompoundInterest) "Bilesik" else "Basit"
        val expression = "${financeCalculator.formatCurrency(principal)} @ %$rate Faiz, $time Yil ($type)"
        saveToHistory(expression, financeCalculator.formatCurrency(result.totalAmount), "FAIZ")
    }

    // ========== KAR/ZARAR ==========
    fun updateKarCostPrice(value: String) {
        _state.update { it.copy(karCostPrice = value.filter { c -> c.isDigit() || c == '.' }) }
    }

    fun updateKarSellingPrice(value: String) {
        _state.update { it.copy(karSellingPrice = value.filter { c -> c.isDigit() || c == '.' }) }
    }

    fun calculateKarZarar() {
        val currentState = _state.value
        val cost = currentState.karCostPrice.toDoubleOrNull()
        val selling = currentState.karSellingPrice.toDoubleOrNull()

        if (cost == null || selling == null || cost < 0 || selling < 0) {
            _state.update { it.copy(errorMessage = "Gecerli fiyatlar girin") }
            return
        }

        val result = financeCalculator.calculateProfitLoss(cost, selling)
        _state.update { it.copy(karResult = result, errorMessage = null) }

        val type = if (result.isProfit) "Kar" else "Zarar"
        val expression = "Maliyet: ${financeCalculator.formatCurrency(cost)}, Satis: ${financeCalculator.formatCurrency(selling)}"
        saveToHistory(expression, "$type: ${financeCalculator.formatPercentage(result.percentage)}", "KAR_ZARAR")
    }

    fun clearResults() {
        _state.update {
            it.copy(
                kdvResult = null,
                tevkifatResult = null,
                faizResult = null,
                karResult = null,
                krediResult = null,
                bmiResult = null,
                tipResult = null,
                errorMessage = null
            )
        }
    }

    // ========== KREDİ ==========
    fun updateKrediAmount(value: String) {
        _state.update { it.copy(krediAmount = value.filter { c -> c.isDigit() || c == '.' }) }
    }
    fun updateKrediRate(value: String) {
        _state.update { it.copy(krediRate = value.filter { c -> c.isDigit() || c == '.' }) }
    }
    fun updateKrediMonths(value: String) {
        _state.update { it.copy(krediMonths = value.filter { c -> c.isDigit() }) }
    }
    fun calculateKredi() {
        val s = _state.value
        val amount = s.krediAmount.toDoubleOrNull()
        val rate = s.krediRate.toDoubleOrNull()
        val months = s.krediMonths.toIntOrNull()
        if (amount == null || rate == null || months == null || amount <= 0 || rate < 0 || months <= 0) {
            _state.update { it.copy(errorMessage = "Geçerli değerler girin") }
            return
        }
        val result = financeCalculator.calculateLoan(amount, rate, months)
        _state.update { it.copy(krediResult = result, errorMessage = null) }
        saveToHistory(
            "${financeCalculator.formatCurrency(amount)} @ %$rate Yıllık, $months Ay",
            "Taksit: ${financeCalculator.formatCurrency(result.monthlyPayment)}",
            "KREDI"
        )
    }

    // ========== BMI ==========
    fun updateBmiWeight(value: String) {
        _state.update { it.copy(bmiWeight = value.filter { c -> c.isDigit() || c == '.' }) }
    }
    fun updateBmiHeight(value: String) {
        _state.update { it.copy(bmiHeight = value.filter { c -> c.isDigit() || c == '.' }) }
    }
    fun calculateBmi() {
        val weight = _state.value.bmiWeight.toDoubleOrNull()
        val height = _state.value.bmiHeight.toDoubleOrNull()
        if (weight == null || height == null || weight <= 0 || height <= 0) {
            _state.update { it.copy(errorMessage = "Geçerli değerler girin") }
            return
        }
        val result = financeCalculator.calculateBmi(weight, height)
        _state.update { it.copy(bmiResult = result, errorMessage = null) }
        saveToHistory("${weight}kg, ${height}cm", "BMI: ${String.format("%.1f", result.bmi)} (${result.category})", "BMI")
    }

    // ========== BAHŞİŞ ==========
    fun updateTipBill(value: String) {
        _state.update { it.copy(tipBill = value.filter { c -> c.isDigit() || c == '.' }) }
    }
    fun updateTipPercentage(value: String) {
        _state.update { it.copy(tipPercentage = value.filter { c -> c.isDigit() || c == '.' }) }
    }
    fun updateTipPeople(value: String) {
        _state.update { it.copy(tipPeople = value.filter { c -> c.isDigit() }) }
    }
    fun calculateTip() {
        val bill = _state.value.tipBill.toDoubleOrNull()
        val pct = _state.value.tipPercentage.toDoubleOrNull()
        val people = _state.value.tipPeople.toIntOrNull()
        if (bill == null || pct == null || people == null || bill <= 0 || pct < 0 || people <= 0) {
            _state.update { it.copy(errorMessage = "Geçerli değerler girin") }
            return
        }
        val result = financeCalculator.calculateTip(bill, pct, people)
        _state.update { it.copy(tipResult = result, errorMessage = null) }
        saveToHistory(
            "${financeCalculator.formatCurrency(bill)} @ %$pct, $people kişi",
            "Kişi başı: ${financeCalculator.formatCurrency(result.perPerson)}",
            "BAHSIS"
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
