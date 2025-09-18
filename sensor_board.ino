/*
1.提供門檔短路/斷路的訊號判斷:門檔磁簧開關的接觸不穩定,判定是否接通以設定時間內是否有接收到analog value > 閾值
2.門檔斷路時,外接LED燈亮
3.提供訊號給外接ESP32的GPIO
*/
const int ledpin = 5;  // the number of the LED V+ pin
const int sensor = A2; // the number of the sensor analog input pin
const int esp32_gpio = 7; // the number of the ESP32 GPIO pin
const int threshold = 500; // the threshold of the sensor analog input
const int timeout = 3000; // the timeout of the sensor analog input
int lastTime = 0;

void setup() {
  // put your setup code here, to run once:
  pinMode(ledpin, OUTPUT);
  pinMode(sensor, INPUT);
  pinMode(esp32_gpio, OUTPUT);
}

void loop() {
  // put your main code here, to run repeatedly:
  int sensorValue = analogRead(sensor);
  lastTime = millis();
  while (millis() - lastTime < timeout) {
    sensorValue = analogRead(sensor);
    if (sensorValue > threshold) {
      digitalWrite(ledpin, LOW); //門檔短路時,外接LED燈滅
      digitalWrite(esp32_gpio, HIGH);
      break;
    }
  }
  lastTime = millis();
  while (millis() - lastTime < timeout) {
    sensorValue = analogRead(sensor);
    if (sensorValue < threshold) {
      digitalWrite(ledpin, HIGH); //門檔斷路時,外接LED燈亮
      digitalWrite(esp32_gpio, LOW);
      break;
    }
  }

}
