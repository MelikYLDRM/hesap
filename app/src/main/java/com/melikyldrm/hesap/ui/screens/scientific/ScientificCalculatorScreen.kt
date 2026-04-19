package com.melikyldrm.hesap.ui.screens.scientific

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.melikyldrm.hesap.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScientificCalculatorScreen(
    onHistoryClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onToggleMode: (() -> Unit)? = null,
    viewModel: ScientificViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isRadianMode by viewModel.isRadianMode.collectAsStateWithLifecycle()
    val isSecondFunction by viewModel.isSecondFunction.collectAsStateWithLifecycle()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bilimsel Hesap Makinesi") },
                navigationIcon = {
                    if (onToggleMode != null) {
                        IconButton(onClick = onToggleMode) {
                            Icon(Icons.Default.Calculate, contentDescription = "Temel Mod")
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onHistoryClick) {
                        Icon(Icons.Default.History, contentDescription = "Geçmiş")
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Ayarlar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                windowInsets = WindowInsets(0.dp)
            )
        }
    ) { paddingValues ->
        if (isLandscape) {
            // ── YATAY MOD: Display üstte ince, altında iki panel yan yana ──
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Display - ince şerit
                CalculatorDisplay(
                    expression = state.expression,
                    result = state.result,
                    previousExpression = state.previousExpression,
                    isError = state.isError,
                    modifier = Modifier.weight(0.30f) // Increased from 0.22f
                )

                // İki panel yan yana
                Row(
                    modifier = Modifier
                        .weight(0.70f) // Decreased from 0.78f
                        .fillMaxWidth()
                ) {
                    // Sol panel: Bilimsel fonksiyonlar
                    ScientificFunctionPad(
                        isRadianMode = isRadianMode,
                        isSecondFunction = isSecondFunction,
                        onToggleRadianMode = viewModel::toggleRadianMode,
                        onToggleSecondFunction = viewModel::toggleSecondFunction,
                        onFunctionClick = viewModel::onFunctionClick,
                        onConstantClick = viewModel::onConstantClick,
                        onParenthesisClick = viewModel::onParenthesisClick,
                        onOperatorClick = viewModel::onOperatorClick,
                        onClearClick = viewModel::onClearClick,
                        onDeleteClick = viewModel::onDeleteClick,
                        modifier = Modifier
                            .weight(0.55f)
                            .fillMaxHeight()
                            .padding(start = 4.dp, end = 2.dp, top = 2.dp, bottom = 4.dp)
                    )
                    // Sağ panel: Sayı tuş takımı
                    ScientificNumberPad(
                        onNumberClick = viewModel::onNumberClick,
                        onOperatorClick = viewModel::onOperatorClick,
                        onDecimalClick = viewModel::onDecimalClick,
                        onEqualsClick = viewModel::onEqualsClick,
                        modifier = Modifier
                            .weight(0.45f)
                            .fillMaxHeight()
                            .padding(start = 2.dp, end = 4.dp, top = 2.dp, bottom = 4.dp)
                    )
                }
            }
        } else {
            // ── DİKEY MOD: Mevcut layout ──
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                CalculatorDisplay(
                    expression = state.expression,
                    result = state.result,
                    previousExpression = state.previousExpression,
                    isError = state.isError,
                    modifier = Modifier.weight(0.35f) // Increased from 0.22f
                )
                ScientificButtonPad(
                    onNumberClick = viewModel::onNumberClick,
                    onOperatorClick = viewModel::onOperatorClick,
                    onFunctionClick = viewModel::onFunctionClick,
                    onConstantClick = viewModel::onConstantClick,
                    onParenthesisClick = viewModel::onParenthesisClick,
                    onDecimalClick = viewModel::onDecimalClick,
                    onEqualsClick = viewModel::onEqualsClick,
                    onClearClick = viewModel::onClearClick,
                    onDeleteClick = viewModel::onDeleteClick,
                    isRadianMode = isRadianMode,
                    isSecondFunction = isSecondFunction,
                    onToggleRadianMode = viewModel::toggleRadianMode,
                    onToggleSecondFunction = viewModel::toggleSecondFunction,
                    modifier = Modifier
                        .weight(0.65f) // Decreased from 0.78f
                        .padding(horizontal = 4.dp, vertical = 4.dp)
                )
            }
        }
    }
}

