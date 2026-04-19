package com.melikyldrm.hesap.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.melikyldrm.hesap.ui.theme.CalculatorTextStyles
import com.melikyldrm.hesap.ui.theme.CalculatorTheme
import java.util.Locale

private fun String.forceDotDecimalForDisplay(): String {
    return this.replace(Regex("(\\d),(\\d)"), "$1.$2")
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalculatorDisplay(
    expression: String,
    result: String,
    modifier: Modifier = Modifier,
    previousExpression: String = "",
    isError: Boolean = false
) {
    val colors = CalculatorTheme.colors
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var showActions by remember { mutableStateOf(false) }

    // Auto-scroll to end when expression changes
    LaunchedEffect(expression) {
        scrollState.scrollTo(scrollState.maxValue)
    }

    // Gradient background - remember ile cache'le
    val displayGradient = remember(colors.displayBackground) {
        Brush.verticalGradient(
            colors = listOf(
                colors.displayBackground,
                colors.displayBackground.copy(alpha = 0.85f)
            )
        )
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 4.dp,
        color = Color.Transparent
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(displayGradient, RoundedCornerShape(24.dp))
                .combinedClickable(
                    onClick = { showActions = false },
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showActions = !showActions
                    }
                )
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            // Previous expression
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

            // Result - hafif Crossfade ile (AnimatedContent yerine ~%40 daha az frame drop)
            val displayResult = remember(result) {
                formatDisplayResult(result).forceDotDecimalForDisplay()
            }

            Crossfade(
                targetState = displayResult,
                animationSpec = tween(150),
                label = "resultCrossfade"
            ) { targetResult ->
                AutoSizeText(
                    text = targetResult,
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

            // Copy/Share actions (shown on long press)
            if (showActions && result != "0") {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            copyToClipboard(context, result)
                            showActions = false
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = "Kopyala",
                            modifier = Modifier.size(18.dp),
                            tint = colors.displaySecondaryText
                        )
                    }
                    IconButton(
                        onClick = {
                            shareResult(context, expression, result)
                            showActions = false
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Paylaş",
                            modifier = Modifier.size(18.dp),
                            tint = colors.displaySecondaryText
                        )
                    }
                }
            }
        }
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Hesap Sonucu", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "Kopyalandı", Toast.LENGTH_SHORT).show()
}

private fun shareResult(context: Context, expression: String, result: String) {
    val shareText = if (expression.isNotEmpty()) "$expression = $result" else result
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, shareText)
    }
    context.startActivity(Intent.createChooser(intent, "Sonucu Paylaş"))
}

/**
 * Auto-sizing text - Karakter uzunluğuna göre TEK SEFERDE boyut hesaplar.
 * onTextLayout recomposition döngüsü yok - her tuşa basışta 5-10 recomposition tasarrufu.
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
    val estimatedFontSize = remember(text, maxFontSize, minFontSize) {
        val len = text.length
        when {
            len <= 7 -> maxFontSize
            len <= 10 -> (maxFontSize.value * 0.78f).sp
            len <= 14 -> (maxFontSize.value * 0.62f).sp
            len <= 18 -> (maxFontSize.value * 0.50f).sp
            len <= 24 -> (maxFontSize.value * 0.40f).sp
            else -> minFontSize
        }
    }

    Text(
        text = text,
        style = CalculatorTextStyles.resultLarge.copy(fontSize = estimatedFontSize),
        color = color,
        maxLines = 1,
        softWrap = false,
        textAlign = textAlign,
        modifier = modifier,
        overflow = TextOverflow.Ellipsis
    )
}

private fun formatDisplayResult(result: String): String {
    if (result.isEmpty()) return "0"

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
