package com.doorguardclock.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.doorguardclock.data.IoTService
import com.doorguardclock.viewmodel.ClockViewModel

@Composable
fun ClockScreen(
    viewModel: ClockViewModel,
    onNavigateToSettings: () -> Unit
) {
    val currentTime by viewModel.currentTime.collectAsState()
    val currentDate by viewModel.currentDate.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val doorStatus by viewModel.doorStatus.collectAsState()
    val doorStatusText by viewModel.doorStatusText.collectAsState()

    val backgroundColor = Color(settings.backgroundColor)
    val textColor = viewModel.getCurrentTextColor()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .clickable { onNavigateToSettings() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Time display
            Text(
                text = currentTime,
                fontSize = 96.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Date display
            Text(
                text = currentDate,
                fontSize = 36.sp,
                fontWeight = FontWeight.Normal,
                color = textColor.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Door status indicator
            if (doorStatusText.isNotEmpty()) {
                val statusColor = when (doorStatus) {
                    IoTService.DoorStatus.OPEN -> Color(0xFFFF0000)
                    IoTService.DoorStatus.CLOSED -> Color(0xFF00FF00)
                    else -> textColor.copy(alpha = 0.6f)
                }

                Text(
                    text = doorStatusText,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    color = statusColor
                )
            }
        }
    }
}