package com.melikyldrm.hesap.ui.screens.basic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.melikyldrm.hesap.domain.engine.CalculatorEngine
import com.melikyldrm.hesap.domain.model.CalculationHistory
import com.melikyldrm.hesap.domain.model.CalculationType
import com.melikyldrm.hesap.domain.model.CalculatorState
import com.melikyldrm.hesap.domain.repository.HistoryRepository
import com.melikyldrm.hesap.speech.SpeechCommand
import com.melikyldrm.hesap.speech.SpeechRecognitionManager
import com.melikyldrm.hesap.speech.SpeechState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class BasicCalculatorViewModel @Inject constructor(
    private val calculatorEngine: CalculatorEngine,
    private val historyRepository: HistoryRepository,
    private val speechRecognitionManager: SpeechRecognitionManager
) : ViewModel() {

    private val _state = MutableStateFlow(CalculatorState())
    val state: StateFlow<CalculatorState> = _state.asStateFlow()

    val speechState: StateFlow<SpeechState> = speechRecognitionManager.speechState

    init {
        // Speech command listener - her zaman başlat
        viewModelScope.launch {
            speechRecognitionManager.lastCommand.collect { command ->
                Timber.d("Received command from flow: %s", command)
                handleSpeechCommand(command)
            }
        }
    }

    fun startListening() {
        speechRecognitionManager.startListening()
    }

    fun onNumberClick(number: String) {
        Timber.d("onNumberClick: number='%s'", number)

        _state.update { currentState ->
            if (currentState.isResultDisplayed) {
                Timber.d("onNumberClick: Starting new expression with '%s'", number)
                currentState.copy(
                    expression = number,
                    previousExpression = "${currentState.expression} = ${currentState.result}",
                    isResultDisplayed = false,
                    isError = false
                )
            } else {
                val newExpression = if (currentState.expression == "0") {
                    number
                } else {
                    currentState.expression + number
                }
                Timber.d("onNumberClick: Appending, new expression='%s'", newExpression)
                currentState.copy(
                    expression = newExpression,
                    isError = false
                )
            }
        }

        // Calculate preview result
        calculatePreview()
    }

    fun onOperatorClick(operator: String) {
        _state.update { currentState ->
            Timber.d("onOperatorClick: operator='%s', isResultDisplayed=%s", operator, currentState.isResultDisplayed)

            val newExpression = if (currentState.isResultDisplayed) {
                currentState.result + operator
            } else if (currentState.expression.isNotEmpty()) {
                val lastChar = currentState.expression.lastOrNull()
                if (lastChar != null && lastChar in "+-×÷") {
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

    fun onDecimalClick() {
        _state.update { currentState ->
            val lastNumber = currentState.expression.split(Regex("[+\\-×÷]")).lastOrNull() ?: ""

            if (!lastNumber.contains(".")) {
                val newExpression = if (currentState.isResultDisplayed) {
                    "0."
                } else if (currentState.expression.isEmpty() || currentState.expression.last() in "+-×÷") {
                    currentState.expression + "0."
                } else {
                    currentState.expression + "."
                }

                currentState.copy(
                    expression = newExpression,
                    isResultDisplayed = false,
                    isError = false
                )
            } else {
                currentState // no change
            }
        }
    }

    fun onEqualsClick() {
        val currentExpression = _state.value.expression
        if (currentExpression.isEmpty()) return

        val calcExpression = currentExpression
            .replace("×", "*")
            .replace("÷", "/")

        calculatorEngine.evaluate(calcExpression).fold(
            onSuccess = { result ->
                val formattedResult = calculatorEngine.formatResult(result)
                Timber.d("onEqualsClick: Success, formattedResult='%s'", formattedResult)

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
                Timber.e("onEqualsClick: Failed, error=%s", error.message)
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

    fun onPercentClick() {
        val currentExpression = _state.value.expression
        if (currentExpression.isEmpty()) return

        val expression = currentExpression
            .replace("×", "*")
            .replace("÷", "/")

        calculatorEngine.evaluate("($expression)/100").fold(
            onSuccess = { result ->
                val formattedResult = calculatorEngine.formatResult(result)
                _state.update { currentState ->
                    currentState.copy(
                        expression = formattedResult,
                        result = formattedResult,
                        isResultDisplayed = true
                    )
                }
            },
            onFailure = { /* Keep current state */ }
        )
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

    fun onMemoryStore() {
        _state.update { currentState ->
            val valueToStore = if (currentState.isResultDisplayed) {
                currentState.result.toDoubleOrNull() ?: 0.0
            } else {
                val calcExpression = currentState.expression.replace("×", "*").replace("÷", "/")
                calculatorEngine.evaluate(calcExpression).getOrNull() ?: 0.0
            }

            currentState.copy(
                memoryValue = valueToStore,
                hasMemory = true
            )
        }
    }

    fun onMemoryRecall() {
        _state.update { currentState ->
            if (currentState.hasMemory) {
                val memoryString = calculatorEngine.formatResult(currentState.memoryValue)
                currentState.copy(
                    expression = if (currentState.isResultDisplayed) {
                        memoryString
                    } else {
                        currentState.expression + memoryString
                    },
                    isResultDisplayed = false
                )
            } else {
                currentState
            }
        }
    }

    fun onMemoryClear() {
        _state.update { it.copy(memoryValue = 0.0, hasMemory = false) }
    }

    fun onMemoryAdd() {
        _state.update { currentState ->
            val valueToAdd = currentState.result.toDoubleOrNull()
            if (valueToAdd != null) {
                currentState.copy(
                    memoryValue = currentState.memoryValue + valueToAdd,
                    hasMemory = true
                )
            } else {
                currentState
            }
        }
    }

    fun onMemorySubtract() {
        _state.update { currentState ->
            val valueToSubtract = currentState.result.toDoubleOrNull()
            if (valueToSubtract != null) {
                currentState.copy(
                    memoryValue = currentState.memoryValue - valueToSubtract,
                    hasMemory = true
                )
            } else {
                currentState
            }
        }
    }

    // Speech recognition functions - startListening is defined above with lazy init

    fun stopListening() {
        speechRecognitionManager.stopListening()
    }

    fun resetSpeechState() {
        speechRecognitionManager.resetState()
    }

    private fun handleSpeechCommand(command: SpeechCommand) {
        Timber.d("handleSpeechCommand: %s", command)
        when (command) {
            is SpeechCommand.Calculate -> {
                Timber.d("Processing Calculate: %s", command.expression)
                val calcExpression = command.expression

                calculatorEngine.evaluate(calcExpression).fold(
                    onSuccess = { result ->
                        val formattedResult = calculatorEngine.formatResult(result)
                        val displayExpression = command.expression.replace("*", "×").replace("/", "÷")

                        Timber.d("Calculation success - result: %s", formattedResult)

                        _state.update {
                            it.copy(
                                expression = displayExpression,
                                result = formattedResult,
                                previousExpression = displayExpression,
                                isResultDisplayed = true,
                                isError = false
                            )
                        }

                        saveToHistory(displayExpression, formattedResult)
                    },
                    onFailure = { error ->
                        Timber.e("Calculation failed: %s", error.message)
                        val displayExpression = command.expression.replace("*", "×").replace("/", "÷")
                        _state.update {
                            it.copy(
                                expression = displayExpression,
                                result = "Hata",
                                isError = true,
                                errorMessage = error.message
                            )
                        }
                    }
                )
            }
            is SpeechCommand.Clear -> onClearClick()
            is SpeechCommand.Delete -> onDeleteClick()
            is SpeechCommand.Equals -> onEqualsClick()
            is SpeechCommand.ContinueCalculation -> {
                Timber.d("Processing ContinueCalculation: %s", command.operatorAndValue)

                val currentResult = _state.value.let { currentState ->
                    if (currentState.isResultDisplayed || currentState.result != "0") {
                        currentState.result
                    } else {
                        val calcExpr = currentState.expression.replace("×", "*").replace("÷", "/")
                        calculatorEngine.evaluate(calcExpr).getOrNull()?.let {
                            calculatorEngine.formatResult(it)
                        } ?: "0"
                    }
                }

                val fullExpression = currentResult + command.operatorAndValue

                calculatorEngine.evaluate(fullExpression).fold(
                    onSuccess = { result ->
                        val formattedResult = calculatorEngine.formatResult(result)
                        val displayExpression = fullExpression.replace("*", "×").replace("/", "÷")

                        _state.update {
                            it.copy(
                                expression = displayExpression,
                                result = formattedResult,
                                previousExpression = displayExpression,
                                isResultDisplayed = true,
                                isError = false
                            )
                        }

                        saveToHistory(displayExpression, formattedResult)
                    },
                    onFailure = { error ->
                        Timber.e("Continue calculation failed: %s", error.message)
                        _state.update {
                            it.copy(
                                result = "Hata",
                                isError = true,
                                errorMessage = error.message
                            )
                        }
                    }
                )
            }
            else -> { /* Handle other commands in other ViewModels */ }
        }
    }

    private fun calculatePreview() {
        val currentState = _state.value

        if (currentState.expression.isNotEmpty() &&
            calculatorEngine.isExpressionComplete(currentState.expression)) {

            val calcExpression = currentState.expression
                .replace("×", "*")
                .replace("÷", "/")

            calculatorEngine.evaluate(calcExpression).fold(
                onSuccess = { result ->
                    _state.update {
                        it.copy(
                            result = calculatorEngine.formatResult(result),
                            isError = false
                        )
                    }
                },
                onFailure = { /* Keep previous result */ }
            )
        }
    }

    private fun saveToHistory(expression: String, result: String) {
        viewModelScope.launch {
            historyRepository.saveHistory(
                CalculationHistory(
                    expression = expression,
                    result = result,
                    type = CalculationType.BASIC
                )
            )
        }
    }

    fun loadExpression(expression: String) {
        _state.update {
            it.copy(
                expression = expression,
                isResultDisplayed = false
            )
        }
        calculatePreview()
    }

    override fun onCleared() {
        super.onCleared()
        speechRecognitionManager.destroy()
    }
}
