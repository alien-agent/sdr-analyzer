package com.example.sdr_analyzer.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.sdr_analyzer.data.model.SDRSettings
import com.example.sdr_analyzer.data.model.SignalData
import com.example.sdr_analyzer.ui.components.*
import kotlinx.coroutines.delay
import kotlin.math.pow
import kotlin.random.Random

@Composable
fun MainScreen() {
    var connectionState by remember { mutableStateOf(ConnectionState.Connecting) }

    var sampleData by remember { mutableStateOf(generateSampleData()) }
    var waterfallData by remember { mutableStateOf(generateWaterfallData(100)) }
    val sdrSettings = remember {
        SDRSettings(
            centerFrequency = 150.0f,
            frequencyRange = 100.0f,
            minFrequency = 1.0f,
            maxFrequency = 7000.0f
        )
    }

    LaunchedEffect(Unit) {
        while (true) {
            sampleData =
                generateSampleData(
                    sdrSettings.startFrequency,
                    sdrSettings.endFrequency,
                    sdrSettings.frequencyRange / 200
                )  // Обновляем данные 20 раз в секунду
            waterfallData = waterfallData.toMutableList().apply {
                removeAt(0)
                add(
                    generateSampleData(
                        sdrSettings.startFrequency,
                        sdrSettings.endFrequency,
                        sdrSettings.frequencyRange / 200
                    )
                )
            }
            delay(50)  // 50 мс = 20 раз в секунду
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        NavigationBar(
            onSettingsClick = { /* TODO: Navigate to settings screen */ },
            connectionState = connectionState,
            startFrequency = sdrSettings.startFrequency,
            endFrequency = sdrSettings.endFrequency
        )
        SignalStrengthGraph(
            data = sampleData,
            settings = sdrSettings,
            modifier = Modifier.weight(1f)
        )
        WaterfallPlot(data = waterfallData, modifier = Modifier.weight(1f))
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(2000)
            connectionState = when (connectionState) {
                ConnectionState.Connecting -> ConnectionState.Connected
                ConnectionState.Connected -> ConnectionState.Disconnected
                ConnectionState.Disconnected -> ConnectionState.Connecting
            }
        }
    }
}

fun generateWaterfallData(
    rows: Int,
    startFrequency: Float = 100.0f,
    endFrequency: Float = 200.0f,
    step: Float = 0.1f
): List<List<SignalData>> {
    val data = mutableListOf<List<SignalData>>()
    repeat(rows) {
        data.add(
            generateSampleData(
                startFrequency,
                endFrequency,
                step
            )
        )
    }
    return data
}


fun generateSampleData(
    startFrequency: Float = 100.0f,
    endFrequency: Float = 200.0f,
    step: Float = 0.1f
): List<SignalData> {
    val data = mutableListOf<SignalData>()
    val centerFrequency = 105.0f
    val bandwidth = (endFrequency - startFrequency) / 6 // Ширина полосы около центральной частоты

    var frequency = startFrequency
    while (frequency <= endFrequency) {
        var signalStrength = -75 + Random.nextFloat() * 5
        val distanceFromCenter = Math.abs(frequency - centerFrequency)
        if (distanceFromCenter < bandwidth / 3) {
            signalStrength += Random.nextFloat() * 100 * Math.exp(-distanceFromCenter.toDouble()/4)
                .toFloat() / 2
            signalStrength = signalStrength.coerceAtMost(-10F)
        }

        data.add(SignalData(frequency = frequency, signalStrength = signalStrength.toFloat()))
        frequency += step
    }
    return data
}