package com.melikyldrm.hesap.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.*
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.melikyldrm.hesap.MainActivity

/**
 * 2x2 Hesap Makinesi Widget'ı
 * Ana ekranda hızlı erişim ve hesap makinesi görünümü sağlar
 */
class CalculatorWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Responsive(
        setOf(
            DpSize(80.dp, 80.dp),    // 1x1
            DpSize(160.dp, 160.dp),  // 2x2
            DpSize(240.dp, 160.dp),  // 3x2
        )
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            CalculatorWidgetContent()
        }
    }
}

@Composable
private fun CalculatorWidgetContent() {
    val size = LocalSize.current

    GlanceTheme {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.widgetBackground)
                .cornerRadius(16.dp)
                .clickable(actionStartActivity<MainActivity>()),
            contentAlignment = Alignment.Center
        ) {
            when {
                size.width >= 160.dp && size.height >= 160.dp -> {
                    // 2x2 veya daha büyük - Tam hesap makinesi görünümü
                    FullCalculatorContent()
                }
                else -> {
                    // 1x1 - Kompakt görünüm
                    CompactCalculatorContent()
                }
            }
        }
    }
}

@Composable
private fun CompactCalculatorContent() {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "🧮",
            style = TextStyle(fontSize = 28.sp)
        )
        Spacer(modifier = GlanceModifier.height(4.dp))
        Text(
            text = "Hesapla",
            style = TextStyle(
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = GlanceTheme.colors.onSurface
            )
        )
    }
}

@Composable
private fun FullCalculatorContent() {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Display Area
        Box(
            modifier = GlanceModifier
                .fillMaxWidth()
                .height(44.dp)
                .background(GlanceTheme.colors.secondaryContainer)
                .cornerRadius(8.dp)
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Text(
                text = "0",
                style = TextStyle(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlanceTheme.colors.onSecondaryContainer,
                    textAlign = TextAlign.End
                )
            )
        }

        Spacer(modifier = GlanceModifier.height(8.dp))

        // Calculator Buttons Grid
        Column(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Row 1: C, ⌫, %, ÷
            WidgetButtonRow(
                buttons = listOf(
                    WidgetButtonData("C", WidgetButtonType.CLEAR),
                    WidgetButtonData("⌫", WidgetButtonType.FUNCTION),
                    WidgetButtonData("%", WidgetButtonType.FUNCTION),
                    WidgetButtonData("÷", WidgetButtonType.OPERATOR)
                )
            )
            Spacer(modifier = GlanceModifier.height(4.dp))

            // Row 2: 7, 8, 9, ×
            WidgetButtonRow(
                buttons = listOf(
                    WidgetButtonData("7", WidgetButtonType.NUMBER),
                    WidgetButtonData("8", WidgetButtonType.NUMBER),
                    WidgetButtonData("9", WidgetButtonType.NUMBER),
                    WidgetButtonData("×", WidgetButtonType.OPERATOR)
                )
            )
            Spacer(modifier = GlanceModifier.height(4.dp))

            // Row 3: 4, 5, 6, -
            WidgetButtonRow(
                buttons = listOf(
                    WidgetButtonData("4", WidgetButtonType.NUMBER),
                    WidgetButtonData("5", WidgetButtonType.NUMBER),
                    WidgetButtonData("6", WidgetButtonType.NUMBER),
                    WidgetButtonData("-", WidgetButtonType.OPERATOR)
                )
            )
            Spacer(modifier = GlanceModifier.height(4.dp))

            // Row 4: 1, 2, 3, +
            WidgetButtonRow(
                buttons = listOf(
                    WidgetButtonData("1", WidgetButtonType.NUMBER),
                    WidgetButtonData("2", WidgetButtonType.NUMBER),
                    WidgetButtonData("3", WidgetButtonType.NUMBER),
                    WidgetButtonData("+", WidgetButtonType.OPERATOR)
                )
            )
            Spacer(modifier = GlanceModifier.height(4.dp))

            // Row 5: 0, ., =
            WidgetButtonRow(
                buttons = listOf(
                    WidgetButtonData("0", WidgetButtonType.NUMBER),
                    WidgetButtonData(".", WidgetButtonType.NUMBER),
                    WidgetButtonData("=", WidgetButtonType.EQUALS),
                    WidgetButtonData("", WidgetButtonType.EMPTY)
                )
            )
        }
    }
}

private enum class WidgetButtonType {
    NUMBER, OPERATOR, FUNCTION, CLEAR, EQUALS, EMPTY
}

private data class WidgetButtonData(
    val text: String,
    val type: WidgetButtonType
)

@Composable
private fun WidgetButtonRow(buttons: List<WidgetButtonData>) {
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        buttons.forEachIndexed { index, button ->
            if (button.type != WidgetButtonType.EMPTY) {
                WidgetButton(
                    text = button.text,
                    type = button.type,
                    modifier = GlanceModifier.defaultWeight()
                )
            } else {
                Spacer(modifier = GlanceModifier.defaultWeight())
            }
            if (index < buttons.lastIndex) {
                Spacer(modifier = GlanceModifier.width(4.dp))
            }
        }
    }
}

@Composable
private fun WidgetButton(
    text: String,
    type: WidgetButtonType,
    modifier: GlanceModifier = GlanceModifier
) {
    val backgroundColor = when (type) {
        WidgetButtonType.NUMBER -> GlanceTheme.colors.surface
        WidgetButtonType.OPERATOR -> GlanceTheme.colors.primary
        WidgetButtonType.FUNCTION -> GlanceTheme.colors.tertiary
        WidgetButtonType.CLEAR -> GlanceTheme.colors.error
        WidgetButtonType.EQUALS -> GlanceTheme.colors.primary
        WidgetButtonType.EMPTY -> GlanceTheme.colors.surface
    }

    val textColor = when (type) {
        WidgetButtonType.NUMBER -> GlanceTheme.colors.onSurface
        WidgetButtonType.OPERATOR -> GlanceTheme.colors.onPrimary
        WidgetButtonType.FUNCTION -> GlanceTheme.colors.onTertiary
        WidgetButtonType.CLEAR -> GlanceTheme.colors.onError
        WidgetButtonType.EQUALS -> GlanceTheme.colors.onPrimary
        WidgetButtonType.EMPTY -> GlanceTheme.colors.onSurface
    }

    Box(
        modifier = modifier
            .height(28.dp)
            .background(backgroundColor)
            .cornerRadius(6.dp)
            .clickable(actionStartActivity<MainActivity>()),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        )
    }
}

/**
 * Widget Receiver - Android sistem tarafından çağrılır
 */
class CalculatorWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CalculatorWidget()
}

