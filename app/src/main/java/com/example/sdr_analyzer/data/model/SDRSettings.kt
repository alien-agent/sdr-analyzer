package com.example.sdr_analyzer.data.model

class SDRSettings(
    centerFrequency: Float,
    frequencyRange: Float,
    val minFrequency: Float,
    val maxFrequency: Float
) {
    var centerFrequency = centerFrequency
        set(value) {
            field = value.coerceIn(
                minFrequency + frequencyRange / 2,
                maxFrequency - frequencyRange / 2
            )
        }

    var frequencyRange = frequencyRange
        set(value) {
            field = value
            centerFrequency = centerFrequency.coerceIn(
                minFrequency + field / 2,
                maxFrequency - field / 2
            )
        }

    val startFrequency: Float
        get() = centerFrequency - frequencyRange / 2

    val endFrequency: Float
        get() = centerFrequency + frequencyRange / 2
}