// ── Yatay mod için SOL PANEL: Bilimsel fonksiyon tuşları ──
@Composable
fun ScientificFunctionPad(
    isRadianMode: Boolean,
    isSecondFunction: Boolean,
    onToggleRadianMode: () -> Unit,
    onToggleSecondFunction: () -> Unit,
    onFunctionClick: (String) -> Unit,
    onConstantClick: (String) -> Unit,
    onParenthesisClick: (String) -> Unit,
    onOperatorClick: (String) -> Unit,
    onClearClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        // Satır 1: Mod tuşları
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            CalculatorButton(text = "2nd", onClick = onToggleSecondFunction,
                buttonType = if (isSecondFunction) ButtonType.OPERATOR else ButtonType.FUNCTION,
                modifier = Modifier.weight(1f))
            CalculatorButton(text = if (isRadianMode) "RAD" else "DEG", onClick = onToggleRadianMode,
                buttonType = ButtonType.FUNCTION, modifier = Modifier.weight(1f))
            CalculatorButton(text = if (isSecondFunction) "x³" else "x²", onClick = { onFunctionClick("x²") },
                buttonType = ButtonType.FUNCTION, modifier = Modifier.weight(1f))
            CalculatorButton(text = if (isSecondFunction) "∛" else "√", onClick = { onFunctionClick("√") },
                buttonType = ButtonType.FUNCTION, modifier = Modifier.weight(1f))
            CalculatorButton(text = "xʸ", onClick = { onOperatorClick("^") },
                buttonType = ButtonType.FUNCTION, modifier = Modifier.weight(1f))
        }
        // Satır 2: Trigonometri
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            CalculatorButton(text = if (isSecondFunction) "sin⁻¹" else "sin", onClick = { onFunctionClick("sin") },
                buttonType = ButtonType.FUNCTION, modifier = Modifier.weight(1f))
            CalculatorButton(text = if (isSecondFunction) "cos⁻¹" else "cos", onClick = { onFunctionClick("cos") },
                buttonType = ButtonType.FUNCTION, modifier = Modifier.weight(1f))
            CalculatorButton(text = if (isSecondFunction) "tan⁻¹" else "tan", onClick = { onFunctionClick("tan") },
                buttonType = ButtonType.FUNCTION, modifier = Modifier.weight(1f))
            CalculatorButton(text = if (isSecondFunction) "eˣ" else "ln", onClick = { onFunctionClick("ln") },
                buttonType = ButtonType.FUNCTION, modifier = Modifier.weight(1f))
            CalculatorButton(text = if (isSecondFunction) "10ˣ" else "log", onClick = { onFunctionClick("log") },
                buttonType = ButtonType.FUNCTION, modifier = Modifier.weight(1f))
        }
        // Satır 3: Sabitler ve parantez
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            CalculatorButton(text = "(", onClick = { onParenthesisClick("(") },
                buttonType = ButtonType.FUNCTION, modifier = Modifier.weight(1f))
            CalculatorButton(text = ")", onClick = { onParenthesisClick(")") },
                buttonType = ButtonType.FUNCTION, modifier = Modifier.weight(1f))
            CalculatorButton(text = "π", onClick = { onConstantClick("π") },
                buttonType = ButtonType.FUNCTION, modifier = Modifier.weight(1f))
            CalculatorButton(text = "e", onClick = { onConstantClick("e") },
                buttonType = ButtonType.FUNCTION, modifier = Modifier.weight(1f))
            CalculatorButton(text = "n!", onClick = { onFunctionClick("!") },
                buttonType = ButtonType.FUNCTION, modifier = Modifier.weight(1f))
        }
        // Satır 4: Temizle, sil, yüzde, böl, çarp
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            CalculatorButton(text = "C", onClick = onClearClick,
                buttonType = ButtonType.CLEAR, modifier = Modifier.weight(1f))
            IconCalculatorButton(
                icon = {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Backspace,
                        contentDescription = "Sil")
                },
                onClick = onDeleteClick, buttonType = ButtonType.FUNCTION,
                modifier = Modifier.weight(1f))
            CalculatorButton(text = "%", onClick = { onOperatorClick("%") },
                buttonType = ButtonType.FUNCTION, modifier = Modifier.weight(1f))
            CalculatorButton(text = "÷", onClick = { onOperatorClick("÷") },
                buttonType = ButtonType.OPERATOR, modifier = Modifier.weight(1f))
            CalculatorButton(text = "×", onClick = { onOperatorClick("×") },
                buttonType = ButtonType.OPERATOR, modifier = Modifier.weight(1f))
        }
    }
}

