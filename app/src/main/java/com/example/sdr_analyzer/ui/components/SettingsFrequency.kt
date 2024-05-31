package com.example.sdr_analyzer.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.sdr_analyzer.application.AnalyzerApp
import com.example.sdr_analyzer.data.model.Frequency
import com.example.sdr_analyzer.data.model.MHz
import com.example.sdr_analyzer.data.model.toMHz
import com.example.sdr_analyzer.devices.IDevice

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FrequencySettings(device: IDevice) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        FrequencyField(label = "Центр", value = device.centerFrequency, onValueChange = {freq -> device.centerFrequency = freq}, focusManager = focusManager)
        Spacer(modifier = Modifier.height(8.dp))
        FrequencyField(label = "Старт", value = device.startFrequency, onValueChange = {freq -> device.startFrequency = freq}, focusManager = focusManager)
        Spacer(modifier = Modifier.height(8.dp))
        FrequencyField(label = "Стоп", value = device.endFrequency, onValueChange = {freq -> device.endFrequency = freq}, focusManager = focusManager)
        Spacer(modifier = Modifier.height(8.dp))
        FrequencyField(label = "Полоса", value = device.frequencyRange, onValueChange = {freq -> device.frequencyRange = freq}, focusManager = focusManager)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FrequencyField(label: String, value: Frequency, onValueChange: (Frequency) -> Unit, focusManager: FocusManager){
    TextField(
        value = value.toMHz().toString(),
        onValueChange = { newValue ->
            val valueDouble = newValue.toDoubleOrNull()
            if (valueDouble != null) {
                onValueChange(newValue.toDouble().toFloat() * MHz)
            }
        },
        label = { Text(label) },
        visualTransformation = SuffixTransformer(" МГц"),
        keyboardActions = KeyboardActions(onDone = {
            focusManager.clearFocus()
        }),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction = ImeAction.Done,
        ),
        modifier = Modifier.fillMaxWidth(1.0f)
    )
}

class SuffixTransformer(private val suffix: String) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val transformedText = text.text + suffix
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                return offset
            }

            override fun transformedToOriginal(offset: Int): Int {
                return when {
                    offset > text.length -> text.length
                    else -> offset
                }
            }
        }

        return TransformedText(AnnotatedString(transformedText), offsetMapping)
    }
}
