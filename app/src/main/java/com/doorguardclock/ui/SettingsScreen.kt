package com.doorguardclock.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.doorguardclock.data.IoTService
import com.doorguardclock.ui.theme.colorPresets
import com.doorguardclock.ui.theme.toArgbInt
import com.doorguardclock.viewmodel.ClockViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: ClockViewModel,
    onNavigateBack: () -> Unit
) {
    val settings by viewModel.settings.collectAsState()
    val isConnected by viewModel.isConnected.collectAsState()
    val connectionError by viewModel.connectionError.collectAsState()
    val doorStatus by viewModel.doorStatus.collectAsState()

    var autoBrightness by remember { mutableStateOf(settings.autoBrightness) }
    var manualBrightness by remember { mutableFloatStateOf(settings.manualBrightness.toFloat()) }

    var backgroundColor by remember { mutableStateOf(Color(settings.backgroundColor)) }
    var textColor by remember { mutableStateOf(Color(settings.textColor)) }
    var alertTextColor by remember { mutableStateOf(Color(settings.alertTextColor)) }

    var iotUrl by remember { mutableStateOf(settings.iotUrl) }
    var pollingInterval by remember { mutableStateOf(settings.pollingInterval.toString()) }

    // Font size settings
    var portraitTimeSize by remember { mutableStateOf(settings.portraitTimeSize.toString()) }
    var portraitDateSize by remember { mutableStateOf(settings.portraitDateSize.toString()) }
    var landscapeTimeSize by remember { mutableStateOf(settings.landscapeTimeSize.toString()) }
    var landscapeDateSize by remember { mutableStateOf(settings.landscapeDateSize.toString()) }

    var showBackgroundPicker by remember { mutableStateOf(false) }
    var showTextColorPicker by remember { mutableStateOf(false) }
    var showAlertColorPicker by remember { mutableStateOf(false) }

    // Sync with stored settings
    LaunchedEffect(settings) {
        autoBrightness = settings.autoBrightness
        manualBrightness = settings.manualBrightness.toFloat()
        backgroundColor = Color(settings.backgroundColor)
        textColor = Color(settings.textColor)
        alertTextColor = Color(settings.alertTextColor)
        iotUrl = settings.iotUrl
        pollingInterval = settings.pollingInterval.toString()
        portraitTimeSize = settings.portraitTimeSize.toString()
        portraitDateSize = settings.portraitDateSize.toString()
        landscapeTimeSize = settings.landscapeTimeSize.toString()
        landscapeDateSize = settings.landscapeDateSize.toString()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("設定") },
                navigationIcon = {
                    TextButton(onClick = {
                        viewModel.updateBrightness(autoBrightness, manualBrightness.toInt())
                        viewModel.updateColors(
                            backgroundColor.toArgbInt(),
                            textColor.toArgbInt(),
                            alertTextColor.toArgbInt()
                        )
                        viewModel.updateIoTSettings(iotUrl, pollingInterval.toIntOrNull() ?: 5)
                        viewModel.updateFontSizes(
                            portraitTimeSize.toFloatOrNull() ?: 54.6f,
                            portraitDateSize.toFloatOrNull() ?: 8f,
                            landscapeTimeSize.toFloatOrNull() ?: 54.6f,
                            landscapeDateSize.toFloatOrNull() ?: 8f
                        )
                        onNavigateBack()
                    }) {
                        Text("返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // === Brightness Section ===
            Text(
                text = "亮度設定",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("自動亮度", modifier = Modifier.weight(1f))
                Switch(
                    checked = autoBrightness,
                    onCheckedChange = { autoBrightness = it }
                )
            }

            if (!autoBrightness) {
                Text("手動亮度: ${manualBrightness.toInt()}")
                Slider(
                    value = manualBrightness,
                    onValueChange = { manualBrightness = it },
                    valueRange = 1f..255f,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            // === Color Section ===
            Text(
                text = "盤面顏色",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            ColorRow(
                label = "背景顏色",
                color = backgroundColor,
                onClick = { showBackgroundPicker = true }
            )

            ColorRow(
                label = "字體顏色（正常）",
                color = textColor,
                onClick = { showTextColorPicker = true }
            )

            ColorRow(
                label = "字體顏色（門窗開啟）",
                color = alertTextColor,
                onClick = { showAlertColorPicker = true }
            )

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            // === Font Size Section ===
            Text(
                text = "字體大小（%）",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Text(
                text = "直式模式",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = portraitTimeSize,
                    onValueChange = { portraitTimeSize = it },
                    label = { Text("時間") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                OutlinedTextField(
                    value = portraitDateSize,
                    onValueChange = { portraitDateSize = it },
                    label = { Text("日期") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }

            Text(
                text = "橫式模式",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = landscapeTimeSize,
                    onValueChange = { landscapeTimeSize = it },
                    label = { Text("時間") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                OutlinedTextField(
                    value = landscapeDateSize,
                    onValueChange = { landscapeDateSize = it },
                    label = { Text("日期") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            // === IoT Section ===
            Text(
                text = "IoT 連線設定",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            OutlinedTextField(
                value = iotUrl,
                onValueChange = { iotUrl = it },
                label = { Text("IoT  URL") },
                placeholder = { Text("http://192.168.1.100/status") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = pollingInterval,
                onValueChange = { pollingInterval = it },
                label = { Text("輪詢間隔（秒）") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        viewModel.updateIoTSettings(iotUrl, pollingInterval.toIntOrNull() ?: 5)
                        viewModel.refreshDoorStatus()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("測試連線")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Connection status
            val statusText = when {
                iotUrl.isBlank() -> "尚未設定 URL"
                isConnected -> "連線成功"
                connectionError != null -> "連線失敗: $connectionError"
                else -> "連線中..."
            }

            val statusColor = when {
                iotUrl.isBlank() -> Color.Gray
                isConnected -> Color.Green
                connectionError != null -> Color.Red
                else -> Color.Yellow
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = statusText,
                    color = statusColor,
                    fontSize = 14.sp
                )
            }

            // Door status display
            if (isConnected) {
                val doorStatusStr = when (doorStatus) {
                    IoTService.DoorStatus.OPEN -> "門窗狀態: 開啟"
                    IoTService.DoorStatus.CLOSED -> "門窗狀態: 關閉"
                    else -> "門窗狀態: 未知"
                }
                Text(
                    text = doorStatusStr,
                    modifier = Modifier.padding(top = 4.dp),
                    fontSize = 16.sp
                )
            }
        }
    }

    // Color picker dialogs
    if (showBackgroundPicker) {
        ColorPickerDialog(
            title = "選擇背景顏色",
            currentColor = backgroundColor,
            onColorSelected = { backgroundColor = it },
            onDismiss = { showBackgroundPicker = false }
        )
    }

    if (showTextColorPicker) {
        ColorPickerDialog(
            title = "選擇正常字體顏色",
            currentColor = textColor,
            onColorSelected = { textColor = it },
            onDismiss = { showTextColorPicker = false }
        )
    }

    if (showAlertColorPicker) {
        ColorPickerDialog(
            title = "選擇警示字體顏色",
            currentColor = alertTextColor,
            onColorSelected = { alertTextColor = it },
            onDismiss = { showAlertColorPicker = false }
        )
    }
}

@Composable
private fun ColorRow(
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f)
        )
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color)
                .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
        )
    }
}

@Composable
private fun ColorPickerDialog(
    title: String,
    currentColor: Color,
    onColorSelected: (Color) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(colorPresets) { color ->
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(color)
                            .border(
                                width = if (color == currentColor) 3.dp else 1.dp,
                                color = if (color == currentColor) Color.Blue else Color.Gray,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                onColorSelected(color)
                                onDismiss()
                            }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}