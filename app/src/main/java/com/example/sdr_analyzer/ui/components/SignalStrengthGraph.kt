package com.example.sdr_analyzer.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.NativeCanvas
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sdr_analyzer.data.model.Frequency
import com.example.sdr_analyzer.data.model.MHz
import com.example.sdr_analyzer.data.model.SignalData
import com.example.sdr_analyzer.data.model.toText
import com.example.sdr_analyzer.data.model.toTextWithoutUnit
import com.example.sdr_analyzer.devices.IDevice
import kotlin.math.absoluteValue

inline val Int.dp: Dp
    @Composable get() = with(LocalDensity.current) { this@dp.toDp() }

inline val Dp.px: Float
    @Composable get() = with(LocalDensity.current) { this@px.toPx() }

@Composable
fun SignalStrengthGraph(
    data: List<SignalData>,
    device: IDevice,
    minVirtualAmplitude: Float = -120f,
    maxVirtualAmplitude: Float = -35f,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollableState { delta ->
        device.centerFrequency = device.centerFrequency - (delta / 25)
        delta
    }

    val minAmplitude = minVirtualAmplitude
    val maxAmplitude = maxVirtualAmplitude

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .scrollable(scrollState, orientation = Orientation.Horizontal)
            .background(Color.Black)
    ) {

        val signalPaint = Paint().apply {
            color = Color.Green
            style = PaintingStyle.Stroke
            strokeWidth = 2.dp.toPx()
            isAntiAlias = false
        }

        val textPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 14.sp.toPx()
            isAntiAlias = true
        }

        val gridPaint = Paint().apply {
            color = Color.Gray.copy(alpha = 0.2f)
            style = PaintingStyle.Stroke
            strokeWidth = 1.dp.toPx()
            isAntiAlias = true
        }

        val tickPaint = Paint().apply {
            color = Color.Gray
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

        val smallTickLength = 5.dp.toPx();
        val mediumTickLength = 7.5.dp.toPx();
        val largeTickLength = 10.dp.toPx();

//        // Рисуем сетку по всему графику
//        // Отметки на оси X (частота)
//        val frequencyStep = frequencyRange / 10
//        drawIntoCanvas { canvas ->
//            for (i in 0..10) {
//                val frequency = minFrequency + i * frequencyStep
//                val x = (frequency - minFrequency) / frequencyRange * width
//
//                // Большие отметки
//                drawLine(
//                    color = tickPaint.color,
//                    start = Offset(x, height - paddingBottom),
//                    end = Offset(x, height - paddingBottom + largeTickLength),
//                    strokeWidth = tickPaint.strokeWidth
//                )
//
//                // Малые отметки
//                if (i < 10) {
//                    val smallStep = frequencyStep / 10
//                    for (j in 1..9) {
//                        val smallFrequency = frequency + j * smallStep
//                        val smallX = (smallFrequency - minFrequency) / frequencyRange * width
//                        drawLine(
//                            color = tickPaint.color,
//                            start = Offset(smallX, height - paddingBottom),
//                            end = Offset(smallX, height - paddingBottom + smallTickLength),
//                            strokeWidth = tickPaint.strokeWidth
//                        )
//                    }
//                }
//
//                val text = frequency.toText()
//                val textWidth = textPaint.measureText(text)
//
//
//            }
//        }



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

        drawIntoCanvas { canvas ->
            canvas.drawPath(path, signalPaint)
        }

        drawIntoCanvas { canvas ->
            drawFrequencyTicks(
                minFrequency = minFrequency,
                maxFrequency = maxFrequency,
                height = height,
                width = width,
                nativeCanvas = canvas.nativeCanvas
            )
        }


        drawIntoCanvas { canvas ->
            drawMagnitudeTicks(
                minMagnitude = minAmplitude,
                maxMagnitude = maxAmplitude,
                height = height,
                nativeCanvas = canvas.nativeCanvas
            )
        }
    }
}

fun DrawScope.drawMagnitudeTicks(
    minMagnitude: Float,
    maxMagnitude: Float,
    height: Float,
    nativeCanvas: NativeCanvas
) {
    val smallTickLength = 5.dp.toPx();
    val mediumTickLength = 7.dp.toPx();
    val largeTickLength = 10.dp.toPx();
    val textPaint = createTextPaint()
    val tickPaint = createTickPaint()

    val magnitudeRange = maxMagnitude - minMagnitude

    for (mag in minMagnitude.toInt()..maxMagnitude.toInt()) {
        val y = height - (mag - minMagnitude) / magnitudeRange * height
        drawLine(
            color = tickPaint.color,
            start = Offset(
                when (mag.absoluteValue % 10) {
                    0 -> largeTickLength
                    5 -> mediumTickLength
                    else -> smallTickLength
                },
                y
            ),
            end = Offset(0.0f, y),
            strokeWidth = tickPaint.strokeWidth
        )

        if (mag % 10 == 0) {
            val text = String.format("%d", mag)
            val textHeight = textPaint.fontMetrics.bottom - textPaint.fontMetrics.top

            if ((y + textHeight) < height) {
                nativeCanvas.drawText(
                    text,
                    12.dp.toPx(),
                    y + textHeight / 4,
                    textPaint
                )
            }
        }
    }
}

fun DrawScope.drawFrequencyTicks(
    minFrequency: Frequency,
    maxFrequency: Frequency,
    width: Float,
    height: Float,
    nativeCanvas: NativeCanvas
) {
    val smallTickLength = 5.dp.toPx();
    val mediumTickLength = 7.5.dp.toPx();
    val largeTickLength = 10.dp.toPx();
    val textPaint = createTextPaint()
    val tickPaint = createTickPaint()

    val frequencyRange = maxFrequency - minFrequency
    val step = frequencyRange/100

    for (i in 0..100) {
        val freq = minFrequency + step * i
        val x = (freq - minFrequency) / frequencyRange * width

        drawLine(
            color = tickPaint.color,
            start = Offset(x, height),
            end = Offset(
                x, height - (when (i % 10) {
                    0 -> largeTickLength
                    5 -> mediumTickLength
                    else -> smallTickLength
                })
            ),
            strokeWidth = tickPaint.strokeWidth
        )

        if (i % 20 == 10) {
            val text = freq.toTextWithoutUnit()
            val textWidth = textPaint.measureText(text)
            nativeCanvas.drawText(
                text,
                x - textWidth / 2,
                height - 15.dp.toPx(),
                textPaint
            )
        }
    }
}

fun DrawScope.createTickPaint() = Paint().apply {
    color = Color.Gray
    style = PaintingStyle.Stroke
    strokeWidth = 1.dp.toPx()
    isAntiAlias = true
}

fun DrawScope.createTextPaint() = android.graphics.Paint().apply {
    color = android.graphics.Color.WHITE
    textSize = 14.sp.toPx()
    isAntiAlias = true
}
