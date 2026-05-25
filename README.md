# Door Guard Clock

Android 全螢幕時鐘 App，可連接 IoT 門窗感測器，門窗開啟時自動改變字體顏色警示。

## 功能

- 全螢幕顯示時鐘（HH:MM:SS + 日期）
- 亮度設定（自動/手動）
- 盤面顏色設定（背景、正常字體、警示字體）
- IoT 連線：HTTP GET 輪詢 ESP8266-01 `/status` endpoint
- 門窗開啟時，字體變警示色

## 技術棧

- Kotlin
- Jetpack Compose
- Material Design 3
- DataStore Preferences
- OkHttp

## 快速開始

1. 用 Android Studio 開啟 `door-guard-clock` 資料夾
2. Sync Gradle
3. Build & Run

## IoT 整合

### App 設定
在設定頁輸入完整 URL，例如：
```
http://192.168.1.100/status
```

### Arduino 端
在 ESP8266-01 加入 `/status` endpoint（已含於 `arduino/esp8266-01.ino`）：
```cpp
void handleStatus() {
  String doorStatus = getDoorStatusString(); // "open" or "closed"
  String response = "{\"door\": \"" + doorStatus + "\"}";
  server.send(200, "application/json", response);
}
```
並在 setup() 註冊：
```cpp
server.on("/status", HTTP_GET, handleStatus);
```

### 解析邏輯
- 回應含 `open`（不区分大小写）→ 字體變警示色
- 回應含 `closed`（不区分大小写）→ 字體恢復正常
- 輪詢間隔可自訂（預設 5 秒）