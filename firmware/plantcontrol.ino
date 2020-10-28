// This #include statement was automatically added by the Particle IDE.
#include <Adafruit_DHT_Particle.h>
#include <Arduino.h>

#define DHTPIN 2
#define DHTTYPE DHT11


bool lowMoisture = false;
int smsOn = 0;
int waterSensor = A0;
int plantVal = 0;

int led = 6;

double humidityVal = 0;
double temp = 0;

int limit = 1500;

DHT dht(DHTPIN, DHTTYPE);

int changeLimit(String value);
int turnOffSms(String command);


void setup() {
    
    pinMode(led, OUTPUT);
    
    Particle.variable("moistureval", &plantVal, INT);
    Particle.variable("humidityval", &humidityVal, DOUBLE);
    Particle.variable("temp", &temp, DOUBLE);
    Particle.variable("limit", &limit, INT);
    Particle.variable("smsonoff", &smsOn, INT);
    
    Particle.function("changelimit", changeLimit);
    Particle.function("turnoffsms", turnOffSms);
    
    dht.begin();
    
}

void loop() {
    
    humidityVal = dht.getHumidity();
	temp = dht.getTempCelcius();
	
	Serial.print("Humidity: ");
	Serial.println(humidityVal);
	Serial.print("temp: ");
	Serial.println(temp);
  
	if (isnan(humidityVal) || isnan(temp)) {
		Serial.println("Failed to read from DHT sensor!");
		return;
	}
    
    printWaterLevel();
    checkMoisture();
    delay(2000);

}

void printWaterLevel() {
    
    plantVal = analogRead(waterSensor);
    
}


void alertLowMoisture() {
    Particle.publish("twilio_sms", "Plant needs water", PRIVATE);
}

void checkMoisture() {
    if(plantVal < limit && lowMoisture == false) {
        digitalWrite(led, HIGH);
        if(smsOn) {
            alertLowMoisture();
        }
        lowMoisture = true;
    } else if(plantVal > limit) {
        digitalWrite(led, LOW);
        lowMoisture = false;
    }
}

int changeLimit(String value) {
    limit = value.toInt();
    return 1;
}

int turnOffSms(String command) {
    if(command == "on") {
        smsOn = 1;
        return 1;
    } else if(command == "off") {
        smsOn = 0;
        return 0;
    }
    
    return -1;
}


