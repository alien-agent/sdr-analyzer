package com.example.sdr_analyzer.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sdr_analyzer.data.model.Frequency
import com.example.sdr_analyzer.data.model.MHz
import com.example.sdr_analyzer.data.model.SDRSettings
import com.example.sdr_analyzer.data.model.SignalData
import com.example.sdr_analyzer.data.model.toMHz
import com.example.sdr_analyzer.devices.IDevice
import com.example.sdr_analyzer.manager.DeviceManager
import com.example.sdr_analyzer.ui.components.*
import kotlinx.coroutines.delay
import kotlin.math.absoluteValue
import kotlin.math.exp
import kotlin.math.pow
import kotlin.random.Random

class MockDevice : IDevice {
    override val deviceName: String = "Arinst SSA"
    override suspend fun getAmplitudes(): List<SignalData> {
        return generateSampleData(startFrequency, endFrequency)
    }

    override val maxFrequency: Frequency = 6200 * MHz
    override val minFrequency: Frequency = 35 * MHz
    override val maxFrequencyRange: Frequency = 6200 * MHz

    override var frequencyStep: Frequency = 100000.0f
    override var centerFrequency: Frequency = 105 * MHz
        set(value) {
            field =
                value.coerceIn(minFrequency + frequencyRange / 2, maxFrequency - frequencyRange / 2)
        }

    override var frequencyRange: Frequency = 100 * MHz
        set(value) {
            field = value.coerceIn(1.0f, maxFrequencyRange)
            centerFrequency = centerFrequency.coerceIn(
                minFrequency + frequencyRange / 2,
                maxFrequency - frequencyRange / 2
            )
        }

}

@Composable
fun MainScreen(deviceManager: DeviceManager) {
    val manager by remember { mutableStateOf(deviceManager) }
    val mockDevice by remember { mutableStateOf(MockDevice()) }

    var showSettings by remember { mutableStateOf(false) }
    var settingsMenu by remember { mutableStateOf(true) }
    var selectedMenu by remember { mutableStateOf("") }

    var sampleData by remember { mutableStateOf(generateSampleData()) }
    var waterfallData by remember { mutableStateOf(MutableList<List<SignalData>>(0, init = {emptyList()})) }

    LaunchedEffect(Unit) {
        while (true) {
            if(manager.connectedDevice != null){
                val freshData = manager.connectedDevice!!.getAmplitudes()
                if (freshData.isNullOrEmpty()){
                    continue
                }
                sampleData = freshData
                if(waterfallData.size < 200){
                    waterfallData.add(sampleData)
                } else {
                    waterfallData = waterfallData.apply {
                        removeAt(0)
                        add(sampleData)
                    }
                }
            } else {
                waterfallData.toMutableList().add(mockDevice.getAmplitudes())
            }
            delay(50)  // 50 мс = 20 раз в секунду
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .animateContentSize()
    ) {
        NavigationBar(
            onSettingsClick = { showSettings = !showSettings },
            connectionState = manager.connectionStatus,
            deviceName = manager.connectedDevice?.deviceName,
            startFrequency = 100.0f,
            endFrequency = 100.0f
        )

        if (manager.connectedDevice == null) {
            WaitingForDevice()
        } else {
            AnimatedVisibility(
                visible = showSettings,
                enter = expandVertically(
                    spring(
                        stiffness = Spring.StiffnessLow,
                        visibilityThreshold = IntSize.VisibilityThreshold
                    ),
                ),
                exit = shrinkVertically(),
            ) {
                if (settingsMenu) {
                    SettingsMenu(onMenuSelect = { menu ->
                        selectedMenu = menu
                        settingsMenu = false
                    })
                } else {
                    when (selectedMenu) {
                        "Frequency" -> FrequencySettings(
                            device = manager.connectedDevice!!,
                            exit = { settingsMenu = true })
                    }
                }
            }
            SignalStrengthGraph(
                data = sampleData,
                device = manager.connectedDevice!!,
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
        Column(
            modifier = Modifier.fillMaxWidth(0.7f)
        ) {
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
            )
        )
    }
    return data
}


fun generateSampleData(
    startFrequency: Float = 100.0f,
    endFrequency: Float = 200.0f
): List<SignalData> {
    val data = mutableListOf<SignalData>()
    val step = (endFrequency - startFrequency) / 300
    val centerFrequency = when {
        startFrequency > 1000 * MHz -> 1100 * MHz
        startFrequency > 800 * MHz -> 900 * MHz
        startFrequency > 600 * MHz -> 700 * MHz
        startFrequency > 400 * MHz -> 500 * MHz
        startFrequency > 200 * MHz -> 300 * MHz
        startFrequency > 100 * MHz -> 166 * MHz
        else -> 100 * MHz
    }

    var frequency = startFrequency
    while (frequency <= endFrequency) {
        var signalStrength = -110 + Random.nextFloat() * 5
        val diff = (frequency-centerFrequency).absoluteValue
        if (diff < endFrequency-startFrequency/10){
            signalStrength += Random.nextFloat()*100 * Math.exp(-diff.toMHz().toDouble() / 4).toFloat()/2
        }

        data.add(SignalData(frequency = frequency, signalStrength = signalStrength))
        frequency += step
    }
    return data
}
