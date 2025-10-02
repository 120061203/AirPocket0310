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
//const char* ssid = "TSAI_TSAI";
//const char* password = "0930051269";
const char* ssid = "Redmi2";
const char* password = "w3c5qgac";
const char* mqttServer = "140.127.220.208 ";  // MQTT伺服器位址
//const char* mqttServer = "124.218.131.20";  // MQTT伺服器位址
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
// Set web server port number to 80
WiFiServer server(80);
// Variable to store the HTTP request
String header;
// Current time
unsigned long currentTime = millis();
// Previous time
unsigned long previousTime = 0; 
// Define timeout time in milliseconds (example: 2000ms = 2s)
const long timeoutTime = 2000;
String myLocalIp;

void setup_wifi() {
  delay(10);

  WiFi.begin(ssid, password);
  unsigned wifiStartTime = millis();
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
    if (millis() - wifiStartTime > 3000) {
      Serial.println("wifi connected fail");
      break;
     }
  }
  if(WiFi.status() == WL_CONNECTED){
    Serial.println(F(""));
    Serial.println(F("WiFi connected"));
  }
  

  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());
  myLocalIp = WiFi.localIP().toString();
  
  Serial.println(myLocalIp);
  server.begin();
}

void reconnect() {
  unsigned mqttStartTime = millis();
  while (!client.connected()) {
    
    if (client.connect(clientID, mqttUserName, mqttPwd)) {
      Serial.println("MQTT connected");
    } else {
      Serial.print(F("failed, rc="));
      Serial.print(client.state());
      Serial.println(F(" try again in 5 seconds"));
      delay(5000);  // 等5秒之後再重試
    }
    if (millis() - mqttStartTime > 10000) {
      Serial.println("mqtt connected fail");
      break;
     }
  }
}




void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);
  dht.begin();
  
  String bluetoothName="airpocket"+boxID;
  BT.begin(bluetoothName); //藍芽裝置名稱
  Serial.println(F("The device started, now you can pair it with bluetooth!"));
  

}

