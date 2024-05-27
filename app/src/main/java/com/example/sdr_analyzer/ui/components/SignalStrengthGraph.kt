package com.example.sdr_analyzer.ui.components

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sdr_analyzer.data.model.SDRSettings
import com.example.sdr_analyzer.data.model.SignalData

@Composable
fun SignalStrengthGraph(
    data: List<SignalData>,
    settings: SDRSettings,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollableState { delta ->
        settings.centerFrequency = settings.centerFrequency - delta / 100
        delta
    }

    val rawMinSignalStrength = data.minOfOrNull { it.signalStrength } ?: -100f
    val rawMaxSignalStrength = data.maxOfOrNull { it.signalStrength } ?: 0f

    // Добавляем 25% пустого пространства сверху и снизу
    val signalStrengthRange = rawMaxSignalStrength - rawMinSignalStrength
    val minSignalStrength = rawMinSignalStrength - signalStrengthRange * 0.25f
    val maxSignalStrength = rawMaxSignalStrength + signalStrengthRange * 0.25f

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .scrollable(scrollState, orientation = Orientation.Horizontal)
            .background(Color.Black)
    ) {

        val paint = Paint().apply {
            color = Color.Green
            style = PaintingStyle.Stroke
            strokeWidth = 2.dp.toPx()
            isAntiAlias = true
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

        val maxFrequency = data.maxOfOrNull { it.frequency } ?: 1f
        val minFrequency = data.minOfOrNull { it.frequency } ?: 0f

        val frequencyRange = maxFrequency - minFrequency
        val scaledSignalStrengthRange = maxSignalStrength - minSignalStrength

        val width = size.width
        val height = size.height

        // Устанавливаем размеры для отступов слева и снизу
        val paddingLeft = 0.dp.toPx()
        val paddingBottom = 0.dp.toPx()

        // Рисуем сетку по всему графику
        // Отметки на оси X (частота)
        val frequencyStep = frequencyRange / 10
        for (i in 0..10) {
            val frequency = minFrequency + i * frequencyStep
            val x = paddingLeft + (frequency - minFrequency) / frequencyRange * width
            drawLine(
                color = gridPaint.color,
                start = Offset(x, 0f),
                end = Offset(x, height),
                strokeWidth = gridPaint.strokeWidth
            )
        }

        // Отметки на оси Y (сила сигнала)
        val signalStrengthStep = scaledSignalStrengthRange / 10
        for (i in 0..10) {
            val signalStrength = minSignalStrength + i * signalStrengthStep
            val y =
                height - (signalStrength - minSignalStrength) / scaledSignalStrengthRange * height
            drawLine(
                color = gridPaint.color,
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = gridPaint.strokeWidth
            )
        }

        val path = Path()

        data.forEachIndexed { index, signalData ->
            val x = paddingLeft + (signalData.frequency - minFrequency) / frequencyRange * width
            val y =
                height - (signalData.signalStrength - minSignalStrength) / scaledSignalStrengthRange * height

            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        drawIntoCanvas { canvas ->
            canvas.drawPath(path, paint)

            // Отрисовка текста для частот на нижней оси
            for (i in 1..9) {
                val frequency = minFrequency + i * (frequencyRange / 10)
                val x = paddingLeft + (frequency - minFrequency) / frequencyRange * width
                val text = String.format("%.1f", frequency)
                val textWidth = textPaint.measureText(text)

                canvas.nativeCanvas.drawText(
                    text,
                    x - textWidth / 2,
                    height - 10.dp.toPx(),
                    textPaint
                )
            }

            // Отрисовка текста для силы сигнала на левой оси
            for (i in 1..9) {
                val signalStrength = minSignalStrength + i * (scaledSignalStrengthRange / 10)
                val y =
                    height - (signalStrength - minSignalStrength) / scaledSignalStrengthRange * height
                val text = String.format("%.0f dB", signalStrength)
                val textHeight = textPaint.fontMetrics.bottom - textPaint.fontMetrics.top

                canvas.nativeCanvas.drawText(
                    text,
                    5.dp.toPx(),
                    y + textHeight / 4,
                    textPaint
                )
            }
        }
    }
}
