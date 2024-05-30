package com.example.sdr_analyzer.data.model

typealias Frequency = Float

const val Hz: Frequency = 1.0F
const val kHz: Frequency = 1000.0F
const val MHz: Frequency = 1000000.0F
const val GHz: Frequency = 1000000000.0F

fun Frequency.toText(): String {
    return when {
        this >= GHz -> String.format("%.2f GHz", this / GHz)
        this >= MHz -> String.format("%.2f MHz", this / MHz)
        this >= kHz -> String.format("%.2f kHz", this / kHz)
        else -> String.format("%.0f Hz", this)
    }
}

fun Frequency.toTextWithoutUnit(): String {
    return when {
        this >= GHz -> String.format("%.2f", this / GHz)
        this >= MHz -> String.format("%.2f", this / MHz)
        this >= kHz -> String.format("%.2f", this / kHz)
        else -> String.format("%.0f", this)
    }
}

fun Frequency.unitToText(): String {
    return when {
        this >= GHz -> "ГГц"
        this >= MHz -> "МГц"
        this >= kHz -> "КГц"
        else -> "Гц"
    }
}

fun Frequency.unitValue(): Frequency {
    return when {
        this >= GHz -> GHz
        this >= MHz -> MHz
        this >= kHz -> kHz
        else -> Hz
    }
}

fun Frequency.toMHz(): Frequency{
    return this/ MHz
}