void loop() {
  // put your main code here, to run repeatedly:
  h = dht.readHumidity();
  t = dht.readTemperature();
//  Serial.print(F("Humidity: "));
//  Serial.println(h);
//  Serial.print(F("Temperature: "));
//  Serial.println(t);

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

  if(WiFi.status() != WL_CONNECTED){
      setup_wifi();
   }
  if(WiFi.status() == WL_CONNECTED){
      client.setServer(mqttServer, 1883);
  }
  

  if (!client.connected()) {
    reconnect();
  }
  client.loop();

  // 等待10秒
  if (millis() - prevMillis > interval) {
      h = dht.readHumidity();
      t = dht.readTemperature();
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
    msgStr=msgStr+"\"latitude\":\""+lat_str+"\",";
    msgStr=msgStr+"\"IP\":\""+myLocalIp;
    msgStr=msgStr+"\"}";
    msgStr=msgStr+"}]";
    BT.println(msgStr);
    

    
    
    // 宣告字元陣列
    byte arrSize = msgStr.length() + 1;
    char msg[arrSize];

    Serial.print("Publish message: ");
    Serial.println(msgStr);
    msgStr.toCharArray(msg, arrSize); // 把String字串轉換成字元陣列格式
    client.publish(topic, msg);       // 發布MQTT主題與訊息
    
    msgStr = "";
    delay(2000);
  }
  
  WiFiClient wifiClient = server.available();   // Listen for incoming clients

  if (wifiClient) {                             // If a new client connects,
    currentTime = millis();
    previousTime = currentTime;
    Serial.println("New Client.");          // print a message out in the serial port
    String currentLine = "";                // make a String to hold incoming data from the client
    while (client.connected() && currentTime - previousTime <= timeoutTime) {  // loop while the client's connected
      currentTime = millis();
      if (wifiClient.available()) {             // if there's bytes to read from the client,
        char c = wifiClient.read();             // read a byte, then
        Serial.write(c);                    // print it out the serial monitor
        header += c;
        if (c == '\n') {                    // if the byte is a newline character
          // if the current line is blank, you got two newline characters in a row.
          // that's the end of the client HTTP request, so send a response:
          if (currentLine.length() == 0) {
            // HTTP headers always start with a response code (e.g. HTTP/1.1 200 OK)
            // and a content-type so the client knows what's coming, then a blank line:
            wifiClient.println("HTTP/1.1 200 OK");
            wifiClient.println("Content-type:text/html");
            wifiClient.println("Connection: close");
            wifiClient.println();
            
            
            
            // Display the HTML web page
            String ptr = "<!DOCTYPE html> <html>\n";
                ptr +="<head><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, user-scalable=no\">\n";
                ptr +="<meta charset='utf-8'>\n";
                //ptr +="<meta http-equiv = 'refresh' content = '1'>\n";
                ptr +="<title>空氣監測系統</title>\n";
                ptr +="<script language='JavaScript'>";
                ptr +="function myrefresh()";
                ptr +="{window.location.reload();}";
                ptr +="setTimeout('myrefresh()',1000); //指定1秒刷新一次";
                ptr +="</script>";
                ptr +="<style>";
                ptr +="body{background:linear-gradient(45deg,#4cd7be,#25a0a0);background-repeat:round ;font-family: Arial, Helvetica, sans-serif;margin: 0;padding: 0;}";
                ptr +="h1{text-align: center;font-size: 2.5rem;height: 50px;color:white;}";
                ptr +=".airBoxData{margin: 0 auto;width:80%;height: calc(90vh - 108px);border-radius: 10px;background-color:rgba(255, 255, 255, 0.3);text-align: center;border-collapse:collapse;margin-bottom: 10vh;}";
                ptr +=".airBoxData thead{font-weight: 300;font-size: 1.7rem;color:white;}";
                ptr +=".airBoxData thead th{border-bottom: 2px solid white;height: 50px;}";
                ptr +=".airBoxData tbody{color:white;font-size: 1.4rem;font-weight: bold;}";
                ptr +=".airBoxData tbody tr{height: 50px;}";
                ptr +=".airBoxData tbody tr:nth-child(even){background-color: rgba(255, 255, 255, 0.5);color:#666;}";
                ptr +="";
                ptr +="</style>\n";
                ptr +="</head>\n";
                ptr +="<body>\n";
                
               
                ptr +="<body>";
                ptr +="    <h1>空氣監測系統</h1>";
                ptr +="    <table class=\"airBoxData\">";
                ptr +="        <thead>";
                ptr +="            <tr>";
                ptr +="                <th>資料</th>";
                ptr +="                <th>數值</th>";
                ptr +="            </tr>";
                ptr +="        </thead>";
                ptr +="        <tbody>";
                ptr +="            <tr>";
                ptr +="                <td>日期</td>";
                ptr +="                <td>";
                ptr += month;
                ptr +="月";
                ptr += day;
                ptr +="日</td>";
                ptr +="            </tr>";
                ptr +="            <tr>";
                ptr +="                <td>時間</td>";
                ptr +="                <td>";
                ptr +=hour;
                ptr +=":";
                if(minute<10){
                  ptr+="0";
                }
                ptr +=minute;
                ptr +=":";
                if(second<10){
                  ptr+="0";
                }
                ptr +=second;
                ptr +="</td>";
                ptr +="            </tr>";
                ptr +="            <tr>";
                ptr +="                <td>溫度</td>";
                ptr +="                <td>";
                ptr +=t;
                ptr +="°c</td>";
                ptr +="            </tr>";
                ptr +="            <tr>";
                ptr +="                <td>濕度</td>";
                ptr +="                <td>";
                ptr += h;
                ptr +="%</td>";
                ptr +="            </tr>";
                ptr +="            <tr>";
                ptr +="                <td>經度</td>";
                ptr +="                <td>";
                ptr +=lng_str;
                ptr +="度</td>";
                ptr +="            </tr>";
                ptr +="            <tr>";
                ptr +="                <td>緯度</td>";
                ptr +="                <td>";
                ptr +=lat_str;
                ptr +="度</td>";
                ptr +="            </tr>";
                ptr +="            <tr>";
                ptr +="                <td>PM10</td>";
                ptr +="                <td>";
                ptr +=pmat100;
                ptr+="</td>";
                ptr +="            </tr>";
                ptr +="            <tr>";
                ptr +="                <td>PM2.5</td>";
                ptr +="                <td>";
                ptr +=pmat25;
                ptr += "</td>";
                ptr +="            </tr>";
                ptr +="            <tr>";
                ptr +="                <td>PM1.0</td>";
                ptr +="                <td>";
                ptr +=pmat1;
                ptr +="</td>";
                ptr +="            </tr>";
                ptr +="            <tr>";
                ptr +="                <td>Mac</td>";
                ptr +="                <td>";
                ptr +=boxID;
                ptr +="</td>";
                ptr +="            </tr>";
                ptr +="            <tr>";
                ptr +="                <td>IP</td>";
                ptr +="                <td>";
                ptr +=myLocalIp;
                ptr +="</td>";
                ptr +="            </tr>";
                ptr +="        </tbody>";
                ptr +="    </table>";
                ptr +="</body>";
                ptr +="</html>";
            wifiClient.print(ptr);
           
            
            // The HTTP response ends with another blank line
            wifiClient.println();
            // Break out of the while loop
            break;
          } else { // if you got a newline, then clear currentLine
            currentLine = "";
          }
        } else if (c != '\r') {  // if you got anything else but a carriage return character,
          currentLine += c;      // add it to the end of the currentLine
        }
      }
    }
    // Clear the header variable
    header = "";
    // Close the connection
    wifiClient.stop();
    Serial.println("Client disconnected.");
    Serial.println("");
  }






  
}
