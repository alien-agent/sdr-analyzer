package com.example.sdr_analyzer.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsMenu(onMenuSelect: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Button(onClick = { onMenuSelect("Frequency") }, modifier = Modifier.fillMaxWidth()) {
            Text("Frequency Settings", )
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}
