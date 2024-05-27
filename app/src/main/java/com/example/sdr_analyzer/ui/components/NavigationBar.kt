package com.example.sdr_analyzer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NavigationBar(
    modifier: Modifier = Modifier,
    onSettingsClick: () -> Unit,
    connectionState: ConnectionState,
    startFrequency: Float,
    endFrequency: Float
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(MaterialTheme.colorScheme.primary),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        ConnectionStatus(state = connectionState, modifier = Modifier.padding(start = 4.dp))

        Spacer(modifier = Modifier.weight(1f))

        FrequencyRangeDisplay(
            startFrequency = startFrequency,
            endFrequency = endFrequency,
            modifier = Modifier.padding(end = 4.dp)
        )

        IconButton(onClick = onSettingsClick) {
            Icon(
                imageVector = Icons.Filled.Settings,
                contentDescription = "Settings",
                tint = Color.White
            )
        }
    }
}

@Composable
fun FrequencyRangeDisplay(startFrequency: Float, endFrequency: Float, modifier: Modifier = Modifier) {
    val startFrequencyText = if (startFrequency >= 1000) {
        "%.1f MHz".format(startFrequency / 1000)
    } else {
        "%.0f Hz".format(startFrequency)
    }

    val endFrequencyText = if (endFrequency >= 1000) {
        "%.1f MHz".format(endFrequency / 1000)
    } else {
        "%.0f Hz".format(endFrequency)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .background(MaterialTheme.colorScheme.background, shape = MaterialTheme.shapes.small)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = "$startFrequencyText - $endFrequencyText",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 16.sp
        )
    }
}
