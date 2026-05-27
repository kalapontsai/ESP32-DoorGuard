package com.doorguardclock.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    companion object {
        // Brightness
        val AUTO_BRIGHTNESS = booleanPreferencesKey("auto_brightness")
        val MANUAL_BRIGHTNESS = intPreferencesKey("manual_brightness")

        // Colors
        val BACKGROUND_COLOR = intPreferencesKey("background_color")
        val TEXT_COLOR = intPreferencesKey("text_color")
        val ALERT_TEXT_COLOR = intPreferencesKey("alert_text_color")

        // IoT Settings
        val IOT_URL = stringPreferencesKey("iot_url")
        val POLLING_INTERVAL = intPreferencesKey("polling_interval")

        // Font sizes (as percentage multipliers, 100 = 1.0x)
        val PORTRAIT_TIME_SIZE = floatPreferencesKey("portrait_time_size")
        val PORTRAIT_DATE_SIZE = floatPreferencesKey("portrait_date_size")
        val LANDSCAPE_TIME_SIZE = floatPreferencesKey("landscape_time_size")
        val LANDSCAPE_DATE_SIZE = floatPreferencesKey("landscape_date_size")
    }

    data class AppSettings(
        val autoBrightness: Boolean = true,
        val manualBrightness: Int = 128,
        val backgroundColor: Int = 0xFF000000.toInt(),
        val textColor: Int = 0xFFFFFFFF.toInt(),
        val alertTextColor: Int = 0xFFFF0000.toInt(),
        val iotUrl: String = "",
        val pollingInterval: Int = 5,
        val portraitTimeSize: Float = 54.6f,  // 0.546 as percentage
        val portraitDateSize: Float = 8f,     // 0.08 as percentage
        val landscapeTimeSize: Float = 54.6f,
        val landscapeDateSize: Float = 8f
    )

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            autoBrightness = prefs[AUTO_BRIGHTNESS] ?: true,
            manualBrightness = prefs[MANUAL_BRIGHTNESS] ?: 128,
            backgroundColor = prefs[BACKGROUND_COLOR] ?: 0xFF000000.toInt(),
            textColor = prefs[TEXT_COLOR] ?: 0xFFFFFFFF.toInt(),
            alertTextColor = prefs[ALERT_TEXT_COLOR] ?: 0xFFFF0000.toInt(),
            iotUrl = prefs[IOT_URL] ?: "",
            pollingInterval = prefs[POLLING_INTERVAL] ?: 5,
            portraitTimeSize = prefs[PORTRAIT_TIME_SIZE] ?: 54.6f,
            portraitDateSize = prefs[PORTRAIT_DATE_SIZE] ?: 8f,
            landscapeTimeSize = prefs[LANDSCAPE_TIME_SIZE] ?: 54.6f,
            landscapeDateSize = prefs[LANDSCAPE_DATE_SIZE] ?: 8f
        )
    }

    suspend fun updateBrightness(auto: Boolean, manual: Int) {
        context.dataStore.edit { prefs ->
            prefs[AUTO_BRIGHTNESS] = auto
            prefs[MANUAL_BRIGHTNESS] = manual
        }
    }

    suspend fun updateColors(background: Int, text: Int, alert: Int) {
        context.dataStore.edit { prefs ->
            prefs[BACKGROUND_COLOR] = background
            prefs[TEXT_COLOR] = text
            prefs[ALERT_TEXT_COLOR] = alert
        }
    }

    suspend fun updateIoTSettings(url: String, interval: Int) {
        context.dataStore.edit { prefs ->
            prefs[IOT_URL] = url
            prefs[POLLING_INTERVAL] = interval
        }
    }

    suspend fun updateFontSizes(portraitTime: Float, portraitDate: Float, landscapeTime: Float, landscapeDate: Float) {
        context.dataStore.edit { prefs ->
            prefs[PORTRAIT_TIME_SIZE] = portraitTime
            prefs[PORTRAIT_DATE_SIZE] = portraitDate
            prefs[LANDSCAPE_TIME_SIZE] = landscapeTime
            prefs[LANDSCAPE_DATE_SIZE] = landscapeDate
        }
    }
}