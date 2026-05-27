package com.doorguardclock.viewmodel

import android.app.Application
import android.view.WindowManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.doorguardclock.data.IoTService
import com.doorguardclock.data.SettingsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ClockViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepository = SettingsRepository(application)
    private val iotService = IoTService()

    // Current time
    private val _currentTime = MutableStateFlow("")
    val currentTime: StateFlow<String> = _currentTime.asStateFlow()

    private val _currentDate = MutableStateFlow("")
    val currentDate: StateFlow<String> = _currentDate.asStateFlow()

    // Door status
    private val _doorStatus = MutableStateFlow(IoTService.DoorStatus.UNKNOWN)
    val doorStatus: StateFlow<IoTService.DoorStatus> = _doorStatus.asStateFlow()

    private val _doorStatusText = MutableStateFlow("")
    val doorStatusText: StateFlow<String> = _doorStatusText.asStateFlow()

    // Settings
    val settings: StateFlow<SettingsRepository.AppSettings> = settingsRepository.settingsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, SettingsRepository.AppSettings())

    // Connection status
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _connectionError = MutableStateFlow<String?>(null)
    val connectionError: StateFlow<String?> = _connectionError.asStateFlow()

    private var pollingJob: Job? = null

    init {
        // Start time ticker
        viewModelScope.launch {
            while (true) {
                val now = java.time.LocalDateTime.now()
                _currentTime.value = now.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"))
                _currentDate.value = now.format(java.time.format.DateTimeFormatter.ofPattern("yyyy/MM/dd"))
                delay(1000)
            }
        }

        // Start IoT polling
        viewModelScope.launch {
            settings.collect { s ->
                if (s.iotUrl.isNotBlank()) {
                    startPolling(s.iotUrl, s.pollingInterval)
                } else {
                    stopPolling()
                }
            }
        }
    }

    private fun startPolling(url: String, intervalSeconds: Int) {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (true) {
                val result = iotService.checkDoorStatus(url)
                _doorStatus.value = result.status
                _isConnected.value = result.isConnected
                _connectionError.value = result.errorMessage

                _doorStatusText.value = when (result.status) {
                    IoTService.DoorStatus.OPEN -> "門窗：開啟"
                    IoTService.DoorStatus.CLOSED -> "門窗：關閉"
                    IoTService.DoorStatus.UNKNOWN -> if (result.errorMessage != null) "連線失敗" else ""
                }

                delay(intervalSeconds * 1000L)
            }
        }
    }

    private fun stopPolling() {
        pollingJob?.cancel()
        _doorStatus.value = IoTService.DoorStatus.UNKNOWN
        _isConnected.value = false
        _connectionError.value = null
        _doorStatusText.value = ""
    }

    // Computed text color based on door status
    fun getCurrentTextColor(): Color {
        val s = settings.value
        return if (_doorStatus.value == IoTService.DoorStatus.OPEN) {
            Color(s.alertTextColor)
        } else {
            Color(s.textColor)
        }
    }

    fun getBackgroundColor(): Color = Color(settings.value.backgroundColor)

    // Update functions
    fun updateBrightness(auto: Boolean, manual: Int) {
        viewModelScope.launch {
            settingsRepository.updateBrightness(auto, manual)
        }
    }

    fun updateColors(background: Int, text: Int, alert: Int) {
        viewModelScope.launch {
            settingsRepository.updateColors(background, text, alert)
        }
    }

    fun updateIoTSettings(url: String, interval: Int) {
        viewModelScope.launch {
            settingsRepository.updateIoTSettings(url, interval)
        }
    }

    fun updateFontSizes(portraitTime: Float, portraitDate: Float, landscapeTime: Float, landscapeDate: Float) {
        viewModelScope.launch {
            settingsRepository.updateFontSizes(portraitTime, portraitDate, landscapeTime, landscapeDate)
        }
    }

    // Manual door status check
    fun refreshDoorStatus() {
        val s = settings.value
        if (s.iotUrl.isNotBlank()) {
            viewModelScope.launch {
                val result = iotService.checkDoorStatus(s.iotUrl)
                _doorStatus.value = result.status
                _isConnected.value = result.isConnected
                _connectionError.value = result.errorMessage

                _doorStatusText.value = when (result.status) {
                    IoTService.DoorStatus.OPEN -> "門窗：開啟"
                    IoTService.DoorStatus.CLOSED -> "門窗：關閉"
                    IoTService.DoorStatus.UNKNOWN -> if (result.errorMessage != null) "連線失敗" else ""
                }
            }
        }
    }
}