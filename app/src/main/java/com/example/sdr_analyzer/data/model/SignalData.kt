package com.example.sdr_analyzer.data.model

data class SignalData(
    val frequency: Frequency, // Частота в МГц
    val signalStrength: Float, // Сила сигнала в dB
)
