package com.melikyldrm.hesap.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.melikyldrm.hesap.ui.theme.CalculatorTextStyles
import com.melikyldrm.hesap.ui.theme.CalculatorTheme
import java.util.Locale

private fun String.forceDotDecimalForDisplay(): String {
    // Normalize only decimal comma usages like "3,52" -> "3.52".
    // (We don't touch other commas such as thousand separators.)
    return this.replace(Regex("(\\d),(\\d)"), "$1.$2")
}

@Composable
fun CalculatorDisplay(
    expression: String,
    result: String,
    modifier: Modifier = Modifier,
    previousExpression: String = "",
    isError: Boolean = false
) {
    // Debug log
    android.util.Log.d("CalcDisplay", "Rendering - expression: $expression, result: $result")

    val colors = CalculatorTheme.colors
    val scrollState = rememberScrollState()

    // Auto-scroll to end when expression changes (non-animated for faster startup)
    LaunchedEffect(expression) {
        scrollState.scrollTo(scrollState.maxValue)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        color = colors.displayBackground,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            // Previous expression (if available)
            if (previousExpression.isNotEmpty()) {
                Text(
                    text = previousExpression.forceDotDecimalForDisplay(),
                    style = CalculatorTextStyles.historyExpression,
                    color = colors.displaySecondaryText.copy(alpha = 0.5f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Current expression
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = expression.ifEmpty { "0" },
                    style = CalculatorTextStyles.expression,
                    color = colors.displaySecondaryText,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Result with auto-sizing text
            AutoSizeText(
                text = formatDisplayResult(result).forceDotDecimalForDisplay(),
                color = if (isError) {
                    MaterialTheme.colorScheme.error
                } else {
                    colors.displayText
                },
                maxFontSize = 56.sp,
                minFontSize = 16.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Auto-sizing text that scales down to fit the available width
 */
@Composable
fun AutoSizeText(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    maxFontSize: TextUnit = 56.sp,
    minFontSize: TextUnit = 14.sp,
    textAlign: TextAlign = TextAlign.End
) {
    var scaledFontSize by remember { mutableStateOf(maxFontSize) }

    // Text değiştiğinde font boyutunu sıfırla
    LaunchedEffect(text) {
        scaledFontSize = maxFontSize
    }

    Text(
        text = text,
        style = CalculatorTextStyles.resultLarge.copy(fontSize = scaledFontSize),
        color = color,
        maxLines = 1,
        softWrap = false,
        textAlign = textAlign,
        modifier = modifier,
        onTextLayout = { textLayoutResult ->
            if (textLayoutResult.hasVisualOverflow && scaledFontSize > minFontSize) {
                // Font boyutunu küçült ve tekrar dene
                scaledFontSize = (scaledFontSize.value - 4f).coerceAtLeast(minFontSize.value).sp
            }
        }
    )
}

private fun formatDisplayResult(result: String): String {
    if (result.isEmpty()) return "0"

    // Try to format as number with thousand separators.
    // IMPORTANT: We intentionally use Locale.US to force "." as decimal separator in display.
    return try {
        val number = result.toDoubleOrNull() ?: return result

        if (number == number.toLong().toDouble()) {
            String.format(Locale.US, "%,d", number.toLong())
        } else {
            val formatted = String.format(Locale.US, "%,.10f", number)
            formatted.trimEnd('0').trimEnd(',').trimEnd('.')
        }
    } catch (_: Exception) {
        result
    }
}

@Composable
fun CompactDisplay(
    expression: String,
    result: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false
) {
    val colors = CalculatorTheme.colors

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = colors.displayBackground,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.End
    ) {
        Text(
            text = expression.ifEmpty { "0" }.forceDotDecimalForDisplay(),
            style = CalculatorTextStyles.historyExpression,
            color = colors.displaySecondaryText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = result.ifEmpty { "0" }.forceDotDecimalForDisplay(),
            style = CalculatorTextStyles.historyResult,
            color = if (isError) {
                MaterialTheme.colorScheme.error
            } else {
                colors.displayText
            },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
