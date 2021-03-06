#include <DHT.h>
#include <DHT_U.h>
#include <WiFi101.h>
#include <RTCZero.h>


//input pin variables

#define SMOKE_SENSOR A1
#define CO_SENSOR A2
#define FLAME_DETECTOR A3
#define DHT_PIN 1
#define PIR_SENSOR 2
#define WIND_OPENED 11
#define RESET_CTRL 12

//output pin variables

#define BUZZER 6
#define LED_SMOKE_LOW 7
#define LED_SMOKE_MED 8
#define LED_SMOKE_HIGH 9
#define LED_CO_FIRE_CODE 10
#define LED_HUM_PRES 3
#define RESET 15

#define DHTTYPE DHT11


//reading settings (RHP: Reset Human Presence)
long previousMillis = 0;
unsigned long currentMillis = 0;
long previousMillisHP = 0;
unsigned long currentMillisHP = 0;
long intervallHP = 180000;

//parameters
int maxOptical;
int temp;
int hum;
int smokeLev;
boolean flame;
boolean coPres;
boolean humanPres;
boolean winOpen;

//device configuration settings
long idDevice = 1234567;
char ssid[] = "TIM-24109723";
char pass[] = "GQ3lV4CkJMVM4ehwBaSfOd7f";
long intervall = 10000;

String data;

//network settings
IPAddress server(192, 168, 1, 108);
WiFiClient client;
int wifiStatus = WL_IDLE_STATUS;

//time settings
RTCZero rtc;
const int GMT = 2;

//other settings
DHT dht(DHT_PIN, DHTTYPE);




void setup() {

  Serial.begin (9600);

  //input init
  pinMode(FLAME_DETECTOR, INPUT);
  pinMode(PIR_SENSOR, INPUT);
  pinMode(WIND_OPENED, INPUT);
  pinMode(RESET, INPUT);

  //output init
  pinMode(BUZZER, OUTPUT);
  pinMode(LED_SMOKE_LOW, OUTPUT);
  pinMode(LED_SMOKE_MED, OUTPUT);
  pinMode(LED_SMOKE_HIGH, OUTPUT);
  pinMode(LED_CO_FIRE_CODE, OUTPUT);
  pinMode(LED_HUM_PRES, OUTPUT);
  pinMode(RESET_CTRL, OUTPUT);

  digitalWrite(RESET_CTRL, HIGH);

  int attempts = 0;
  int maxAttempts = 10;

  //attempt to connect to WiFi network
  while ( wifiConnection(ssid, pass) != WL_CONNECTED) {
    Serial.println("\nfailed");
    Serial.println("retrying...");
    if (attempts > maxAttempts) {
      wifiConnectionError();
    }
    attempts++;

  }

  //test server communication
  attempts = 0;
  while (WiFi.ping(server) < 0) {

    Serial.println("\nERROR: no communication with server side!");
    Serial.println("retrying...");
    if (attempts > maxAttempts) {
      serverUnreachableError();
    }
    attempts++;
  }
  Serial.println("Server reached!");


  //initialization RTC (Real Time Clock)
  rtc.begin();
  setEpoch();

  dht.begin();

  //parameters initialization
  temp = 0;
  hum = 0;
  smokeLev = 0;
  flame = false;
  coPres = false;
  humanPres = false;
  winOpen = false;

  //data values string
  data = "";

  Serial.println("\n\n\n\n");

  digitalWrite(LED_HUM_PRES, LOW);
  int i = 0;
  while ( i < 5) {
    digitalWrite(LED_HUM_PRES, HIGH);
    delay(1000);
    digitalWrite(LED_HUM_PRES, LOW);
    delay(1000);
    i++;
  }
}




