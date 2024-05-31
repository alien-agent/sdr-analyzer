package com.example.sdr_analyzer.application

import android.content.Context
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.sdr_analyzer.data.model.ConnectionStatus
import com.example.sdr_analyzer.devices.DemoDevice
import com.example.sdr_analyzer.devices.IDevice

@Stable
class AnalyzerApp(context: Context) {
    private var _deviceManager = DeviceManager(context=context, onStatusUpdated = {_connectionStatus = it})
    private var _demoDevice = DemoDevice()

    var screenMaxAmplitude by mutableStateOf(-40)
    var screenMinAmplitude by mutableStateOf(-110)
    var isDemoMode by mutableStateOf(false)

    private var _connectionStatus by mutableStateOf( ConnectionStatus.Waiting)
    val connectionStatus
        get() = if (isDemoMode) ConnectionStatus.Connected else _connectionStatus

    private var _connectedDevice by mutableStateOf(_deviceManager.connectedDevice)
    val connectedDevice
        get() = if (isDemoMode) _demoDevice else _connectedDevice

}