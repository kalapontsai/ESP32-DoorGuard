#include <ESP8266WiFi.h>
#include <ESP8266WebServer.h>
#include <WiFiUdp.h>
#include <NTPClient.h>
#include <EEPROM.h>

#define EEPROM_SIZE 192
#define SSID_ADDR 0
#define PASS_ADDR 32
#define FLAG_ADDR 95
#define TIMEMODE_ADDR 96
#define EMAIL_ADDR 128
#define EMAIL_MAXLEN 63

#define GPIO_2 2 // GPIO2 for onboard LED for DOOR sensor
bool door_open = false; // 初始狀態為關閉

ESP8266WebServer server(80);
WiFiUDP ntpUDP;
NTPClient timeClient(ntpUDP, "pool.ntp.org", 8 * 3600, 60000);

String wifiSSID = "";
String wifiPASS = "";
bool isConnected = false;

const char *apSSID = "ESP-Setup";
const char *apPassword = "12345678";

unsigned long lastReconnectAttempt = 0;
const unsigned long reconnectInterval = 10000; // 每 10 秒嘗試一次

// 新增全域變數儲存時間設定
String timeMode = "Full-Time"; // 預設值
// 新增全域變數儲存郵件通知名單
String alertEmails = ""; // 預設為空

// 儲存時間設定與郵件通知到 EEPROM
void saveSettings() {
  EEPROM.begin(EEPROM_SIZE);
  // 儲存 timeMode
  for (int i = 0; i < 31; i++) {
    EEPROM.write(TIMEMODE_ADDR + i, (i < timeMode.length()) ? timeMode[i] : 0);
  }
  // 儲存 alertEmails
  for (int i = 0; i < EMAIL_MAXLEN; i++) {
    EEPROM.write(EMAIL_ADDR + i, (i < alertEmails.length()) ? alertEmails[i] : 0);
  }
  EEPROM.commit();
  EEPROM.end();
}

// 讀取時間設定與郵件通知
void loadSettings() {
  EEPROM.begin(EEPROM_SIZE);
  char tm[32], em[EMAIL_MAXLEN + 1];
  for (int i = 0; i < 31; i++) tm[i] = EEPROM.read(TIMEMODE_ADDR + i);
  tm[31] = 0;
  for (int i = 0; i < EMAIL_MAXLEN; i++) em[i] = EEPROM.read(EMAIL_ADDR + i);
  em[EMAIL_MAXLEN] = 0;
  if (tm[0] != 0) timeMode = String(tm);
  if (em[0] != 0) alertEmails = String(em);
  EEPROM.end();
}

void saveWiFiInfo(String ssid, String pass) {
  EEPROM.begin(EEPROM_SIZE);
  for (int i = 0; i < 32; i++) {
    EEPROM.write(SSID_ADDR + i, (i < ssid.length()) ? ssid[i] : 0);
    EEPROM.write(PASS_ADDR + i, (i < pass.length()) ? pass[i] : 0);
  }
  EEPROM.write(FLAG_ADDR, 1);
  EEPROM.commit();
  EEPROM.end();
}

bool loadWiFiInfo() {
  EEPROM.begin(EEPROM_SIZE);
  if (EEPROM.read(FLAG_ADDR) != 1) {
    EEPROM.end();
    return false;
  }
  char ssid[33], pass[33];
  for (int i = 0; i < 32; i++) {
    ssid[i] = EEPROM.read(SSID_ADDR + i);
    pass[i] = EEPROM.read(PASS_ADDR + i);
  }
  ssid[32] = 0;
  pass[32] = 0;
  wifiSSID = String(ssid);
  wifiPASS = String(pass);
  EEPROM.end();
  return true;
}

void clearWiFiInfo() {
  EEPROM.begin(EEPROM_SIZE);
  EEPROM.write(FLAG_ADDR, 0);
  EEPROM.commit();
  EEPROM.end();
}

String getDateTime() {
  time_t rawTime = timeClient.getEpochTime();
  struct tm *timeinfo = localtime(&rawTime);
  char buffer[30];
  sprintf(buffer, "%04d/%02d/%02d %02d:%02d:%02d",
          timeinfo->tm_year + 1900,
          timeinfo->tm_mon + 1,
          timeinfo->tm_mday,
          timeinfo->tm_hour,
          timeinfo->tm_min,
          timeinfo->tm_sec);
  return String(buffer);
}

