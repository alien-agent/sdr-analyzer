package com.example.sdr_analyzer.devices

import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbEndpoint
import com.example.sdr_analyzer.data.model.Frequency
import com.example.sdr_analyzer.data.model.MHz
import com.example.sdr_analyzer.data.model.SignalData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ArinstSSA(private val device: UsbDevice, private val connection: UsbDeviceConnection) :
    IDevice {
    override val deviceName: String = "Arinst SSA-TG"
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

    companion object {
        private const val COMMAND_TERMINATE = "\r\n"
    }

    private var packageIndex = 0

    private val inEndpoint: UsbEndpoint
    private val outEndpoint: UsbEndpoint

    init {
        val usbInterface = device.getInterface(1)
        connection.claimInterface(usbInterface, true)

        var inEndpoint: UsbEndpoint? = null
        var outEndpoint: UsbEndpoint? = null

        for (i in 0 until usbInterface.endpointCount) {
            val endpoint = usbInterface.getEndpoint(i)
            if (endpoint.type == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                if (endpoint.direction == UsbConstants.USB_DIR_IN) {
                    inEndpoint = endpoint
                } else if (endpoint.direction == UsbConstants.USB_DIR_OUT) {
                    outEndpoint = endpoint
                }
            }
        }

        if (inEndpoint == null || outEndpoint == null) {
            throw IllegalArgumentException("Could not find both IN and OUT bulk endpoints")
        }

        this.inEndpoint = inEndpoint
        this.outEndpoint = outEndpoint
    }

    private suspend fun sendCommand(command: String, vararg args: Any): String =
        withContext(Dispatchers.IO) {
            val msg = buildString {
                append(command)
                args.forEach { append(" ").append(it) }
                append(" ").append(packageIndex).append(COMMAND_TERMINATE)
            }
            connection.bulkTransfer(outEndpoint, msg.toByteArray(), msg.length, 1000)
            packageIndex++
            readResponse(command)
        }

    private fun readResponse(command: String): String {
        val buffer = ByteArray(1024)
        var result = ""
        while (true) {
            val bytesRead = connection.bulkTransfer(inEndpoint, buffer, buffer.size, 1000)
            if (bytesRead <= 0) break
            result += buffer.copyOf(bytesRead).toString(Charsets.US_ASCII)
            if (result.endsWith(COMMAND_TERMINATE)) break
        }
        return result
    }

    private fun decodeData(
        response: ByteArray,
        startFrequency: Long,
        stepFrequency: Long,
        attenuation: Int
    ): List<SignalData> {
        val amplitudes = mutableListOf<SignalData>()
        for (i in response.indices step 2) {
            // val amplitudeIndex: Int = response[i] & 7 shl 8 or (response[i+1] & 255)
            val amplitudeIndex =
                ((response[i].toInt() and 0x7) shl 8) or (response.getOrElse(i + 1) { 0 }
                    .toInt() and 0xFF)
            val divider = 1
            val attenuation = 0
            val amplitude = (amplitudeIndex / -10.0f) * divider;
            val resultAmplitude = (amplitude + (80 * divider)) - attenuation

            val frequency = startFrequency.toFloat() + (i / 2) * stepFrequency
            amplitudes.add(
                SignalData(
                    frequency,
                    resultAmplitude
                )
            )
        }
        return amplitudes
    }

    override suspend fun getAmplitudes(): List<SignalData>? = withContext(Dispatchers.IO) {
        val attenuation: Int = 0
        val tracking: Boolean = false
        if (attenuation !in -30..0) return@withContext null

        val command = if (tracking) "scn22" else "scn20"
        val adjustedAttenuation = (attenuation * 100) + 10000
        val response =
            sendCommand(
                command,
                startFrequency.toLong(),
                endFrequency.toLong(),
                frequencyStep.toLong(),
                200,
                20,
                10700000,
                adjustedAttenuation
            )
        val processedResponse =
            response.split(COMMAND_TERMINATE).filterNot { it == "" }.map { it.split(' ') }

        if (processedResponse.size != 3 || processedResponse.last()
                .first() != "complete" || processedResponse.first().first() != command
        ) {
            return@withContext emptyList()
        }

        return@withContext removeMostFrequentAmplitude(decodeData(
            processedResponse[1][0].toByteArray(Charsets.US_ASCII),
            startFrequency.toLong(),
            frequencyStep.toLong(),
            adjustedAttenuation
        ))
    }
}


fun removeMostFrequentAmplitude(data: List<SignalData>): List<SignalData> {
    if (data.isEmpty()) return data

    // Группируем по амплитуде и считаем частоту каждой амплитуды
    val amplitudeFrequency = data.groupingBy { it.signalStrength }.eachCount()

    // Находим наиболее часто встречающуюся амплитуду
    val mostFrequentAmplitude = amplitudeFrequency.maxByOrNull { it.value }?.key
    val minAmplitude = data.minBy { it.signalStrength }.signalStrength

    // Возвращаем новый список, исключая значения с наиболее часто встречающейся амплитудой
    return if (mostFrequentAmplitude != null) {
        data.map {
            if (it.signalStrength == mostFrequentAmplitude) {
                 SignalData(frequency = it.frequency, signalStrength = minAmplitude)
            }
            it
        }
    } else {
        data
    }
}
