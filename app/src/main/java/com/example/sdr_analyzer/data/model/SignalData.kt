package com.example.sdr_analyzer.data.model

data class SignalData(
    val frequency: Frequency, // Частота в МГц
    val amplitude: Float, // Сила сигнала в dB
)
