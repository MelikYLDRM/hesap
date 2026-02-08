package com.melikyldrm.hesap.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.melikyldrm.hesap.speech.SpeechState
import com.melikyldrm.hesap.ui.theme.MicActiveColor
import com.melikyldrm.hesap.ui.theme.MicInactiveColor
import com.melikyldrm.hesap.ui.theme.MicListeningPulse

@Composable
fun MicrophoneButton(
    speechState: SpeechState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val isListening = speechState is SpeechState.Listening
    val isProcessing = speechState is SpeechState.Processing

    // Pulse animation for listening state
    val infiniteTransition = rememberInfiniteTransition(label = "micPulse")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val backgroundColor = when {
        isListening -> MicActiveColor
        isProcessing -> MicActiveColor.copy(alpha = 0.7f)
        speechState is SpeechState.Error -> MaterialTheme.colorScheme.error
        speechState is SpeechState.NotAvailable -> Color.Gray
        else -> MicInactiveColor
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Pulse effect background (only when listening)
        if (isListening) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(MicListeningPulse.copy(alpha = pulseAlpha))
            )
        }

        // Main FAB
        FloatingActionButton(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            },
            modifier = Modifier.size(64.dp),
            shape = CircleShape,
            containerColor = backgroundColor,
            contentColor = Color.White,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 6.dp,
                pressedElevation = 12.dp
            )
        ) {
            if (isProcessing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = if (speechState is SpeechState.NotAvailable) {
                        Icons.Default.MicOff
                    } else {
                        Icons.Default.Mic
                    },
                    contentDescription = if (isListening) "Dinlemeyi durdur" else "Sesli komut",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

/**
 * TopAppBar için küçük mikrofon butonu
 */
@Composable
fun SmallMicrophoneButton(
    speechState: SpeechState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val isListening = speechState is SpeechState.Listening
    val isProcessing = speechState is SpeechState.Processing

    val backgroundColor = when {
        isListening -> MicActiveColor
        isProcessing -> MicActiveColor.copy(alpha = 0.7f)
        speechState is SpeechState.Error -> MaterialTheme.colorScheme.error
        speechState is SpeechState.NotAvailable -> Color.Gray
        else -> MicInactiveColor
    }

    // Pulse animation for listening state
    val infiniteTransition = rememberInfiniteTransition(label = "micPulseSmall")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.15f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScaleSmall"
    )

    IconButton(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        modifier = modifier.scale(pulseScale)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            if (isProcessing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = if (speechState is SpeechState.NotAvailable) {
                        Icons.Default.MicOff
                    } else {
                        Icons.Default.Mic
                    },
                    contentDescription = if (isListening) "Dinlemeyi durdur" else "Sesli komut",
                    modifier = Modifier.size(20.dp),
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun SpeechFeedbackCard(
    speechState: SpeechState,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit = {}
) {
    when (speechState) {
        is SpeechState.Listening -> {
            SpeechStatusCard(
                message = "Dinleniyor...",
                isError = false,
                modifier = modifier
            )
        }
        is SpeechState.Processing -> {
            SpeechStatusCard(
                message = "İşleniyor...",
                isError = false,
                modifier = modifier
            )
        }
        is SpeechState.Success -> {
            SpeechResultCard(
                spokenText = speechState.text,
                parsedExpression = speechState.parsedExpression,
                modifier = modifier,
                onDismiss = onDismiss
            )
        }
        is SpeechState.Error -> {
            SpeechStatusCard(
                message = speechState.message,
                isError = true,
                modifier = modifier
            )
        }
        is SpeechState.PermissionRequired -> {
            SpeechStatusCard(
                message = "Mikrofon izni gerekli",
                isError = true,
                modifier = modifier
            )
        }
        is SpeechState.NotAvailable -> {
            SpeechStatusCard(
                message = "Ses tanıma kullanılamıyor",
                isError = true,
                modifier = modifier
            )
        }
        else -> { /* Idle state - show nothing */ }
    }
}

@Composable
private fun SpeechStatusCard(
    message: String,
    isError: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isError) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                MaterialTheme.colorScheme.primaryContainer
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (!isError) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
            }
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isError) {
                    MaterialTheme.colorScheme.onErrorContainer
                } else {
                    MaterialTheme.colorScheme.onPrimaryContainer
                }
            )
        }
    }
}

@Composable
private fun SpeechResultCard(
    spokenText: String,
    parsedExpression: String?,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onDismiss),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "\"$spokenText\"",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            if (parsedExpression != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "→ ${parsedExpression.forceDotDecimalForDisplay()}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

private fun String.forceDotDecimalForDisplay(): String {
    // If the string contains any digit+comma+digit pattern, we assume it's a decimal
    // representation and normalize to '.' for display.
    return this.replace(Regex("(\\d),(\\d)"), "$1.$2")
}
