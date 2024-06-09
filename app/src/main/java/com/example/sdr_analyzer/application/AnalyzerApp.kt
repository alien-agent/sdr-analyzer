package com.example.sdr_analyzer.application

import android.content.Context
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.sdr_analyzer.data.model.ConnectionStatus
import com.example.sdr_analyzer.data.model.SignalData
import com.example.sdr_analyzer.devices.DemoDevice


@Stable
class AnalyzerApp(context: Context) {
    private var _connectionStatus by mutableStateOf(ConnectionStatus.Waiting)
    val connectionStatus by derivedStateOf { if (isDemoMode) ConnectionStatus.Connected else _connectionStatus }

    var signalData by mutableStateOf(emptyList<SignalData>())
    var dataHistory by mutableStateOf(List(100) { emptyList<SignalData>() })

    private var _deviceManager =
        DeviceManager(
            context = context,
            onStatusUpdated = { _connectionStatus = it },
            onDataReceived = {
                signalData = it
                dataHistory = listOf(it) + dataHistory.slice(0 until 99)
            }
        )
    private var _demoDevice by mutableStateOf(DemoDevice(onDataReceived = {
        if (isDemoMode) {
            signalData = it
            dataHistory = listOf(it) + dataHistory.slice(0 until 99)
        }
    }))

    var screenMaxAmplitude by mutableStateOf(-40f)
    var screenMinAmplitude by mutableStateOf(-110f)
    var isDemoMode by mutableStateOf(false)
    var isFreqOverlayShown by mutableStateOf(true)

    val connectedDevice by derivedStateOf { if (isDemoMode) _demoDevice else _deviceManager.connectedDevice }
}