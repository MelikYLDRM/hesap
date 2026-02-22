package com.melikyldrm.hesap.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryLight,
    onPrimaryContainer = PrimaryDark,
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryLight,
    onSecondaryContainer = SecondaryDark,
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    tertiaryContainer = TertiaryLight,
    onTertiaryContainer = TertiaryDark,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    error = Error,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryLight,
    onPrimary = PrimaryDark,
    primaryContainer = Primary,
    onPrimaryContainer = OnPrimary,
    secondary = SecondaryLight,
    onSecondary = SecondaryDark,
    secondaryContainer = Secondary,
    onSecondaryContainer = OnSecondary,
    tertiary = TertiaryLight,
    onTertiary = TertiaryDark,
    tertiaryContainer = Tertiary,
    onTertiaryContainer = OnTertiary,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    error = Error,
    onError = Color.White
)

// Calculator specific colors that change with theme
data class CalculatorColors(
    val numberButton: Color,
    val numberButtonText: Color,
    val operatorButton: Color,
    val operatorButtonText: Color,
    val functionButton: Color,
    val functionButtonText: Color,
    val equalsButton: Color,
    val equalsButtonText: Color,
    val clearButton: Color,
    val clearButtonText: Color,
    val displayBackground: Color,
    val displayText: Color,
    val displaySecondaryText: Color
)

val LightCalculatorColors = CalculatorColors(
    numberButton = NumberButtonLight,
    numberButtonText = NumberButtonTextLight,
    operatorButton = OperatorButtonLight,
    operatorButtonText = OperatorButtonTextLight,
    functionButton = FunctionButtonLight,
    functionButtonText = FunctionButtonTextLight,
    equalsButton = EqualsButtonLight,
    equalsButtonText = EqualsButtonTextLight,
    clearButton = ClearButtonLight,
    clearButtonText = ClearButtonTextLight,
    displayBackground = DisplayBackgroundLight,
    displayText = DisplayTextLight,
    displaySecondaryText = DisplaySecondaryTextLight
)

val DarkCalculatorColors = CalculatorColors(
    numberButton = NumberButtonDark,
    numberButtonText = NumberButtonTextDark,
    operatorButton = OperatorButtonDark,
    operatorButtonText = OperatorButtonTextDark,
    functionButton = FunctionButtonDark,
    functionButtonText = FunctionButtonTextDark,
    equalsButton = EqualsButtonDark,
    equalsButtonText = EqualsButtonTextDark,
    clearButton = ClearButtonDark,
    clearButtonText = ClearButtonTextDark,
    displayBackground = DisplayBackgroundDark,
    displayText = DisplayTextDark,
    displaySecondaryText = DisplaySecondaryTextDark
)

val LocalCalculatorColors = staticCompositionLocalOf { LightCalculatorColors }

@Composable
fun HesapTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val calculatorColors = if (darkTheme) DarkCalculatorColors else LightCalculatorColors

    val view = LocalView.current
    val statusBarColor = colorScheme.background.toArgb()

    if (!view.isInEditMode) {
        // LaunchedEffect sadece darkTheme değiştiğinde çalışır
        androidx.compose.runtime.LaunchedEffect(darkTheme) {
            val window = (view.context as Activity).window
            window.statusBarColor = statusBarColor
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    androidx.compose.runtime.CompositionLocalProvider(
        LocalCalculatorColors provides calculatorColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

// Extension to easily access calculator colors
object CalculatorTheme {
    val colors: CalculatorColors
        @Composable
        get() = LocalCalculatorColors.current
}

