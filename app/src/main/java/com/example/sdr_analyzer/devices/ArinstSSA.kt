package com.example.sdr_analyzer.devices

import android.hardware.usb.UsbDeviceConnection

class ArinstSSA(device: UsbDeviceConnection) : IDevice {
    override val maxFrequency: Float = 6200.0f
    override val minFrequency: Float = 35.0f
    override val maxFrequencyRange: Float = 1000.0f
    override val deviceName: String = "Arinst SSA-TG"

    override var centerFrequency: Float = (minFrequency + maxFrequency) / 2
        set(value) {
            field =
                value.coerceIn(minFrequency + frequencyRange / 2, maxFrequency - frequencyRange / 2)
        }

    override var frequencyRange: Float = maxFrequencyRange
        set(value) {
            field = value.coerceIn(0f, maxFrequencyRange)
            centerFrequency = centerFrequency.coerceIn(
                minFrequency + frequencyRange / 2,
                maxFrequency - frequencyRange / 2
            )
        }

}