// ── Yatay mod için SAĞ PANEL: Sayı tuş takımı ──
@Composable
fun ScientificNumberPad(
    onNumberClick: (String) -> Unit,
    onOperatorClick: (String) -> Unit,
    onDecimalClick: () -> Unit,
    onEqualsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        // 7, 8, 9, −
        Row(modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            CalculatorButton(text = "7", onClick = { onNumberClick("7") }, modifier = Modifier.weight(1f))
            CalculatorButton(text = "8", onClick = { onNumberClick("8") }, modifier = Modifier.weight(1f))
            CalculatorButton(text = "9", onClick = { onNumberClick("9") }, modifier = Modifier.weight(1f))
            CalculatorButton(text = "−", onClick = { onOperatorClick("-") },
                buttonType = ButtonType.OPERATOR, modifier = Modifier.weight(1f))
        }
        // 4, 5, 6, +
        Row(modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            CalculatorButton(text = "4", onClick = { onNumberClick("4") }, modifier = Modifier.weight(1f))
            CalculatorButton(text = "5", onClick = { onNumberClick("5") }, modifier = Modifier.weight(1f))
            CalculatorButton(text = "6", onClick = { onNumberClick("6") }, modifier = Modifier.weight(1f))
            CalculatorButton(text = "+", onClick = { onOperatorClick("+") },
                buttonType = ButtonType.OPERATOR, modifier = Modifier.weight(1f))
        }
        // 1, 2, 3, =
        Row(modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            CalculatorButton(text = "1", onClick = { onNumberClick("1") }, modifier = Modifier.weight(1f))
            CalculatorButton(text = "2", onClick = { onNumberClick("2") }, modifier = Modifier.weight(1f))
            CalculatorButton(text = "3", onClick = { onNumberClick("3") }, modifier = Modifier.weight(1f))
            CalculatorButton(text = "=", onClick = onEqualsClick,
                buttonType = ButtonType.EQUALS, modifier = Modifier.weight(1f))
        }
        // 0(geniş), .
        Row(modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            CalculatorButton(text = "0", onClick = { onNumberClick("0") },
                modifier = Modifier.weight(2f), aspectRatio = 2f)
            CalculatorButton(text = ".", onClick = onDecimalClick, modifier = Modifier.weight(1f))
        }
    }
}

