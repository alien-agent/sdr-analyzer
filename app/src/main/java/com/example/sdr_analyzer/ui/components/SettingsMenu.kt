package com.example.sdr_analyzer.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sdr_analyzer.application.AnalyzerApp
import kotlin.math.roundToInt

enum class MenusList(val title: String) {
    Frequency("Частота"),
    Other("Прочее")
}

@Composable
fun SettingsMenu(isShown: Boolean, app: AnalyzerApp) {
    var selectedMenu: MenusList? by remember { mutableStateOf(null) }
    val app by remember {
        mutableStateOf(app)
    }

    AnimatedVisibility(
        visible = isShown,
        enter = expandVertically(
            spring(
                stiffness = Spring.StiffnessLow,
                visibilityThreshold = IntSize.VisibilityThreshold
            ),
        ),
        exit = shrinkVertically(),
    ) {
        if (selectedMenu == null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                MenusList.values().map { it ->
                    Button(
                        onClick = { selectedMenu = it },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = when (it) {
                            MenusList.Frequency -> app.connectedDevice != null
                            else -> true
                        },
                    ) {
                        Text(it.title)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical=8.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    IconButton(onClick = { selectedMenu = null }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = null,
                        )
                    }
                    Text(
                        selectedMenu!!.title,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 10.dp)
                    )
                }
                when (selectedMenu) {
                    MenusList.Frequency -> FrequencySettings(
                        device = app.connectedDevice!!
                    )

                    MenusList.Other -> OtherMenu(
                        app = app
                    )

                    else -> null
                }
            }
        }
    }
}

@Composable
fun OtherMenu(app: AnalyzerApp) {
    SwitchWithLabel(
        label = "Оверлей частот",
        state = app.isFreqOverlayShown,
        onStateChange = {
            app.isFreqOverlayShown = it
        }
    )
    Spacer(Modifier.height(4.dp))
    SwitchWithLabel(
        label = "Демонстрационный режим",
        state = app.isDemoMode,
        onStateChange = {
            app.isDemoMode = it
        }
    )
}

@Composable
private fun SwitchWithLabel(label: String, state: Boolean, onStateChange: (Boolean) -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .clickable(
                interactionSource = interactionSource,
                // This is for removing ripple when Row is clicked
                indication = null,
                role = Role.Switch,
                onClick = {
                    onStateChange(!state)
                }
            ),
        verticalAlignment = Alignment.CenterVertically

    ) {
        Switch(
            checked = state,
            onCheckedChange = {
                onStateChange(it)
            }
        )
        Spacer(modifier = Modifier.padding(start = 8.dp))
        Text(text = label, fontSize = 16.sp)
    }
}
