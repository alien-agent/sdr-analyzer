package com.example.sdr_analyzer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.sdr_analyzer.manager.DeviceManager
import com.example.sdr_analyzer.ui.screens.MainScreen
import com.example.sdr_analyzer.ui.theme.SdranalyzerTheme

class MainActivity : ComponentActivity() {
    private lateinit var deviceManager: DeviceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val deviceManager = DeviceManager(applicationContext)

        setContent {
            SdranalyzerTheme {
                MainScreen(deviceManager = deviceManager)
            }
        }
    }
}
