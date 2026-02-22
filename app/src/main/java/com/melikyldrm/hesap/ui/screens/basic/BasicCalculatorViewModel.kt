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
                android.util.Log.d("BasicCalcVM", "Received command from flow: $command")
                handleSpeechCommand(command)
            }
        }
    }

    fun startListening() {
        speechRecognitionManager.startListening()
    }

    fun onNumberClick(number: String) {
        val currentState = _state.value
        android.util.Log.d("BasicCalcVM", "onNumberClick: number='$number', isResultDisplayed=${currentState.isResultDisplayed}, expression='${currentState.expression}'")

        if (currentState.isResultDisplayed) {
            // Start new expression after result
            android.util.Log.d("BasicCalcVM", "onNumberClick: Starting new expression with '$number'")
            _state.value = currentState.copy(
                expression = number,
                previousExpression = "${currentState.expression} = ${currentState.result}",
                isResultDisplayed = false,
                isError = false
            )
        } else {
            // Append to current expression
            val newExpression = if (currentState.expression == "0") {
                number
            } else {
                currentState.expression + number
            }
            android.util.Log.d("BasicCalcVM", "onNumberClick: Appending, new expression='$newExpression'")
            _state.value = currentState.copy(
                expression = newExpression,
                isError = false
            )
        }

        // Calculate preview result
        calculatePreview()
    }

    fun onOperatorClick(operator: String) {
        val currentState = _state.value
        android.util.Log.d("BasicCalcVM", "onOperatorClick: operator='$operator', isResultDisplayed=${currentState.isResultDisplayed}, expression='${currentState.expression}', result='${currentState.result}'")

        val newExpression = if (currentState.isResultDisplayed) {
            // Continue from result
            android.util.Log.d("BasicCalcVM", "onOperatorClick: Continuing from result '${currentState.result}'")
            currentState.result + operator
        } else if (currentState.expression.isNotEmpty()) {
            // Check if last char is operator and replace it
            val lastChar = currentState.expression.lastOrNull()
            if (lastChar != null && lastChar in "+-×÷") {
                currentState.expression.dropLast(1) + operator
            } else {
                currentState.expression + operator
            }
        } else {
            // Start with operator (use 0)
            "0$operator"
        }

        android.util.Log.d("BasicCalcVM", "onOperatorClick: New expression='$newExpression', setting isResultDisplayed=false")
        _state.value = currentState.copy(
            expression = newExpression,
            isResultDisplayed = false,
            isError = false
        )
    }

    fun onDecimalClick() {
        val currentState = _state.value

        // Find last number in expression
        val lastNumber = currentState.expression.split(Regex("[+\\-×÷]")).lastOrNull() ?: ""

        // Only add decimal if last number doesn't have one
        if (!lastNumber.contains(".")) {
            val newExpression = if (currentState.isResultDisplayed) {
                "0."
            } else if (currentState.expression.isEmpty() || currentState.expression.last() in "+-×÷") {
                currentState.expression + "0."
            } else {
                currentState.expression + "."
            }

            _state.value = currentState.copy(
                expression = newExpression,
                isResultDisplayed = false,
                isError = false
            )
        }
    }

    fun onEqualsClick() {
        val currentState = _state.value
        android.util.Log.d("BasicCalcVM", "onEqualsClick: expression='${currentState.expression}', result='${currentState.result}'")

        if (currentState.expression.isEmpty()) return

        // Convert display operators to calculation operators
        val calcExpression = currentState.expression
            .replace("×", "*")
            .replace("÷", "/")

        calculatorEngine.evaluate(calcExpression).fold(
            onSuccess = { result ->
                val formattedResult = calculatorEngine.formatResult(result)
                android.util.Log.d("BasicCalcVM", "onEqualsClick: Success, formattedResult='$formattedResult', setting isResultDisplayed=true")

                _state.value = currentState.copy(
                    result = formattedResult,
                    previousExpression = currentState.expression,
                    isResultDisplayed = true,
                    isError = false
                )

                // Save to history
                saveToHistory(currentState.expression, formattedResult)
            },
            onFailure = { error ->
                android.util.Log.e("BasicCalcVM", "onEqualsClick: Failed, error=${error.message}")
                _state.value = currentState.copy(
                    result = "Hata",
                    isError = true,
                    errorMessage = error.message
                )
            }
        )
    }

    fun onClearClick() {
        _state.value = CalculatorState()
    }

    fun onDeleteClick() {
        val currentState = _state.value

        if (currentState.isResultDisplayed) {
            _state.value = CalculatorState()
        } else if (currentState.expression.isNotEmpty()) {
            val newExpression = currentState.expression.dropLast(1)
            _state.value = currentState.copy(
                expression = newExpression,
                isError = false
            )
            calculatePreview()
        }
    }

    fun onPercentClick() {
        val currentState = _state.value

        if (currentState.expression.isNotEmpty()) {
            // Try to calculate percentage of the last number
            val expression = currentState.expression
                .replace("×", "*")
                .replace("÷", "/")

            calculatorEngine.evaluate("($expression)/100").fold(
                onSuccess = { result ->
                    val formattedResult = calculatorEngine.formatResult(result)
                    _state.value = currentState.copy(
                        expression = formattedResult,
                        result = formattedResult,
                        isResultDisplayed = true
                    )
                },
                onFailure = { /* Keep current state */ }
            )
        }
    }

    fun onParenthesisClick(paren: String) {
        val currentState = _state.value

        val newExpression = if (currentState.isResultDisplayed) {
            paren
        } else {
            currentState.expression + paren
        }

        _state.value = currentState.copy(
            expression = newExpression,
            isResultDisplayed = false
        )
    }

    fun onMemoryStore() {
        val currentState = _state.value
        val valueToStore = if (currentState.isResultDisplayed) {
            currentState.result.toDoubleOrNull() ?: 0.0
        } else {
            val calcExpression = currentState.expression.replace("×", "*").replace("÷", "/")
            calculatorEngine.evaluate(calcExpression).getOrNull() ?: 0.0
        }

        _state.value = currentState.copy(
            memoryValue = valueToStore,
            hasMemory = true
        )
    }

    fun onMemoryRecall() {
        val currentState = _state.value
        if (currentState.hasMemory) {
            val memoryString = calculatorEngine.formatResult(currentState.memoryValue)
            _state.value = currentState.copy(
                expression = if (currentState.isResultDisplayed) {
                    memoryString
                } else {
                    currentState.expression + memoryString
                },
                isResultDisplayed = false
            )
        }
    }

    fun onMemoryClear() {
        _state.value = _state.value.copy(
            memoryValue = 0.0,
            hasMemory = false
        )
    }

    fun onMemoryAdd() {
        val currentState = _state.value
        val valueToAdd = currentState.result.toDoubleOrNull() ?: return

        _state.value = currentState.copy(
            memoryValue = currentState.memoryValue + valueToAdd,
            hasMemory = true
        )
    }

    fun onMemorySubtract() {
        val currentState = _state.value
        val valueToSubtract = currentState.result.toDoubleOrNull() ?: return

        _state.value = currentState.copy(
            memoryValue = currentState.memoryValue - valueToSubtract,
            hasMemory = true
        )
    }

    // Speech recognition functions - startListening is defined above with lazy init

    fun stopListening() {
        speechRecognitionManager.stopListening()
    }

    fun resetSpeechState() {
        speechRecognitionManager.resetState()
    }

    private fun handleSpeechCommand(command: SpeechCommand) {
        android.util.Log.d("BasicCalcVM", "handleSpeechCommand called with: $command")
        when (command) {
            is SpeechCommand.Calculate -> {
                android.util.Log.d("BasicCalcVM", "Processing Calculate: ${command.expression}")

                // Expression'ı direkt hesapla (display formatına çevirmeden)
                val calcExpression = command.expression
                android.util.Log.d("BasicCalcVM", "Calculating expression: $calcExpression")

                calculatorEngine.evaluate(calcExpression).fold(
                    onSuccess = { result ->
                        val formattedResult = calculatorEngine.formatResult(result)
                        val displayExpression = command.expression.replace("*", "×").replace("/", "÷")

                        android.util.Log.d("BasicCalcVM", "Calculation success - result: $formattedResult")

                        _state.value = _state.value.copy(
                            expression = displayExpression,
                            result = formattedResult,
                            previousExpression = displayExpression,
                            isResultDisplayed = true,
                            isError = false
                        )

                        // Save to history
                        saveToHistory(displayExpression, formattedResult)

                        android.util.Log.d("BasicCalcVM", "State updated - expression: ${_state.value.expression}, result: ${_state.value.result}")
                    },
                    onFailure = { error ->
                        android.util.Log.e("BasicCalcVM", "Calculation failed: ${error.message}")
                        val displayExpression = command.expression.replace("*", "×").replace("/", "÷")
                        _state.value = _state.value.copy(
                            expression = displayExpression,
                            result = "Hata",
                            isError = true,
                            errorMessage = error.message
                        )
                    }
                )
            }
            is SpeechCommand.Clear -> onClearClick()
            is SpeechCommand.Delete -> onDeleteClick()
            is SpeechCommand.Equals -> onEqualsClick()
            is SpeechCommand.ContinueCalculation -> {
                android.util.Log.d("BasicCalcVM", "Processing ContinueCalculation: ${command.operatorAndValue}")

                val currentState = _state.value
                android.util.Log.d("BasicCalcVM", "Current state - result: '${currentState.result}', expression: '${currentState.expression}', isResultDisplayed: ${currentState.isResultDisplayed}")

                // Mevcut sonucu al (eğer sonuç gösteriliyorsa sonucu, değilse ifadeyi hesapla)
                val currentResult = if (currentState.isResultDisplayed || currentState.result != "0") {
                    // Sonuç zaten Locale.US formatında (nokta ondalık ayırıcı)
                    currentState.result
                } else {
                    // Mevcut ifadeyi hesapla
                    val calcExpr = currentState.expression.replace("×", "*").replace("÷", "/")
                    calculatorEngine.evaluate(calcExpr).getOrNull()?.let {
                        calculatorEngine.formatResult(it)
                    } ?: "0"
                }

                android.util.Log.d("BasicCalcVM", "Using currentResult: '$currentResult'")

                // Mevcut sonuç + yeni işlem
                val fullExpression = currentResult + command.operatorAndValue
                android.util.Log.d("BasicCalcVM", "Full expression: $fullExpression")

                calculatorEngine.evaluate(fullExpression).fold(
                    onSuccess = { result ->
                        val formattedResult = calculatorEngine.formatResult(result)
                        val displayExpression = fullExpression.replace("*", "×").replace("/", "÷")

                        android.util.Log.d("BasicCalcVM", "Continue calculation success - result: $formattedResult")

                        _state.value = _state.value.copy(
                            expression = displayExpression,
                            result = formattedResult,
                            previousExpression = displayExpression,
                            isResultDisplayed = true,
                            isError = false
                        )

                        // Save to history
                        saveToHistory(displayExpression, formattedResult)
                    },
                    onFailure = { error ->
                        android.util.Log.e("BasicCalcVM", "Continue calculation failed: ${error.message}")
                        _state.value = _state.value.copy(
                            result = "Hata",
                            isError = true,
                            errorMessage = error.message
                        )
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
                    _state.value = currentState.copy(
                        result = calculatorEngine.formatResult(result),
                        isError = false
                    )
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
        _state.value = _state.value.copy(
            expression = expression,
            isResultDisplayed = false
        )
        calculatePreview()
    }

    override fun onCleared() {
        super.onCleared()
        speechRecognitionManager.destroy()
    }
}

