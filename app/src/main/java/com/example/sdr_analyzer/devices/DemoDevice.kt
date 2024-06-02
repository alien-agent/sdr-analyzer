package com.example.sdr_analyzer.devices

import com.example.sdr_analyzer.data.model.Frequency
import com.example.sdr_analyzer.data.model.MHz
import com.example.sdr_analyzer.data.model.SignalData
import com.example.sdr_analyzer.data.model.toMHz
import kotlinx.coroutines.delay
import kotlin.math.absoluteValue
import kotlin.math.exp
import kotlin.random.Random

class DemoDevice : IDevice {
    override val deviceName: String = "Demo Device"

    override val maxFrequency: Frequency = 6200 * MHz
    override val minFrequency: Frequency = 35 * MHz
    override val maxFrequencyRange: Frequency = 6200 * MHz

    override var frequencyStep: Frequency = 100000.0f
    override var centerFrequency: Frequency = 105 * MHz
        set(value) {
            field =
                value.coerceIn(minFrequency + frequencyRange / 2, maxFrequency - frequencyRange / 2)
        }

    override var frequencyRange: Frequency = 100 * MHz
        set(value) {
            field = value.coerceIn(1.0f, maxFrequencyRange)
            centerFrequency = centerFrequency.coerceIn(
                minFrequency + frequencyRange / 2,
                maxFrequency - frequencyRange / 2
            )
        }

    override suspend fun getAmplitudes(): List<SignalData> {
        delay(30)
        return generateSampleData(startFrequency, endFrequency)
    }
}

fun generateSampleData(
    startFrequency: Float = 100.0f,
    endFrequency: Float = 200.0f
): List<SignalData> {
    val data = mutableListOf<SignalData>()
    val step = (endFrequency - startFrequency) / 300

    var frequency = startFrequency
    while (frequency <= endFrequency) {
        var signalStrength = -100 + Random.nextFloat() * 5
        val diff = (frequency-closestCenterFrequency(frequency)).absoluteValue
        if (diff < endFrequency - startFrequency / 10) {
            signalStrength += Random.nextFloat().coerceIn(0.3f, 1f) * 50 * exp(-diff.toMHz().toDouble() / 6).toFloat()
        }

        data.add(SignalData(frequency = frequency, amplitude = signalStrength))
        frequency += step
    }
    return data
}

fun closestCenterFrequency(value: Frequency): Frequency{
    val centerFrequencies = listOf(
        100* MHz,
        150* MHz,
        200* MHz,
        250* MHz,
        450* MHz,
        600* MHz,
        750* MHz,
        1000* MHz,
        1200* MHz,
    )

    return centerFrequencies.withIndex().minBy { (value-it.value).absoluteValue }.value
}