package com.melikyldrm.hesap.speech

sealed class SpeechState {
    data object Idle : SpeechState()
    data object Listening : SpeechState()
    data object Processing : SpeechState()
    data class Success(val text: String, val parsedExpression: String?) : SpeechState()
    data class Error(val message: String) : SpeechState()
    data object PermissionRequired : SpeechState()
    data object NotAvailable : SpeechState()
}

sealed class SpeechCommand {
    data class Calculate(val expression: String) : SpeechCommand()
    data class KdvCalculate(val amount: Double, val rate: Int, val isIncluded: Boolean) : SpeechCommand()
    data class TevkifatCalculate(val amount: Double, val kdvRate: Int, val tevkifatRate: String) : SpeechCommand()
    data class Convert(val value: Double, val fromUnit: String, val toUnit: String) : SpeechCommand()
    data object Clear : SpeechCommand()
    data object Delete : SpeechCommand()
    data object Equals : SpeechCommand()
    data class Unknown(val originalText: String) : SpeechCommand()
}