String htmlPage(String message = "", String timeInfo = "") {
  String page = R"rawliteral(
<!DOCTYPE html>
<html lang="zh-Hant">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
<title>ESP8266 WiFi設定</title>
</head>
<body class="bg-light">
<div class="container mt-5">
  <div class="card shadow">
    <div class="card-body">
      <h2 class="card-title">ESP8266 WiFi 設定</h2>
)rawliteral";

  if (!isConnected) {
    page += R"rawliteral(
      <form action="/connect" method="POST">
        <div class="mb-3">
          <label for="ssid" class="form-label">SSID</label>
          <input type="text" class="form-control" name="ssid" id="ssid" placeholder="請輸入WiFi名稱">
        </div>
        <div class="mb-3">
          <label for="password" class="form-label">密碼</label>
          <input type="password" class="form-control" name="password" id="password" placeholder="請輸入WiFi密碼">
        </div>
        <button type="submit" class="btn btn-primary">連線</button>
      </form>
)rawliteral";

    if (wifiSSID != "") {
      page += "<p class='mt-3 text-muted'>上次儲存的SSID: <b>" + wifiSSID + "</b></p>";
    }
  } else {
    page += "<p><b>已連線至:</b> " + wifiSSID + "</p>";
    page += "<p><b>目前時間:</b> <span id='time'>" + timeInfo + "</span></p>";
    // 新增時間設定與郵件通知表單
    page += R"rawliteral(
      <form action="/settime" method="POST" class="mb-3">
        <label for="timemode" class="form-label">時間設定</label>
        <select class="form-select" name="timemode" id="timemode">
          <option value="Full-Time")rawliteral";
    if (timeMode == "Full-Time") page += " selected";
    page += R"rawliteral(>Full-Time</option>
          <option value="23:00-06:00")rawliteral";
    if (timeMode == "23:00-06:00") page += " selected";
    page += R"rawliteral(>23:00-06:00</option>
        </select>
        <div class="mt-3">
          <label for="emails" class="form-label">郵件通知 (多個請用 ; 分隔)</label>
          <input type="text" class="form-control" name="emails" id="emails" placeholder="user1@example.com;user2@example.com" value=")rawliteral";
    page += alertEmails;
    page += R"rawliteral(">
        </div>
        <button type="submit" class="btn btn-secondary mt-2">套用</button>
      </form>
    )rawliteral";
    page += R"rawliteral(
      <form action="/disconnect" method="POST">
        <button type="submit" class="btn btn-danger">結束連線</button>
      </form>
    )rawliteral";
    page += R"rawliteral(
          <div class="mt-3">
            <button onclick="checkStatus()" class="btn btn-info">檢查系統狀態</button>
            <div id="gpioStatus" class="mt-2"></div>
          </div>
          <script>
          function checkStatus() {
            Promise.all([
              fetch('/gpio').then(response => response.text()),
              fetch('/time').then(response => response.text())
            ]).then(([gpioData, timeData]) => {
              document.getElementById('gpioStatus').innerHTML = gpioData;
              document.getElementById('time').innerText = timeData;
            });
          }
          </script>
        )rawliteral";
  }

  if (message != "") {
    page += "<div class='alert alert-info mt-3'>" + message + "</div>";
  }

  page += R"rawliteral(
    </div>
  </div>
</div>
</body></html>
)rawliteral";

  return page;
}

void handleRoot() {
  String timeStr = isConnected ? getDateTime() : "";
  server.send(200, "text/html", htmlPage("", timeStr));
}

void handleConnect() {
  wifiSSID = server.arg("ssid");
  wifiPASS = server.arg("password");

  WiFi.mode(WIFI_STA);
  WiFi.begin(wifiSSID.c_str(), wifiPASS.c_str());

  int attempt = 0;
  while (WiFi.status() != WL_CONNECTED && attempt < 20) {
    delay(500);
    attempt++;
  }

  if (WiFi.status() == WL_CONNECTED) {
    isConnected = true;
    saveWiFiInfo(wifiSSID, wifiPASS);
    timeClient.begin();
    server.send(200, "text/html", htmlPage("連線成功並已儲存設定!", getDateTime()));
    Serial.print(getDateTime());
    Serial.println(" - 連線成功並已儲存設定!");

  } else {
    isConnected = false;
    WiFi.mode(WIFI_AP);
    WiFi.softAP(apSSID, apPassword);
    server.send(200, "text/html", htmlPage("連線失敗，請重新輸入!", ""));
    Serial.print(getDateTime());
    Serial.println(" - 連線失敗，請重新輸入!");
  }
}

void handleDisconnect() {
  WiFi.disconnect();
  isConnected = false;
  timeClient.end();
  clearWiFiInfo();
  WiFi.mode(WIFI_AP);
  WiFi.softAP(apSSID, apPassword);
  server.send(200, "text/html", htmlPage("已結束連線，並清除設定", ""));
  Serial.print(getDateTime());
  Serial.println(" - 已結束連線，並清除設定");
}

