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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
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

    val (backgroundColor, textColor, borderColor) = when (buttonType) {
        ButtonType.NUMBER -> Triple(colors.numberButton, colors.numberButtonText, colors.numberButtonText.copy(alpha = 0.25f))
        ButtonType.OPERATOR -> Triple(colors.operatorButton, colors.operatorButtonText, colors.operatorButtonText.copy(alpha = 0.4f))
        ButtonType.FUNCTION -> Triple(colors.functionButton, colors.functionButtonText, colors.functionButtonText.copy(alpha = 0.3f))
        ButtonType.EQUALS -> Triple(colors.equalsButton, colors.equalsButtonText, colors.equalsButtonText.copy(alpha = 0.3f))
        ButtonType.CLEAR -> Triple(colors.clearButton, colors.clearButtonText, colors.clearButtonText.copy(alpha = 0.3f))
    }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "buttonScale"
    )

    Surface(
        modifier = modifier
            .fillMaxSize()
            .padding(1.dp)
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(),
                enabled = enabled,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                }
            ),
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        shadowElevation = if (isPressed) 1.dp else 3.dp,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = text,
                style = TextStyle(
                    fontSize = if (text.length > 3) 12.sp else if (text.length > 2) 14.sp else 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                ),
                color = textColor,
                maxLines = 1
            )
        }
    }
}

@Composable
fun WideCalculatorButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    buttonType: ButtonType = ButtonType.NUMBER,
    enabled: Boolean = true
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val colors = CalculatorTheme.colors

    val (backgroundColor, textColor, borderColor) = when (buttonType) {
        ButtonType.NUMBER -> Triple(colors.numberButton, colors.numberButtonText, colors.numberButtonText.copy(alpha = 0.25f))
        ButtonType.OPERATOR -> Triple(colors.operatorButton, colors.operatorButtonText, colors.operatorButtonText.copy(alpha = 0.4f))
        ButtonType.FUNCTION -> Triple(colors.functionButton, colors.functionButtonText, colors.functionButtonText.copy(alpha = 0.3f))
        ButtonType.EQUALS -> Triple(colors.equalsButton, colors.equalsButtonText, colors.equalsButtonText.copy(alpha = 0.3f))
        ButtonType.CLEAR -> Triple(colors.clearButton, colors.clearButtonText, colors.clearButtonText.copy(alpha = 0.3f))
    }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "buttonScale"
    )

    Surface(
        modifier = modifier
            .fillMaxSize()
            .padding(1.dp)
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(),
                enabled = enabled,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                }
            ),
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        shadowElevation = if (isPressed) 1.dp else 3.dp,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = text,
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                ),
                color = textColor
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

    val (backgroundColor, borderColor) = when (buttonType) {
        ButtonType.NUMBER -> colors.numberButton to colors.numberButtonText.copy(alpha = 0.25f)
        ButtonType.OPERATOR -> colors.operatorButton to colors.operatorButtonText.copy(alpha = 0.4f)
        ButtonType.FUNCTION -> colors.functionButton to colors.functionButtonText.copy(alpha = 0.3f)
        ButtonType.EQUALS -> colors.equalsButton to colors.equalsButtonText.copy(alpha = 0.3f)
        ButtonType.CLEAR -> colors.clearButton to colors.clearButtonText.copy(alpha = 0.3f)
    }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "buttonScale"
    )

    Surface(
        modifier = modifier
            .fillMaxSize()
            .padding(1.dp)
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(),
                enabled = enabled,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                }
            ),
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        shadowElevation = if (isPressed) 1.dp else 3.dp,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            icon()
        }
    }
}
