package com.example.sdr_analyzer.manager

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.sdr_analyzer.devices.ArinstSSA
import com.example.sdr_analyzer.devices.IDevice
import kotlinx.coroutines.*

const val LOGGER_TAG = "DeviceManager"
const val ACTION_USB_PERMISSION = "com.example.sdr_analyzer.USB_PERMISSION"

enum class ConnectionState {
    Waiting,
    Connecting,
    Failed,
    Connected,
}

class DeviceManager(private val context: Context) {
    private var usbManager: UsbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
    var connectionStatus by mutableStateOf(ConnectionState.Waiting)
        private set
    var connectedDevice: IDevice? = null
        private set
    val isConnected: Boolean
        get() = connectedDevice != null

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (ACTION_USB_PERMISSION == action) {
                synchronized(this) {
                    val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        device?.let {
                            Log.d(LOGGER_TAG, "Permission granted for device ${device.deviceName}")
                            detectAndConnect()
                        }
                    } else {
                        Log.d(LOGGER_TAG, "Permission denied for device ${device?.deviceName}")
                        connectionStatus = ConnectionState.Failed
                    }
                }
            }
        }
    }

    init {
        scope.launch {
            while (true) {
                if (!isConnected) {
                    detectAndConnect()
                }
                delay(3000)
            }
        }
    }

    private fun detectAndConnect() {
        val device = usbManager.deviceList?.values?.firstOrNull() ?: return

        if (!usbManager.hasPermission(device)) {
            Log.d(LOGGER_TAG, "Permission missing, requesting...")
            usbManager.requestPermission(
                device, PendingIntent.getBroadcast(
                    context, 0, Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_IMMUTABLE
                )
            )
            connectionStatus = ConnectionState.Connecting
            return
        }

        val connection: UsbDeviceConnection
        connectionStatus = ConnectionState.Connecting
        try {
            connection = usbManager.openDevice(device)
        } catch (_: Exception) {
            connectionStatus = ConnectionState.Failed
            return
        }

        if (device.vendorId == 1155 && device.deviceId == 1002) {
            connectedDevice = ArinstSSA(connection)
        } else {
            Log.d(LOGGER_TAG, "Unsupported device (${device.vendorId},${device.deviceId})")
            connectionStatus = ConnectionState.Failed
            return
        }

        connectionStatus = ConnectionState.Connected
    }
}
