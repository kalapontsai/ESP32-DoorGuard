package com.doorguardclock.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.doorguardclock.viewmodel.ClockViewModel

@Composable
fun ClockScreen(
    viewModel: ClockViewModel,
    onNavigateToSettings: () -> Unit
) {
    val currentTime by viewModel.currentTime.collectAsState()
    val currentDate by viewModel.currentDate.collectAsState()
    val settings by viewModel.settings.collectAsState()

    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val screenHeightDp = configuration.screenHeightDp
    val isLandscape = screenWidthDp > screenHeightDp

    // Use the dimension that corresponds to the text layout direction
    val textAreaDimension = if (isLandscape) screenHeightDp else screenWidthDp

    // Get font sizes from settings (stored as percentages, e.g., 54.6 = 0.546)
    val timeFontSize = if (isLandscape) {
        (textAreaDimension * (settings.landscapeTimeSize / 100f)).toInt().sp
    } else {
        (textAreaDimension * (settings.portraitTimeSize / 100f)).toInt().sp
    }

    val dateFontSize = if (isLandscape) {
        (textAreaDimension * (settings.landscapeDateSize / 100f)).toInt().sp
    } else {
        (textAreaDimension * (settings.portraitDateSize / 100f)).toInt().sp
    }

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
                fontSize = timeFontSize,
                fontWeight = FontWeight.Bold,
                color = textColor,
                textAlign = TextAlign.Center,
                lineHeight = timeFontSize * 1.1f
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Date display
            Text(
                text = currentDate,
                fontSize = dateFontSize,
                fontWeight = FontWeight.Normal,
                color = textColor.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}