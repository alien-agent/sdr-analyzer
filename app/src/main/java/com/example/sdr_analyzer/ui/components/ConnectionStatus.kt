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
import com.example.sdr_analyzer.data.model.ConnectionStatus

@Composable
fun ConnectionStatus(status: ConnectionStatus, deviceName: String?, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .padding(8.dp)
            .background(
                color = when (status) {
                    ConnectionStatus.Connected -> Color(0xFF4CAF50)
                    ConnectionStatus.Waiting -> Color(0xFF2196F3)
                    ConnectionStatus.Failed -> Color(0xFFF44336)
                },
                shape = MaterialTheme.shapes.small
            )
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = when (status) {
                ConnectionStatus.Connected -> Icons.Filled.CheckCircle
                ConnectionStatus.Failed -> Icons.Filled.Error
                ConnectionStatus.Waiting -> Icons.Filled.Sync
            },
            contentDescription = "Статус",
            tint = Color.White,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = when (status) {
                ConnectionStatus.Connected -> deviceName ?: "Подключено"
                ConnectionStatus.Waiting -> "Ожидание"
                ConnectionStatus.Failed -> "Ошибка"
            },
            color = Color.White,
            fontSize = 16.sp
        )
    }
}
