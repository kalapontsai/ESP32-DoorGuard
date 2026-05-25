package com.doorguardclock.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class IoTService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    enum class DoorStatus {
        OPEN,
        CLOSED,
        UNKNOWN
    }

    data class DoorState(
        val status: DoorStatus = DoorStatus.UNKNOWN,
        val isConnected: Boolean = false,
        val errorMessage: String? = null
    )

    /**
     * 發送 HTTP GET 至指定 URL，回應 body 解析 door 狀態
     * 回應包含 "open" → OPEN
     * 回應包含 "closed" → CLOSED
     * 其餘 → UNKNOWN
     */
    suspend fun checkDoorStatus(url: String): DoorState = withContext(Dispatchers.IO) {
        if (url.isBlank()) {
            return@withContext DoorState(
                status = DoorStatus.UNKNOWN,
                isConnected = false,
                errorMessage = "URL 未設定"
            )
        }

        try {
            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                val body = response.body?.string()?.lowercase() ?: ""
                val status = when {
                    body.contains("\"door\"") && body.contains("\"open\"") -> DoorStatus.OPEN
                    body.contains("\"door\"") && body.contains("\"closed\"") -> DoorStatus.CLOSED
                    body.contains("open") -> DoorStatus.OPEN
                    body.contains("closed") -> DoorStatus.CLOSED
                    else -> DoorStatus.UNKNOWN
                }
                DoorState(status = status, isConnected = true)
            } else {
                DoorState(
                    status = DoorStatus.UNKNOWN,
                    isConnected = false,
                    errorMessage = "HTTP ${response.code}"
                )
            }
        } catch (e: Exception) {
            DoorState(
                status = DoorStatus.UNKNOWN,
                isConnected = false,
                errorMessage = e.message ?: "連線失敗"
            )
        }
    }
}