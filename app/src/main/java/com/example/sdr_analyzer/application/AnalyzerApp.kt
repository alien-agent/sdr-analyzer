package com.example.sdr_analyzer.application

import android.content.Context
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.sdr_analyzer.data.model.ConnectionStatus
import com.example.sdr_analyzer.devices.DemoDevice

@Stable
class AnalyzerApp(context: Context) {
    private var _connectionStatus by mutableStateOf(ConnectionStatus.Waiting)
    val connectionStatus by derivedStateOf { if (isDemoMode) ConnectionStatus.Connected else _connectionStatus }

    private var _deviceManager = DeviceManager(context=context, onStatusUpdated = {_connectionStatus = it})
    private var _demoDevice by mutableStateOf(DemoDevice())

    var screenMaxAmplitude by mutableStateOf(-40f)
    var screenMinAmplitude by mutableStateOf(-110f)
    var isDemoMode by mutableStateOf(false)
    var isFreqOverlayShown by mutableStateOf(true)

    val connectedDevice by derivedStateOf { if (isDemoMode) _demoDevice else _deviceManager.connectedDevice }
}