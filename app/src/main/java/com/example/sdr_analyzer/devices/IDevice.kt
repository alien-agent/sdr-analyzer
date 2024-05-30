package com.example.sdr_analyzer.devices

import com.example.sdr_analyzer.data.model.Frequency
import com.example.sdr_analyzer.data.model.SignalData

interface IDevice {
    val maxFrequency: Frequency
    val minFrequency: Frequency
    var centerFrequency: Frequency
    var frequencyRange: Frequency
    var frequencyStep: Frequency
    val maxFrequencyRange: Frequency
    var startFrequency: Frequency
        get() = centerFrequency - frequencyRange / 2
        set(value) {centerFrequency = value + frequencyRange/2}
    var endFrequency: Frequency
        get() = centerFrequency + frequencyRange / 2
        set(value) {centerFrequency = value - frequencyRange/2}

    val deviceName: String

    suspend fun getAmplitudes() : List<SignalData>?
}
