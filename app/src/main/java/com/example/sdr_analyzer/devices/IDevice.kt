package com.example.sdr_analyzer.devices

import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection

interface IDevice {
    val maxFrequency: Float
    val minFrequency: Float
    var centerFrequency: Float
    var frequencyRange: Float
    val maxFrequencyRange: Float
    val startFrequency: Float
        get() = centerFrequency - frequencyRange / 2
    val endFrequency: Float
        get() = centerFrequency + frequencyRange / 2
    val deviceName: String
}
