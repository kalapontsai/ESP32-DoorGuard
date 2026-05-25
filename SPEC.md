# Door Guard Clock - 規格文件

## 1. 專案概述

- **專案名稱**: Door Guard Clock
- **類型**: Android 全螢幕時鐘 App
- **核心功能**: 顯示即時時間，並根據門窗感測器狀態改變字體顏色

## 2. 功能需求

### 2.1 主畫面 - 全螢幕時鐘
- 顯示時:分:秒（HH:MM:SS）
- 顯示日期（YYYY/MM/DD）
- 從手機系統取得時間
- 全螢幕顯示，隱藏狀態列與導航列
- 預設字體顏色：白色
- 預設背景：黑色

### 2.2 設定頁面（Setup）
進入方式：點擊時鐘畫面任意位置（短按進入設定，長按鎖定）

#### 2.2.1 亮度設定
- **自動亮度**：依據室內光線強度自動調整
- **手動固定亮度**：滑桿調整（0-255）

#### 2.2.2 盤面顏色設定
- 背景顏色選擇器
- 字體顏色選擇器（正常狀態）
- 字體顏色選擇器（門窗開啟時）
- 預設值：
  - 背景：黑色 (#000000)
  - 正常字體：白色 (#FFFFFF)
  - 警示字體：紅色 (#FF0000)

#### 2.2.3 IoT 連線設定
- IoT 裝置 IP 位址（例：192.168.1.100）
- IoT 裝置連接埠（預設：80）
- 輪詢間隔（秒）（預設：5）
- 連線測試按鈕
- 顯示目前門窗狀態

## 3. IoT 整合

### 3.1 資料來源
- App 發送 HTTP GET 請求至使用者設定的完整 URL
- URL 格式：`http://{IP}:{PORT}/status` 或任何自訂 endpoint
- 支援解析 JSON 回應或 HTML 回應

### 3.2 Arduino endpoint
在 ESP8266-01 新增 `/status` endpoint，回傳 JSON：
```json
{"door": "open"}   // 門開
{"door": "closed"}  // 門關
```

### 3.3 解析邏輯
- App 收到回應後轉小寫比對
- 含 `"door":"open"` 或純文字含 `open` → 開啟 → 字體變警示色
- 含 `"door":"closed"` 或純文字含 `closed` → 關閉 → 字體恢復正常
- 連線失敗或解析失敗 → 維持目前狀態

## 4. 技術架構

### 4.1 技術棧
- **語言**: Kotlin
- **UI**: Jetpack Compose
- **最小 SDK**: 26 (Android 8.0)
- **目標 SDK**: 34

### 4.2 專案結構
```
app/
├── src/main/
│   ├── java/com/doorguardclock/
│   │   ├── MainActivity.kt          # 主程式進入點
│   │   ├── ui/
│   │   │   ├── ClockScreen.kt      # 時鐘畫面 Composable
│   │   │   ├── SettingsScreen.kt   # 設定頁面 Composable
│   │   │   └── theme/
│   │   │       └── Color.kt        # 顏色定義
│   │   ├── data/
│   │   │   ├── SettingsRepository.kt # 設定資料管理
│   │   │   └── IoTService.kt        # IoT HTTP 請求
│   │   └── viewmodel/
│   │       └── ClockViewModel.kt   # ViewModel
│   └── res/
│       └── values/
│           └── strings.xml
└── build.gradle.kts
```

### 4.3 依賴
- Jetpack Compose BOM 2024.02.00
- Kotlin Coroutines
- ViewModel Compose
- DataStore Preferences（設定儲存）
- OkHttp（網路請求）

## 5. 使用者流程

1. 啟動 App → 全螢幕時鐘顯示
2. 點擊畫面 → 進入設定頁面
3. 設定完成 → 點擊返回 → 時鐘套用新設定
4. 門窗開啟 → 字體變色警示
5. 門窗關閉 → 字體恢復正常