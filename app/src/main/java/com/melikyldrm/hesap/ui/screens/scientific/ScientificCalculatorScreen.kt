package com.melikyldrm.hesap.ui.screens.scientific

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.melikyldrm.hesap.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScientificCalculatorScreen(
    onHistoryClick: () -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: ScientificViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isRadianMode by viewModel.isRadianMode.collectAsStateWithLifecycle()
    val isSecondFunction by viewModel.isSecondFunction.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bilimsel Hesap Makinesi") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Display
            CalculatorDisplay(
                expression = state.expression,
                result = state.result,
                previousExpression = state.previousExpression,
                isError = state.isError,
                modifier = Modifier.weight(0.22f)
            )

            // Scientific buttons
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
                    .weight(0.78f)
                    .padding(horizontal = 4.dp, vertical = 4.dp)
            )
        }
    }
}

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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // 2nd function toggle
            CalculatorButton(
                text = "2nd",
                onClick = onToggleSecondFunction,
                buttonType = if (isSecondFunction) ButtonType.OPERATOR else ButtonType.FUNCTION,
                modifier = Modifier.weight(1f)
            )
            // RAD/DEG toggle
            CalculatorButton(
                text = if (isRadianMode) "RAD" else "DEG",
                onClick = onToggleRadianMode,
                buttonType = ButtonType.FUNCTION,
                modifier = Modifier.weight(1f)
            )
            CalculatorButton(
                text = if (isSecondFunction) "x³" else "x²",
                onClick = { onFunctionClick("x²") },
                buttonType = ButtonType.FUNCTION,
                modifier = Modifier.weight(1f)
            )
            CalculatorButton(
                text = if (isSecondFunction) "∛" else "√",
                onClick = { onFunctionClick("√") },
                buttonType = ButtonType.FUNCTION,
                modifier = Modifier.weight(1f)
            )
            CalculatorButton(
                text = "xʸ",
                onClick = { onOperatorClick("^") },
                buttonType = ButtonType.FUNCTION,
                modifier = Modifier.weight(1f)
            )
        }

        // Row 2: Trigonometric functions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            CalculatorButton(
                text = if (isSecondFunction) "sin⁻¹" else "sin",
                onClick = { onFunctionClick("sin") },
                buttonType = ButtonType.FUNCTION,
                modifier = Modifier.weight(1f)
            )
            CalculatorButton(
                text = if (isSecondFunction) "cos⁻¹" else "cos",
                onClick = { onFunctionClick("cos") },
                buttonType = ButtonType.FUNCTION,
                modifier = Modifier.weight(1f)
            )
            CalculatorButton(
                text = if (isSecondFunction) "tan⁻¹" else "tan",
                onClick = { onFunctionClick("tan") },
                buttonType = ButtonType.FUNCTION,
                modifier = Modifier.weight(1f)
            )
            CalculatorButton(
                text = if (isSecondFunction) "eˣ" else "ln",
                onClick = { onFunctionClick("ln") },
                buttonType = ButtonType.FUNCTION,
                modifier = Modifier.weight(1f)
            )
            CalculatorButton(
                text = if (isSecondFunction) "10ˣ" else "log",
                onClick = { onFunctionClick("log") },
                buttonType = ButtonType.FUNCTION,
                modifier = Modifier.weight(1f)
            )
        }

        // Row 3: Constants and special
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            CalculatorButton(
                text = "(",
                onClick = { onParenthesisClick("(") },
                buttonType = ButtonType.FUNCTION,
                modifier = Modifier.weight(1f)
            )
            CalculatorButton(
                text = ")",
                onClick = { onParenthesisClick(")") },
                buttonType = ButtonType.FUNCTION,
                modifier = Modifier.weight(1f)
            )
            CalculatorButton(
                text = "π",
                onClick = { onConstantClick("π") },
                buttonType = ButtonType.FUNCTION,
                modifier = Modifier.weight(1f)
            )
            CalculatorButton(
                text = "e",
                onClick = { onConstantClick("e") },
                buttonType = ButtonType.FUNCTION,
                modifier = Modifier.weight(1f)
            )
            CalculatorButton(
                text = "n!",
                onClick = { onFunctionClick("!") },
                buttonType = ButtonType.FUNCTION,
                modifier = Modifier.weight(1f)
            )
        }

        // Row 4: C, DEL, %, ÷
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            CalculatorButton(
                text = "C",
                onClick = onClearClick,
                buttonType = ButtonType.CLEAR,
                modifier = Modifier.weight(1f)
            )
            IconCalculatorButton(
                icon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Backspace,
                        contentDescription = "Sil",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                onClick = onDeleteClick,
                buttonType = ButtonType.FUNCTION,
                modifier = Modifier.weight(1f)
            )
            CalculatorButton(
                text = "%",
                onClick = { onOperatorClick("%") },
                buttonType = ButtonType.FUNCTION,
                modifier = Modifier.weight(1f)
            )
            CalculatorButton(
                text = "÷",
                onClick = { onOperatorClick("÷") },
                buttonType = ButtonType.OPERATOR,
                modifier = Modifier.weight(1f)
            )
        }

        // Row 5-8: Number pad
        listOf(
            listOf("7", "8", "9", "×"),
            listOf("4", "5", "6", "−"),
            listOf("1", "2", "3", "+"),
        ).forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                row.forEach { button ->
                    when (button) {
                        "×" -> CalculatorButton(
                            text = button,
                            onClick = { onOperatorClick("×") },
                            buttonType = ButtonType.OPERATOR,
                            modifier = Modifier.weight(1f)
                        )
                        "−" -> CalculatorButton(
                            text = button,
                            onClick = { onOperatorClick("-") },
                            buttonType = ButtonType.OPERATOR,
                            modifier = Modifier.weight(1f)
                        )
                        "+" -> CalculatorButton(
                            text = button,
                            onClick = { onOperatorClick("+") },
                            buttonType = ButtonType.OPERATOR,
                            modifier = Modifier.weight(1f)
                        )
                        else -> CalculatorButton(
                            text = button,
                            onClick = { onNumberClick(button) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Last row: 0, ., =
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            CalculatorButton(
                text = "0",
                onClick = { onNumberClick("0") },
                modifier = Modifier.weight(2f),
                aspectRatio = 2f
            )
            CalculatorButton(
                text = ".",
                onClick = onDecimalClick,
                modifier = Modifier.weight(1f)
            )
            CalculatorButton(
                text = "=",
                onClick = onEqualsClick,
                buttonType = ButtonType.EQUALS,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
