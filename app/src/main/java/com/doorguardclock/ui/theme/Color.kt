package com.doorguardclock.ui.theme

import androidx.compose.ui.graphics.Color

// Default colors
val DefaultBackground = Color(0xFF000000)  // Black
val DefaultTextColor = Color(0xFFFFFFFF)    // White
val AlertTextColor = Color(0xFFFF0000)     // Red

// Color picker presets
val colorPresets = listOf(
    Color(0xFF000000), // Black
    Color(0xFFFFFFFF), // White
    Color(0xFFFF0000), // Red
    Color(0xFF00FF00), // Green
    Color(0xFF0000FF), // Blue
    Color(0xFFFFFF00), // Yellow
    Color(0xFFFF00FF), // Magenta
    Color(0xFF00FFFF), // Cyan
    Color(0xFFFFA500), // Orange
    Color(0xFF800080), // Purple
    Color(0xFF808080), // Gray
    Color(0xFFA52A2A), // Brown
)

fun Int.toComposeColor(): Color = Color(this)

fun Color.toArgbInt(): Int {
    val red = (this.red * 255).toInt()
    val green = (this.green * 255).toInt()
    val blue = (this.blue * 255).toInt()
    val alpha = (this.alpha * 255).toInt()
    return (alpha shl 24) or (red shl 16) or (green shl 8) or blue
}