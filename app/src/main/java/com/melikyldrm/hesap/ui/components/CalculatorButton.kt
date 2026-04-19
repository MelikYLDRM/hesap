package com.melikyldrm.hesap.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.melikyldrm.hesap.ui.theme.CalculatorTheme

enum class ButtonType {
    NUMBER,
    OPERATOR,
    FUNCTION,
    EQUALS,
    CLEAR
}

// Sabit shape - her recomposition'da yeniden oluşturulmaz
private val ButtonShape = RoundedCornerShape(percent = 50)

@Composable
fun CalculatorButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    buttonType: ButtonType = ButtonType.NUMBER,
    enabled: Boolean = true,
    aspectRatio: Float = 1f
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val colors = CalculatorTheme.colors

    val backgroundColor = remember(buttonType, colors) {
        when (buttonType) {
            ButtonType.NUMBER -> colors.numberButton
            ButtonType.OPERATOR -> colors.operatorButton
            ButtonType.FUNCTION -> colors.functionButton
            ButtonType.EQUALS -> colors.equalsButton
            ButtonType.CLEAR -> colors.clearButton
        }
    }
    val textColor = remember(buttonType, colors) {
        when (buttonType) {
            ButtonType.NUMBER -> colors.numberButtonText
            ButtonType.OPERATOR -> colors.operatorButtonText
            ButtonType.FUNCTION -> colors.functionButtonText
            ButtonType.EQUALS -> colors.equalsButtonText
            ButtonType.CLEAR -> colors.clearButtonText
        }
    }

    // Buton tipine göre farklı haptic feedback
    val hapticType = remember(buttonType) {
        when (buttonType) {
            ButtonType.EQUALS -> HapticFeedbackType.LongPress
            else -> HapticFeedbackType.TextHandleMove
        }
    }

    // Hızlı ve hafif animasyon - NoBouncy + StiffnessHigh = anında tepki
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "buttonScale"
    )

    // Equals butonu için premium gradient efekti - remember ile cache
    val isEqualsButton = buttonType == ButtonType.EQUALS
    val equalsGradient = remember(backgroundColor) {
        Brush.linearGradient(
            colors = listOf(
                backgroundColor,
                backgroundColor.copy(red = (backgroundColor.red + 0.08f).coerceAtMost(1f), green = (backgroundColor.green + 0.06f).coerceAtMost(1f)),
                backgroundColor
            )
        )
    }

    // Accessibility label
    val accessibilityLabel = remember(text, buttonType) {
        when (buttonType) {
            ButtonType.EQUALS -> "Eşittir"
            ButtonType.CLEAR -> "Temizle"
            ButtonType.OPERATOR -> when (text) {
                "+" -> "Artı"; "−" -> "Eksi"; "×" -> "Çarpı"; "÷" -> "Bölü"; "%" -> "Yüzde"
                else -> text
            }
            else -> text
        }
    }

    // Font boyutunu remember ile cache'le
    val fontSize = remember(text) {
        when {
            text.length > 3 -> 14.sp
            text.length > 2 -> 16.sp
            else -> 24.sp
        }
    }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .padding(2.dp)
            .scale(scale)
            .clip(ButtonShape)
            .semantics { contentDescription = accessibilityLabel; role = Role.Button }
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(),
                enabled = enabled,
                onClick = {
                    haptic.performHapticFeedback(hapticType)
                    onClick()
                }
            ),
        shape = ButtonShape,
        color = if (isEqualsButton) Color.Transparent else backgroundColor,
        // Sabit elevation - press'te shadow recalculation yok
        shadowElevation = 2.dp,
        border = BorderStroke(0.dp, Color.Transparent)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (isEqualsButton) Modifier.drawBehind { drawRect(equalsGradient) }
                    else Modifier
                )
        ) {
            Text(
                text = text,
                style = TextStyle(
                    fontSize = fontSize,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                ),
                color = textColor,
                maxLines = 1
            )
        }
    }
}

@Composable
fun IconCalculatorButton(
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    buttonType: ButtonType = ButtonType.FUNCTION,
    enabled: Boolean = true,
    contentDescription: String? = null
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val colors = CalculatorTheme.colors

    val backgroundColor = remember(buttonType, colors) {
        when (buttonType) {
            ButtonType.NUMBER -> colors.numberButton
            ButtonType.OPERATOR -> colors.operatorButton
            ButtonType.FUNCTION -> colors.functionButton
            ButtonType.EQUALS -> colors.equalsButton
            ButtonType.CLEAR -> colors.clearButton
        }
    }
    val contentColor = remember(buttonType, colors) {
        when (buttonType) {
            ButtonType.NUMBER -> colors.numberButtonText
            ButtonType.OPERATOR -> colors.operatorButtonText
            ButtonType.FUNCTION -> colors.functionButtonText
            ButtonType.EQUALS -> colors.equalsButtonText
            ButtonType.CLEAR -> colors.clearButtonText
        }
    }

    // Aynı hızlı animasyon
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "buttonScale"
    )

    Surface(
        modifier = modifier
            .fillMaxSize()
            .padding(2.dp)
            .scale(scale)
            .clip(ButtonShape)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(),
                enabled = enabled,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                }
            ),
        shape = ButtonShape,
        color = backgroundColor,
        contentColor = contentColor,
        // Sabit elevation
        shadowElevation = 2.dp,
        border = BorderStroke(0.dp, Color.Transparent)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            icon()
        }
    }
}
