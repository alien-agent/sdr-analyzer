package com.example.sdr_analyzer.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

@Composable
fun MaterialButtonToggleGroup(
    items: List<String>,
    onClick: (index: Int) -> Unit = {}
) {
    val cornerRadius = 8.dp

    val (selectedIndex, onIndexSelected) = remember { mutableStateOf<Int?>(null) }
    Row(
        modifier = Modifier.padding(8.dp)
    ) {
        items.forEachIndexed { index, item ->
            OutlinedButton(
                modifier = when (index) {
                    0 ->
                        Modifier
                            .offset(0.dp, 0.dp)
                            .zIndex(if (selectedIndex == index) 1f else 0f)

                    else ->
                        Modifier
                            .offset((-1 * index).dp, 0.dp)
                            .zIndex(if (selectedIndex == index) 1f else 0f)
                },
                onClick = {
                    onIndexSelected(index)
                    onClick(index)
                },
                shape = when (index) {
                    // left outer button
                    0 -> RoundedCornerShape(
                        topStart = cornerRadius,
                        topEnd = 0.dp,
                        bottomStart = cornerRadius,
                        bottomEnd = 0.dp
                    )
                    // right outer button
                    items.size - 1 -> RoundedCornerShape(
                        topStart = 0.dp,
                        topEnd = cornerRadius,
                        bottomStart = 0.dp,
                        bottomEnd = cornerRadius
                    )
                    // middle button
                    else -> RoundedCornerShape(
                        topStart = 0.dp,
                        topEnd = 0.dp,
                        bottomStart = 0.dp,
                        bottomEnd = 0.dp
                    )
                },
                border = BorderStroke(
                    1.dp, if (selectedIndex == index) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        Color.DarkGray.copy(alpha = 0.75f)
                    }
                ),
                colors = if (selectedIndex == index) {
                    // selected colors
                    ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                } else {
                    // not selected colors
                    ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                },
            ) {
                Text(
                    text = item,
                    color = if (selectedIndex == index) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        Color.DarkGray.copy(alpha = 0.9f)
                    },
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
    }
}