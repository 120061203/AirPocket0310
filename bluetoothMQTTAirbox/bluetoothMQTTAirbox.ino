#include "DHT.h"
#define DHTPIN 2     // Digital pin connected to the DHT sensor
#define DHTTYPE DHT22   // DHT 22  (AM2302), AM2321

#include <BluetoothSerial.h>
BluetoothSerial BT;//宣告藍芽物件，名稱為BT

DHT dht(DHTPIN, DHTTYPE);

float h = 0;
float t = 0;
//////////////////////////////
#include "PMS.h"
PMS pms(Serial);
PMS::DATA data;
int pmat1 = 0;
int pmat25 = 0;
int pmat100 = 0;
//////////////////////////////
#include <WiFi.h>
#include <PubSubClient.h>
String boxID =WiFi.macAddress();
const char* ssid = "TP-LINK_2.4GHz_BE5C9B";
const char* password = "076113299";
const char* mqttServer = "140.127.220.208 ";  // MQTT伺服器位址
const char* mqttUserName = "airBox1";  // 使用者名稱，隨意設定。
const char* mqttPwd = "";  // MQTT密碼
const char* clientID = boxID.c_str();      // 用戶端ID，隨意設定，但同一個broker上不可相同
const char* topic = "iot/group3/box1";


unsigned long prevMillis = 0;  // 暫存經過時間（毫秒）
const long interval = 10000;  // 上傳資料的間隔時間，10秒。
String msgStr = "";      // 暫存MQTT訊息字串

String lat_str , lng_str;
int year,month,day,hour,minute,second;

WiFiClient espClient;
PubSubClient client(espClient);


void setup_wifi() {
  delay(10);

  WiFi.begin(ssid, password);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  Serial.println(F(""));
  Serial.println(F("WiFi connected"));
}

void reconnect() {
  while (!client.connected()) {
    if (client.connect(clientID, mqttUserName, mqttPwd)) {
      Serial.println("MQTT connected");
    } else {
      Serial.print(F("failed, rc="));
      Serial.print(client.state());
      Serial.println(F(" try again in 5 seconds"));
      delay(5000);  // 等5秒之後再重試
    }
  }
}




void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);
  dht.begin();
  setup_wifi();
  client.setServer(mqttServer, 1883);

  BT.begin(F("airpocketBluetooth")); //藍芽裝置名稱
  Serial.println(F("The device started, now you can pair it with bluetooth!"));
  

}

void loop() {
  // put your main code here, to run repeatedly:
  h = dht.readHumidity();
  t = dht.readTemperature();
  //Serial.print(F("Humidity: "));
  //Serial.println(h);
  //Serial.print(F("Temperature: "));
  //Serial.println(t);

  if (pms.read(data))
  {
    //Serial.print("PM 1.0 (ug/m3): ");
    pmat1 = data.PM_AE_UG_1_0;
    //Serial.println(pmat1);
    
    //Serial.print("PM 2.5 (ug/m3): ");
    pmat25 = data.PM_AE_UG_2_5;
    //Serial.println(pmat25);
    
    //Serial.print("PM 10.0 (ug/m3): ");
    pmat100 = data.PM_AE_UG_10_0;
    //Serial.println(pmat100);
    Serial.println();
  }
////////////////////////////////////////////


if (!client.connected()) {
    reconnect();
  }
  client.loop();

  // 等待20秒
  if (millis() - prevMillis > interval) {
    prevMillis = millis();

    

    // 組合MQTT訊息；field1填入溫度、field2填入濕度
    msgStr=msgStr+"[{";
    msgStr=msgStr+"\"boxID\":"+"\""+boxID+"\"";
    msgStr=msgStr+",";
    msgStr=msgStr+"\"data\":{";
    msgStr=msgStr+"\"year\":\""+year+"\",";
    msgStr=msgStr+"\"month\":\""+month+"\",";
    msgStr=msgStr+"\"day\":\""+day+"\",";
    msgStr=msgStr+"\"hour\":\""+hour+"\",";
    msgStr=msgStr+"\"minute\":\""+minute+"\",";
    msgStr=msgStr+"\"second\":\""+second+"\",";
    msgStr=msgStr+"\"temperature\":\""+t+"\",";
    msgStr=msgStr+"\"humidity\":\""+h+"\",";
    msgStr=msgStr+"\"pm100\":\""+pmat100+"\",";
    msgStr=msgStr+"\"pm25\":\""+pmat25+"\",";
    msgStr=msgStr+"\"pm1\":\""+pmat1+"\",";
    msgStr=msgStr+"\"longitude\":\""+lng_str+"\",";
    msgStr=msgStr+"\"latitude\":\""+lat_str;
    msgStr=msgStr+"\"}";
    msgStr=msgStr+"}]";
    
    // 宣告字元陣列
    byte arrSize = msgStr.length() + 1;
    char msg[arrSize];

    Serial.print("Publish message: ");
    Serial.println(msgStr);
    msgStr.toCharArray(msg, arrSize); // 把String字串轉換成字元陣列格式
    client.publish(topic, msg);       // 發布MQTT主題與訊息
    BT.println(msgStr);
    msgStr = "";



    
  delay(20);
  }








  
}
