package com.melikyldrm.hesap.domain.model

data class CalculatorState(
    val expression: String = "",
    val result: String = "0",
    val previousExpression: String = "",
    val isError: Boolean = false,
    val errorMessage: String? = null,
    val isResultDisplayed: Boolean = false,
    val memoryValue: Double = 0.0,
    val hasMemory: Boolean = false
)

enum class CalculationType {
    BASIC,
    SCIENTIFIC,
    FINANCE,
    CONVERTER
}

enum class FinanceSubType {
    KDV,
    TEVKIFAT,
    FAIZ,
    KAR_ZARAR,
    VADE
}

enum class CalculatorOperation(val symbol: String, val displaySymbol: String) {
    ADD("+", "+"),
    SUBTRACT("-", "−"),
    MULTIPLY("*", "×"),
    DIVIDE("/", "÷"),
    MODULO("%", "%"),
    POWER("^", "^"),
    NONE("", "")
}

