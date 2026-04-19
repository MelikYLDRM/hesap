package com.melikyldrm.hesap.ui.screens.scientific

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.melikyldrm.hesap.domain.engine.CalculatorEngine
import com.melikyldrm.hesap.domain.model.CalculationHistory
import com.melikyldrm.hesap.domain.model.CalculationType
import com.melikyldrm.hesap.domain.model.CalculatorState
import com.melikyldrm.hesap.domain.repository.HistoryRepository
import com.melikyldrm.hesap.speech.SpeechRecognitionManager
import com.melikyldrm.hesap.speech.SpeechState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScientificViewModel @Inject constructor(
    private val calculatorEngine: CalculatorEngine,
    private val historyRepository: HistoryRepository,
    private val speechRecognitionManager: SpeechRecognitionManager
) : ViewModel() {

    private val _state = MutableStateFlow(CalculatorState())
    val state: StateFlow<CalculatorState> = _state.asStateFlow()

    private val _isRadianMode = MutableStateFlow(true)
    val isRadianMode: StateFlow<Boolean> = _isRadianMode.asStateFlow()

    private val _isSecondFunction = MutableStateFlow(false)
    val isSecondFunction: StateFlow<Boolean> = _isSecondFunction.asStateFlow()

    val speechState: StateFlow<SpeechState> = speechRecognitionManager.speechState

    fun onNumberClick(number: String) {
        _state.update { currentState ->
            if (currentState.isResultDisplayed) {
                currentState.copy(
                    expression = number,
                    previousExpression = "${currentState.expression} = ${currentState.result}",
                    isResultDisplayed = false,
                    isError = false
                )
            } else {
                val newExpression = if (currentState.expression == "0") number
                                   else currentState.expression + number
                currentState.copy(expression = newExpression, isError = false)
            }
        }
        calculatePreview()
    }

    fun onOperatorClick(operator: String) {
        _state.update { currentState ->
            val newExpression = if (currentState.isResultDisplayed) {
                currentState.result + operator
            } else if (currentState.expression.isNotEmpty()) {
                val lastChar = currentState.expression.lastOrNull()
                if (lastChar != null && lastChar in "+-×÷^") {
                    currentState.expression.dropLast(1) + operator
                } else {
                    currentState.expression + operator
                }
            } else {
                "0$operator"
            }

            currentState.copy(
                expression = newExpression,
                isResultDisplayed = false,
                isError = false
            )
        }
    }

    fun onFunctionClick(function: String) {
        val isSecond = _isSecondFunction.value

        // Map function based on mode
        val actualFunction = when (function) {
            "sin" -> if (isSecond) "asin" else if (_isRadianMode.value) "sin" else "sind"
            "cos" -> if (isSecond) "acos" else if (_isRadianMode.value) "cos" else "cosd"
            "tan" -> if (isSecond) "atan" else if (_isRadianMode.value) "tan" else "tand"
            "ln" -> if (isSecond) "exp" else "ln"
            "log" -> if (isSecond) "10^" else "log10"
            "√" -> if (isSecond) "cbrt" else "sqrt"
            "x²" -> if (isSecond) "x³" else "^2"
            else -> function
        }

        _state.update { currentState ->
            val newExpression = when (actualFunction) {
                "^2", "^3", "!" -> {
                    if (currentState.isResultDisplayed) {
                        currentState.result + actualFunction.replace("^", "")
                    } else {
                        currentState.expression + actualFunction.replace("^", "")
                    }
                }
                "10^" -> {
                    if (currentState.isResultDisplayed) {
                        "10^${currentState.result}"
                    } else if (currentState.expression.isEmpty()) {
                        "10^"
                    } else {
                        currentState.expression + "×10^"
                    }
                }
                else -> {
                    if (currentState.isResultDisplayed) {
                        "$actualFunction(${currentState.result})"
                    } else {
                        currentState.expression + "$actualFunction("
                    }
                }
            }

            currentState.copy(
                expression = newExpression,
                isResultDisplayed = false
            )
        }

        // Reset second function mode after use
        _isSecondFunction.update { false }
    }

    fun onConstantClick(constant: String) {
        val value = when (constant) {
            "π" -> "3.14159265359"
            "e" -> "2.71828182846"
            else -> constant
        }

        _state.update { currentState ->
            val newExpression = if (currentState.isResultDisplayed) {
                value
            } else if (currentState.expression.isEmpty() ||
                       currentState.expression.last() in "+-×÷^(") {
                currentState.expression + value
            } else {
                currentState.expression + "×$value"
            }

            currentState.copy(
                expression = newExpression,
                isResultDisplayed = false
            )
        }
    }

    fun onParenthesisClick(paren: String) {
        _state.update { currentState ->
            val newExpression = if (currentState.isResultDisplayed) {
                paren
            } else {
                currentState.expression + paren
            }

            currentState.copy(
                expression = newExpression,
                isResultDisplayed = false
            )
        }
    }

    fun toggleRadianMode() {
        _isRadianMode.update { !it }
    }

    fun toggleSecondFunction() {
        _isSecondFunction.update { !it }
    }

    fun onDecimalClick() {
        _state.update { currentState ->
            val lastNumber = currentState.expression.split(Regex("[+\\-×÷^(]")).lastOrNull() ?: ""

            if (!lastNumber.contains(".")) {
                val newExpression = if (currentState.isResultDisplayed) {
                    "0."
                } else if (currentState.expression.isEmpty() ||
                           currentState.expression.last() in "+-×÷^(") {
                    currentState.expression + "0."
                } else {
                    currentState.expression + "."
                }

                currentState.copy(
                    expression = newExpression,
                    isResultDisplayed = false
                )
            } else {
                currentState
            }
        }
    }

    fun onEqualsClick() {
        val currentExpression = _state.value.expression
        if (currentExpression.isEmpty()) return

        val calcExpression = currentExpression
            .replace("×", "*")
            .replace("÷", "/")
            .replace("²", "^2")
            .replace("³", "^3")
            .replace("√", "sqrt")

        calculatorEngine.evaluate(calcExpression).fold(
            onSuccess = { result ->
                val formattedResult = calculatorEngine.formatResult(result)
                _state.update { currentState ->
                    currentState.copy(
                        result = formattedResult,
                        previousExpression = currentState.expression,
                        isResultDisplayed = true,
                        isError = false
                    )
                }
                saveToHistory(currentExpression, formattedResult)
            },
            onFailure = { error ->
                _state.update { currentState ->
                    currentState.copy(
                        result = "Hata",
                        isError = true,
                        errorMessage = error.message
                    )
                }
            }
        )
    }

    fun onClearClick() {
        _state.update { CalculatorState() }
    }

    fun onDeleteClick() {
        _state.update { currentState ->
            if (currentState.isResultDisplayed) {
                CalculatorState()
            } else if (currentState.expression.isNotEmpty()) {
                currentState.copy(
                    expression = currentState.expression.dropLast(1),
                    isError = false
                )
            } else {
                currentState
            }
        }
        calculatePreview()
    }

    private fun calculatePreview() {
        val currentState = _state.value
        if (currentState.expression.isNotEmpty() &&
            calculatorEngine.isExpressionComplete(currentState.expression)) {

            val calcExpression = currentState.expression
                .replace("×", "*")
                .replace("÷", "/")
                .replace("²", "^2")
                .replace("³", "^3")

            calculatorEngine.evaluate(calcExpression).fold(
                onSuccess = { result ->
                    _state.update {
                        it.copy(
                            result = calculatorEngine.formatResult(result),
                            isError = false
                        )
                    }
                },
                onFailure = { }
            )
        }
    }

    private fun saveToHistory(expression: String, result: String) {
        viewModelScope.launch {
            historyRepository.saveHistory(
                CalculationHistory(
                    expression = expression,
                    result = result,
                    type = CalculationType.SCIENTIFIC
                )
            )
        }
    }

    // Speech functions
    fun startListening() = speechRecognitionManager.startListening()
    fun stopListening() = speechRecognitionManager.stopListening()
    fun resetSpeechState() = speechRecognitionManager.resetState()
}
