package com.example.sdr_analyzer.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.NativeCanvas
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sdr_analyzer.application.AnalyzerApp
import com.example.sdr_analyzer.data.model.Frequency
import com.example.sdr_analyzer.data.model.MHz
import com.example.sdr_analyzer.data.model.SignalData
import com.example.sdr_analyzer.data.model.toText
import com.example.sdr_analyzer.data.model.toTextWithoutUnit
import com.example.sdr_analyzer.devices.IDevice
import kotlin.math.absoluteValue

class DrawingSettings(
    val smallTickLength: Float,
    val mediumTickLength: Float,
    val largeTickLength: Float,

    val height: Float,
    val width: Float,

    val tickPaint: Paint,
    val textPaint: android.graphics.Paint,
    val gridPaint: Paint,

    val minFrequency: Frequency,
    val maxFrequency: Frequency,
    val minMagnitude: Float,
    val maxMagnitude: Float,
) {
    val frequencyRange: Float
        get() = maxFrequency - minFrequency
    val magnitudeRange: Float
        get() = maxMagnitude - minMagnitude
}

@Composable
fun SignalStrengthGraph(
    app: AnalyzerApp,
    data: List<SignalData>,
    device: IDevice,
    modifier: Modifier = Modifier,
    minVirtualAmplitude: Float = -120f,
    maxVirtualAmplitude: Float = -35f,
) {
    val scrollState = rememberScrollableState { delta ->
        device.centerFrequency -= 100 * MHz * delta / 1000
        delta
    }

    val minAmplitude = app.screenMinAmplitude
    val maxAmplitude = app.screenMaxAmplitude

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .scrollable(scrollState, orientation = Orientation.Horizontal)
                .background(Color.Black)
        ) {

            val signalPaint = Paint().apply {
                color = Color.Yellow
                style = PaintingStyle.Stroke
                strokeWidth = 1.dp.toPx()
                isAntiAlias = true
            }

            val maxFrequency = data.maxOfOrNull { it.frequency } ?: 1.0F
            val minFrequency = data.minOfOrNull { it.frequency } ?: 0.0F

            val frequencyRange = maxFrequency - minFrequency
            val amplitudeRange = maxAmplitude - minAmplitude

            val width = size.width
            val height = size.height

            // Устанавливаем размеры для отступов слева и снизу
            val paddingBottom = 30.dp.toPx()

            val path = Path()

            data.forEachIndexed { index, signalData ->
                val x = (signalData.frequency - minFrequency) / frequencyRange * width
                val y =
                    height - paddingBottom - (signalData.signalStrength - minAmplitude) / amplitudeRange * (height - paddingBottom)

                if (index == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }

            val s: DrawingSettings = DrawingSettings(
                smallTickLength = 5.dp.toPx(),
                mediumTickLength = 7.5.dp.toPx(),
                largeTickLength = 10.dp.toPx(),
                height = height,
                width = width,
                tickPaint = Paint().apply {
                    color = Color.Gray
                    style = PaintingStyle.Stroke
                    strokeWidth = 1.dp.toPx()
                    isAntiAlias = true
                },
                textPaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 14.sp.toPx()
                    isAntiAlias = true
                },
                gridPaint = Paint().apply {
                    color = Color.Gray.copy(alpha = 0.2f)
                    style = PaintingStyle.Stroke
                    strokeWidth = 1.dp.toPx()
                    isAntiAlias = true
                },
                minFrequency = minFrequency,
                maxFrequency = maxFrequency,
                minMagnitude = minAmplitude,
                maxMagnitude = maxAmplitude
            )

            drawIntoCanvas { canvas ->
                canvas.drawPath(path, signalPaint)
            }

            drawIntoCanvas { canvas ->
                drawFrequencyTicks(
                    s = s,
                    nativeCanvas = canvas.nativeCanvas
                )
            }

            drawIntoCanvas { canvas ->
                drawMagnitudeTicks(
                    s = s,
                    nativeCanvas = canvas.nativeCanvas
                )
            }
        }
        FreqOverlay(app = app, device)
    }
}

@Composable
fun BoxScope.FreqOverlay(app: AnalyzerApp, device: IDevice) {
    AnimatedVisibility(
        modifier = Modifier.align(Alignment.TopEnd),
        visible = app.isFreqOverlayShown,
        enter = slideInHorizontally(initialOffsetX = { w -> w }) + expandHorizontally(expandFrom = Alignment.End),
        exit = slideOutHorizontally(targetOffsetX = { w -> w }) + shrinkHorizontally(
            shrinkTowards = Alignment.End
        )
    ) {
        Column(
            modifier = Modifier
                .padding(10.dp)
                .clip(shape = RoundedCornerShape(10.dp))
                .background(Color(0xAA222222))
        ) {
            listOf(
                "Центр" to device.centerFrequency,
                "Старт" to device.startFrequency,
                "Стоп" to device.endFrequency,
                "Полоса" to device.frequencyRange
            ).map {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .width(160.dp)
                ) {
                    Text(it.first, color = Color.Cyan)
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = it.second.toText(),
                        color = Color.LightGray
                    )
                }
            }
        }
    }
}

fun DrawScope.drawMagnitudeTicks(
    s: DrawingSettings,
    nativeCanvas: NativeCanvas
) {
    for (mag in s.minMagnitude.toInt()..s.maxMagnitude.toInt()) {
        val y = s.height - (mag - s.minMagnitude) / s.magnitudeRange * s.height
        drawLine(
            color = s.tickPaint.color,
            start = Offset(
                when (mag.absoluteValue % 10) {
                    0 -> s.largeTickLength
                    5 -> s.mediumTickLength
                    else -> s.smallTickLength
                },
                y
            ),
            end = Offset(0.0f, y),
            strokeWidth = s.tickPaint.strokeWidth
        )

        if (mag % 10 == 0) {
            drawLine(
                color = s.gridPaint.color,
                start = Offset(0f, y),
                end = Offset(s.width, y),
                strokeWidth = s.gridPaint.strokeWidth
            )

            val text = String.format("%d", mag)
            val textHeight = s.textPaint.fontMetrics.bottom - s.textPaint.fontMetrics.top

            if ((y + textHeight) < s.height && (y-textHeight) > 0) {
                nativeCanvas.drawText(
                    text,
                    12.dp.toPx(),
                    y + textHeight / 4,
                    s.textPaint
                )
            }
        }
    }
}

fun DrawScope.drawFrequencyTicks(
    s: DrawingSettings,
    nativeCanvas: NativeCanvas
) {
    val step = s.frequencyRange / 100

    for (i in 0..100) {
        val freq = s.minFrequency + step * i
        val x = (freq - s.minFrequency) / s.frequencyRange * s.width

        drawLine(
            color = s.tickPaint.color,
            start = Offset(x, s.height),
            end = Offset(
                x, s.height - (when (i % 10) {
                    0 -> s.largeTickLength
                    5 -> s.mediumTickLength
                    else -> s.smallTickLength
                })
            ),
            strokeWidth = s.tickPaint.strokeWidth
        )

        if (i % 20 == 10) {
            drawLine(
                color = s.gridPaint.color,
                start = Offset(x, 0f),
                end = Offset(x, s.height),
                strokeWidth = s.gridPaint.strokeWidth
            )

            val text = freq.toTextWithoutUnit()
            val textWidth = s.textPaint.measureText(text)
            nativeCanvas.drawText(
                text,
                x - textWidth / 2,
                s.height - 15.dp.toPx(),
                s.textPaint
            )
        }
    }
}