// ── Dikey mod için orijinal tam tuş takımı (portrait) ──
@Composable
fun ScientificButtonPad(
    onNumberClick: (String) -> Unit,
    onOperatorClick: (String) -> Unit,
    onFunctionClick: (String) -> Unit,
    onConstantClick: (String) -> Unit,
    onParenthesisClick: (String) -> Unit,
    onDecimalClick: () -> Unit,
    onEqualsClick: () -> Unit,
    onClearClick: () -> Unit,
    onDeleteClick: () -> Unit,
    isRadianMode: Boolean,
    isSecondFunction: Boolean,
    onToggleRadianMode: () -> Unit,
    onToggleSecondFunction: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        // Row 1: Mode buttons
        Row(modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            CalculatorButton(text = "2nd", onClick = onToggleSecondFunction,
                buttonType = if (isSecondFunction) ButtonType.OPERATOR else ButtonType.FUNCTION,
                modifier = Modifier.weight(1f))
            CalculatorButton(text = if (isRadianMode) "RAD" else "DEG", onClick = onToggleRadianMode,
                buttonType = ButtonType.FUNCTION, modifier = Modifier.weight(1f))
            CalculatorButton(text = if (isSecondFunction) "x³" else "x²", onClick = { onFunctionClick("x²") },
                buttonType = ButtonType.FUNCTION, modifier = Modifier.weight(1f))
            CalculatorButton(text = if (isSecondFunction) "∛" else "√", onClick = { onFunctionClick("√") },
                buttonType = ButtonType.FUNCTION, modifier = Modifier.weight(1f))
            CalculatorButton(text = "xʸ", onClick = { onOperatorClick("^") },
                buttonType = ButtonType.FUNCTION, modifier = Modifier.weight(1f))
        }
        // Row 2: Trigonometric functions
        Row(modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            CalculatorButton(text = if (isSecondFunction) "sin⁻¹" else "sin", onClick = { onFunctionClick("sin") },
                buttonType = ButtonType.FUNCTION, modifier = Modifier.weight(1f))
            CalculatorButton(text = if (isSecondFunction) "cos⁻¹" else "cos", onClick = { onFunctionClick("cos") },
                buttonType = ButtonType.FUNCTION, modifier = Modifier.weight(1f))
            CalculatorButton(text = if (isSecondFunction) "tan⁻¹" else "tan", onClick = { onFunctionClick("tan") },
                buttonType = ButtonType.FUNCTION, modifier = Modifier.weight(1f))
            CalculatorButton(text = if (isSecondFunction) "eˣ" else "ln", onClick = { onFunctionClick("ln") },
                buttonType = ButtonType.FUNCTION, modifier = Modifier.weight(1f))
            CalculatorButton(text = if (isSecondFunction) "10ˣ" else "log", onClick = { onFunctionClick("log") },
                buttonType = ButtonType.FUNCTION, modifier = Modifier.weight(1f))
        }
        // Row 3: Constants and special
        Row(modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            CalculatorButton(text = "(", onClick = { onParenthesisClick("(") },
                buttonType = ButtonType.FUNCTION, modifier = Modifier.weight(1f))
            CalculatorButton(text = ")", onClick = { onParenthesisClick(")") },
                buttonType = ButtonType.FUNCTION, modifier = Modifier.weight(1f))
            CalculatorButton(text = "π", onClick = { onConstantClick("π") },
                buttonType = ButtonType.FUNCTION, modifier = Modifier.weight(1f))
            CalculatorButton(text = "e", onClick = { onConstantClick("e") },
                buttonType = ButtonType.FUNCTION, modifier = Modifier.weight(1f))
            CalculatorButton(text = "n!", onClick = { onFunctionClick("!") },
                buttonType = ButtonType.FUNCTION, modifier = Modifier.weight(1f))
        }
        // Row 4: C, DEL, %, ÷
        Row(modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            CalculatorButton(text = "C", onClick = onClearClick,
                buttonType = ButtonType.CLEAR, modifier = Modifier.weight(1f))
            IconCalculatorButton(
                icon = {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Backspace,
                        contentDescription = "Sil")
                },
                onClick = onDeleteClick, buttonType = ButtonType.FUNCTION,
                modifier = Modifier.weight(1f))
            CalculatorButton(text = "%", onClick = { onOperatorClick("%") },
                buttonType = ButtonType.FUNCTION, modifier = Modifier.weight(1f))
            CalculatorButton(text = "÷", onClick = { onOperatorClick("÷") },
                buttonType = ButtonType.OPERATOR, modifier = Modifier.weight(1f))
        }
        // Rows 5-7: Number pad
        listOf(
            listOf("7", "8", "9", "×"),
            listOf("4", "5", "6", "−"),
            listOf("1", "2", "3", "+"),
        ).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                row.forEach { button ->
                    when (button) {
                        "×" -> CalculatorButton(text = button, onClick = { onOperatorClick("×") },
                            buttonType = ButtonType.OPERATOR, modifier = Modifier.weight(1f))
                        "−" -> CalculatorButton(text = button, onClick = { onOperatorClick("-") },
                            buttonType = ButtonType.OPERATOR, modifier = Modifier.weight(1f))
                        "+" -> CalculatorButton(text = button, onClick = { onOperatorClick("+") },
                            buttonType = ButtonType.OPERATOR, modifier = Modifier.weight(1f))
                        else -> CalculatorButton(text = button, onClick = { onNumberClick(button) },
                            modifier = Modifier.weight(1f))
                    }
                }
            }
        }
        // Last row: 0, ., =
        Row(modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            CalculatorButton(text = "0", onClick = { onNumberClick("0") },
                modifier = Modifier.weight(2f), aspectRatio = 2f)
            CalculatorButton(text = ".", onClick = onDecimalClick, modifier = Modifier.weight(1f))
            CalculatorButton(text = "=", onClick = onEqualsClick,
                buttonType = ButtonType.EQUALS, modifier = Modifier.weight(1f))
        }
    }
}
