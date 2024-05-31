package com.example.sdr_analyzer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.sdr_analyzer.application.AnalyzerApp
import com.example.sdr_analyzer.ui.screens.MainScreen
import com.example.sdr_analyzer.ui.theme.SdranalyzerTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = AnalyzerApp(applicationContext)
        setContent {
            SdranalyzerTheme {
                MainScreen(app = app)
            }
        }
    }
}