void handleTime() {
  server.send(200, "text/plain", getDateTime());
}

void handleGPIO() {
  int state = digitalRead(GPIO_2);
  String status;
  if (state == LOW) {
    status = "<div class='text-danger'>門窗狀態：開啟</div>";
    Serial.println("門窗狀態：開啟");
  } else {
    status = "<div class='text-success'>門窗狀態：關閉</div>";
    Serial.println("門窗狀態：關閉");
  }
  server.send(200, "text/html", status);
}

// 修改處理時間設定的 handler，加入郵件欄位
void handleSetTime() {
  if (server.hasArg("timemode")) {
    timeMode = server.arg("timemode");
    Serial.print("時間設定已變更為: ");
    Serial.println(timeMode);
  }
  if (server.hasArg("emails")) {
    alertEmails = server.arg("emails");
    Serial.print("郵件通知已變更為: ");
    Serial.println(alertEmails);
  }
  saveSettings(); // 設定後存入 EEPROM
  String timeStr = isConnected ? getDateTime() : "";
  server.send(200, "text/html", htmlPage("設定已更新", timeStr));
}

void setup() {
  Serial.begin(9600);
  pinMode(GPIO_2, INPUT); // DOOR sensor pin
  Serial.println("系統啟動中...");

  loadSettings(); // 開機時讀取時間設定與郵件通知

  if (loadWiFiInfo()) {
    WiFi.mode(WIFI_STA);
    WiFi.begin(wifiSSID.c_str(), wifiPASS.c_str());

    int attempt = 0;
    while (WiFi.status() != WL_CONNECTED && attempt < 20) {
      Serial.println("Wifi連線失敗, 重試.....");
      delay(1000);
      attempt++;
    }

    if (WiFi.status() == WL_CONNECTED) {
      isConnected = true;
      Serial.println("Wifi已連線");
      timeClient.begin();
    } else {
      isConnected = false;
      Serial.println("Wifi已斷線, 啟動AP模式");
      WiFi.mode(WIFI_AP);
      WiFi.softAP(apSSID, apPassword);
    }
  } else {
    WiFi.mode(WIFI_AP);
    WiFi.softAP(apSSID, apPassword);
  }

  server.on("/", handleRoot);
  server.on("/connect", HTTP_POST, handleConnect);
  server.on("/disconnect", HTTP_POST, handleDisconnect);
  server.on("/time", handleTime);
  server.on("/gpio", HTTP_GET, handleGPIO);
  server.on("/settime", HTTP_POST, handleSetTime);
  server.begin();
}

void loop() {
  // 讀取 DOOR sensor 狀態
  int doorState = digitalRead(GPIO_2);

  // 檢查狀態是否有變化
  if (doorState == LOW && !door_open) {
    door_open = true;
    Serial.println("DOOR sensor 主動偵測到開啟");
    // 判斷是否在通知時段
    bool notify = false;
    if (timeMode == "Full-Time") {
      notify = true;
    } else if (timeMode == "23:00-06:00") {
      time_t rawTime = timeClient.getEpochTime();
      struct tm *timeinfo = localtime(&rawTime);
      int hour = timeinfo->tm_hour;
      // 23:00~23:59 或 0:00~5:59
      if (hour >= 23 || hour < 6) notify = true;
    }

    if (notify && alertEmails.length() > 0) {
      // 寄送通知信件
      Serial.print("寄送通知信件給: ");
      Serial.println(alertEmails);
      // 這裡可呼叫 SMTP 函式或其他通知機制
      // sendEmail(alertEmails, "門窗開啟警報", "門窗於 " + getDateTime() + " 被開啟!");
    }
    // 可在此觸發其他通知機制（如蜂鳴器、推播等）

  } else if (doorState == HIGH && door_open) {
    door_open = false;
    Serial.println("DOOR sensor 主動偵測到關閉");
    // 可在此觸發其他通知機制
  }
  
  server.handleClient();
  
  if (isConnected) {
    timeClient.update();
    if (WiFi.status() != WL_CONNECTED) {
      unsigned long currentMillis = millis();
      if (currentMillis - lastReconnectAttempt > reconnectInterval) {
        Serial.println("[系統] 偵測到 WiFi 斷線，嘗試重新連線...");
        WiFi.begin(wifiSSID.c_str(), wifiPASS.c_str());
        lastReconnectAttempt = currentMillis;
      }
    }
  }
}