void loop() {

  //check if the time intervall is reached and then do another reading
  currentMillis = millis();
  if ( (currentMillis - previousMillis) > intervall ) {

    previousMillis = currentMillis;

    //SMOKE LEVEL reading
    int smoke = 0;
    smokeLev = 0;
    smoke = analogRead(SMOKE_SENSOR);
    smoke = map(smoke, 300, 700, 0, 100);

    if (smoke >= 25) {
      digitalWrite(LED_SMOKE_LOW, HIGH);
      smokeLev = 1;
    } else {
      digitalWrite(LED_SMOKE_LOW, LOW);
    }

    if (smoke >= 50) {
      digitalWrite(LED_SMOKE_MED, HIGH);
      smokeLev = 2;
    } else {
      digitalWrite(LED_SMOKE_MED, LOW);
    }

    if (smoke >= 75) {
      digitalWrite(LED_SMOKE_HIGH, HIGH);
      smokeLev = 3;
    } else {
      digitalWrite(LED_SMOKE_HIGH, LOW);
    }




    //CARBONE MONOXIDE check

    coPres = false;
    if (analogRead(CO_SENSOR) > 700 ) {
      coPres = true;

    } else {
      coPres = false;
    }


    //FLAME DETECTOR check

    if (analogRead(FLAME_DETECTOR) < 400 ) {
      flame = true;
    } else {
      flame = false;
    }


    //TEMPERATURE AND HUMIDITY readings
    temp = dht.readTemperature();
    hum = dht.readHumidity();


    //WINDOW OPEN check
    if (digitalRead(WIND_OPENED)) {
      winOpen = true;
    } else {
      winOpen = false;
    }



    //create data string
    data = "idDevice=" + String(idDevice) + "&smoke=" + String(smokeLev) + "&flame=" + String(flame) + "&coPres="
           + String(coPres) + "&temp=" + String(temp) + "&hum=" + String(hum) + "&humanPres=" + String(humanPres)
           + "&winOpen=" + String(winOpen) + String("&timeReadout=") + String(getTimeReadout());


    //CONNECTING TO THE INTERNET AND SENDING DATA

    if (WiFi.status() != WL_CONNECTED) {

      Serial.println("WiFi connection missing!");
      delay(1000);
      wifiConnectionError();
      digitalWrite(RESET_CTRL, LOW);
    }



    if (client.connect(server, 80)) {

      Serial.println("\nconnecting to server...");
      httpSender(data);
      Serial.println("\nclosing connection...");
      client.stop();
      
    }else{
      Serial.println("Unreachable server on port 80");
      delay(5000);
      serverUnreachableError();
      digitalWrite(RESET_CTRL, LOW);
      
      }
  

} //end reading block


//HUMAN PRESENCE check

if (!flame) {

  currentMillisHP = millis();

  if ( !humanPres or ((currentMillisHP - previousMillisHP) > intervallHP)) {

    previousMillisHP = currentMillisHP;

    if (digitalRead(PIR_SENSOR)) {
      humanPres = true;
    } else {
      humanPres = false;
    }
  }
}

//fire alarm
if (smokeLev != 0) {

  fireSmokeAlarm();

} else if (coPres) {

  coPresAlarm();
}

if ( flame or coPres ) {

  digitalWrite(LED_CO_FIRE_CODE, HIGH);

} else if (!flame and !coPres) {

  digitalWrite(LED_CO_FIRE_CODE, LOW);

}

//human presence notification
if (humanPres) {
  digitalWrite(LED_HUM_PRES, HIGH);

} else {
  digitalWrite(LED_HUM_PRES, LOW);
}
}



//LIGHT NOTIFICATION


//CONFIGURATION MODE

void configMode() {

  digitalWrite(LED_HUM_PRES, HIGH);
  delay(500);
  digitalWrite(LED_HUM_PRES, LOW);
  delay(500);

}

//Errors notification
/*
  ERROR CODE: WiFi connection missing!
*/
void wifiConnectionError() {

  digitalWrite(LED_CO_FIRE_CODE, HIGH);
  delay(400);
  digitalWrite(LED_CO_FIRE_CODE, LOW);
  delay(400);
  digitalWrite(LED_CO_FIRE_CODE, HIGH);
  delay(400);
  digitalWrite(LED_CO_FIRE_CODE, LOW);
  delay(1500);

}


/*
  ERROR CODE: Server unreachable
**/

void serverUnreachableError() {

  digitalWrite(LED_CO_FIRE_CODE, HIGH);
  delay(400);
  digitalWrite(LED_CO_FIRE_CODE, LOW);
  delay(400);
  digitalWrite(LED_CO_FIRE_CODE, HIGH);
  delay(400);
  digitalWrite(LED_CO_FIRE_CODE, LOW);
  delay(400);
  digitalWrite(LED_CO_FIRE_CODE, HIGH);
  delay(400);
  digitalWrite(LED_CO_FIRE_CODE, LOW);
  delay(1500);

}


/*
  ERROR CODE: Server NTP unreachable
**/

