package com.example.sdr_analyzer.ui.screens

import androidx.compose.animation.animateContentSize
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
import com.example.sdr_analyzer.data.model.SignalData
import com.example.sdr_analyzer.application.AnalyzerApp
import com.example.sdr_analyzer.ui.components.*
import kotlinx.coroutines.delay


@Composable
fun MainScreen(app: AnalyzerApp) {
    var showSettings by remember { mutableStateOf(false) }

    var sampleData by remember { mutableStateOf(emptyList<SignalData>()) }
    var waterfallData by remember {
        mutableStateOf(
            MutableList<List<SignalData>>(
                0,
                init = { emptyList() })
        )
    }

    LaunchedEffect(Unit) {
        while (true) {
            if (app.connectedDevice != null) {
                val freshData = app.connectedDevice!!.getAmplitudes()
                if (freshData.isNullOrEmpty()) {
                    continue
                }
                sampleData = freshData
                if (waterfallData.size < 200) {
                    waterfallData.add(sampleData)
                } else {
                    waterfallData = waterfallData.apply {
                        removeAt(0)
                        add(sampleData)
                    }
                }
            } else {
                waterfallData.toMutableList().add(sampleData)
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
            connectionStatus = app.connectionStatus,
            deviceName = app.connectedDevice?.deviceName,
            startFrequency = 100.0f,
            endFrequency = 100.0f
        )
        SettingsMenu(isShown = showSettings, app = app)

        if (app.connectedDevice == null) {
            WaitingForDevice()
        } else {

            SignalStrengthGraph(
                app = app,
                device = app.connectedDevice!!,
                data = sampleData,
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
