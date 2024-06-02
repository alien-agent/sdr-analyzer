package com.example.sdr_analyzer.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import com.example.sdr_analyzer.data.model.SignalData

@Composable
fun WaterfallPlot(data: List<List<SignalData>>, modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {

        val paint = Paint().apply {
            style = PaintingStyle.Fill
            isAntiAlias = true
        }

        val width = size.width
        val height = size.height
        val rowHeight = height / data.size

        data.forEachIndexed { rowIndex, rowData ->
            val minFrequency = rowData.minOfOrNull { it.frequency } ?: 0f
            val maxFrequency = rowData.maxOfOrNull { it.frequency } ?: 1f
            val frequencyRange = maxFrequency - minFrequency

            rowData.forEach { signalData ->
                val x = (signalData.frequency - minFrequency) / frequencyRange * width
                val y = rowIndex * rowHeight
                val colorIntensity =
                    ((signalData.amplitude + 120) / 100 * 255).toInt().coerceIn(0, 255)
                paint.color = Color(colorIntensity, 0, 255 - colorIntensity)
                drawRect(
                    color = paint.color,
                    topLeft = Offset(x, y),
                    size = androidx.compose.ui.geometry.Size(width / rowData.size, rowHeight)
                )
            }
        }
    }
}