void NTPserverUnreachableError() {

  digitalWrite(LED_CO_FIRE_CODE, HIGH);
  delay(400);
  digitalWrite(LED_CO_FIRE_CODE, LOW);
  delay(400);
  digitalWrite(LED_CO_FIRE_CODE, HIGH);
  delay(400);
  digitalWrite(LED_CO_FIRE_CODE, LOW);
  delay(400);
  digitalWrite(LED_CO_FIRE_CODE, HIGH);
  delay(400);
  digitalWrite(LED_CO_FIRE_CODE, LOW);
  delay(400);
  digitalWrite(LED_CO_FIRE_CODE, HIGH);
  delay(400);
  digitalWrite(LED_CO_FIRE_CODE, LOW);
  delay(1500);

}




//SOUND NOTIFICATION

/*
  BUZZER CODE: standard alarm for smoke detection
**/
void fireSmokeAlarm() {

  tone(BUZZER, 3000, 500);
  delay(700);
  tone(BUZZER, 3000, 500);
  delay(700);
  tone(BUZZER, 3000, 500);
  delay(1500);
}


/*
  BUZZER CODE: standard alarm for CO (Carbon Monoxide)
               detection
**/
void coPresAlarm() {

  tone(BUZZER, 3000, 200);
  delay(200);
  tone(BUZZER, 3000, 200);
  delay(200);
  tone(BUZZER, 3000, 200);
  delay(200);
  tone(BUZZER, 3000, 300);
  delay(5000);

}

/*
  CONNECTION TO INTERNET WITH WIFI INTERFACE
**/
int wifiConnection(char ssid[], char pass[]) {

  Serial.print("\n\nAttempting to connect to WPA SSID: ");
  Serial.println(ssid);
  wifiStatus = WiFi.begin(ssid, pass);
  return wifiStatus;
}


/*
   Sending data with HTTP protocol.
   POST method passes body part to
   storeData.php for processing and
   storing into the database
**/
void httpSender(String data) {

  String ipServer = ipAddressToString(server);

  client.println("POST /storeData.php HTTP/1.1");
  client.print("Host: ");
  client.println(ipServer);
  client.println("Content-Type: application/x-www-form-urlencoded");
  client.print("Content-Length: ");
  client.println(data.length());
  client.println();
  client.println(data);

  Serial.println("\n");
  Serial.println("-----------------------------------------------------------");
  Serial.println("POST /storeData.php HTTP/1.1");
  Serial.print("Host: ");
  Serial.println(ipServer);
  Serial.println("Content-Type: application/x-www-form-urlencoded");
  Serial.print("Content-Length: ");
  Serial.println(data.length());
  Serial.println("-----------------------------------------------------------");
  Serial.println();
  Serial.println(data);
  Serial.println("-----------------------------------------------------------");
  Serial.println("Data sent!");

}


/*
   Convert IPAddress value (byte[]) to String
**/
String ipAddressToString(IPAddress ip) {

  return String(ip[0]) + String(".") + String(ip[1]) + String(".") + String(ip[2]) + String(".") + String(ip[3]);
}


/*
  using RTC (Real Time Clock )
  and NTP (Network Time Protocol)
  set epoch variable to RTCZero
  instance and then get time
**/
void setEpoch() {

  unsigned long epoch;
  int attempts = 0, maxAttempts = 10;
  do {
    epoch = WiFi.getTime();
    delay(500);
    attempts++;
    if (attempts > maxAttempts) {
      NTPserverUnreachableError();
    }
  }
  while (epoch == 0);

  Serial.print("Epoch received: ");
  Serial.println(epoch);
  rtc.setEpoch(epoch);


}

/*
  Get time string from RTCZero instance and then format it in datetime type
**/

String getTimeReadout() {

  int hours = 0;
  hours = rtc.getHours() + GMT;
  if ( hours == 25 ) {
    hours = 1;
  }

  return String("20") + String(rtc.getYear()) + String("-")
         + String(print2digits(rtc.getMonth())) + String("-")
         + String(print2digits(rtc.getDay())) + String(" ")
         + String(print2digits(hours))
         + String(":") + String(print2digits(rtc.getMinutes()))
         + String(":") + String(print2digits(rtc.getSeconds()));

}

String print2digits(int number) {
  if (number < 10) {
    return String("0") + String(number);
  }
  return String(number);
}


