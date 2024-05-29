package com.example.sdr_analyzer.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sdr_analyzer.data.model.SDRSettings
import com.example.sdr_analyzer.data.model.SignalData
import com.example.sdr_analyzer.manager.DeviceManager
import com.example.sdr_analyzer.ui.components.*
import kotlinx.coroutines.delay
import kotlin.math.pow
import kotlin.random.Random

@Composable
fun MainScreen(deviceManager: DeviceManager) {
    val manager by remember { mutableStateOf(deviceManager) }

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
                    sdrSettings.frequencyRange / 100
                )  // Обновляем данные 20 раз в секунду
            waterfallData = waterfallData.toMutableList().apply {
                removeAt(0)
                add(
                    generateSampleData(
                        sdrSettings.startFrequency,
                        sdrSettings.endFrequency,
                        sdrSettings.frequencyRange / 100
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
            connectionState = manager.connectionStatus,
            deviceName = manager.connectedDevice?.deviceName,
            startFrequency = sdrSettings.startFrequency,
            endFrequency = sdrSettings.endFrequency
        )

        if (manager.connectedDevice == null) {
            WaitingForDevice()
        } else {
            SignalStrengthGraph(
                data = sampleData,
                settings = sdrSettings,
                modifier = Modifier.weight(1f)
            )
            WaterfallPlot(data = waterfallData, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun WaitingForDevice() {
    val textColor = Color(0xFF383232)
    val titleTextStyle = TextStyle(
        fontSize = 36.sp,
        color = textColor,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.SansSerif
    )
    val instructionTextStyle = TextStyle(
        fontSize = 16.sp,
        color = textColor,
        fontWeight = FontWeight.Normal,
        fontFamily = FontFamily.SansSerif
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Ожидание\nустройства",
            style = titleTextStyle,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Column (
            modifier= Modifier.
            fillMaxWidth(0.7f)
        ){
            Text(
                text = "1. Подключите устройство с помощью USB-кабеля",
                style = instructionTextStyle,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "2. Убедитесь что устройство включено и работает",
                style = instructionTextStyle,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "3. Разрешите доступ к устройству в диалогом окне",
                style = instructionTextStyle,
                textAlign = TextAlign.Center
            )
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
            signalStrength += Random.nextFloat() * 100 * Math.exp(-distanceFromCenter.toDouble() / 4)
                .toFloat() / 2
            signalStrength = signalStrength.coerceAtMost(-10F)
        }

        data.add(SignalData(frequency = frequency, signalStrength = signalStrength.toFloat()))
        frequency += step
    }
    return data
}
