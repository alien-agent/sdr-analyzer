package com.example.sdr_analyzer.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class ConnectionState {
    Connected, Disconnected, Connecting
}

@Composable
fun ConnectionStatus(state: ConnectionState, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition()

    val alpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Row(
        modifier = modifier
            .padding(8.dp)
            .background(
                color = when (state) {
                    ConnectionState.Connected -> Color(0xFF4CAF50)
                    ConnectionState.Disconnected -> Color(0xFFF44336)
                    ConnectionState.Connecting -> Color(0xFFFFC107)
                },
                shape = MaterialTheme.shapes.small
            )
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = when (state) {
                ConnectionState.Connected -> Icons.Filled.CheckCircle
                ConnectionState.Disconnected -> Icons.Filled.Error
                ConnectionState.Connecting -> Icons.Filled.Sync
            },
            contentDescription = when (state) {
                ConnectionState.Connected -> "Connected"
                ConnectionState.Disconnected -> "Disconnected"
                ConnectionState.Connecting -> "Connecting"
            },
            tint = Color.White,
            modifier = if (state == ConnectionState.Connecting) Modifier.alpha(alpha) else Modifier
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = when (state) {
                ConnectionState.Connected -> "Подключено"
                ConnectionState.Disconnected -> "Отключено"
                ConnectionState.Connecting -> "Подключение"
            },
            color = Color.White,
            fontSize = 16.sp
        )
    }
}
