
/**
 * based on example from Firebase ESP Client:
 *
 * Github: https://github.com/mobizt/Firebase-ESP-Client
 * Copyright (c) 2023 mobizt
 *
 * set wifi ssid, widi pw
 *  and firebase api key, firebase project id, fire base user mail and pw
 *
 */

#include <SimpleDHT.h>
#include "time.h"
// This example shows how to create a document in a document collection. This operation required Email/password, custom or OAUth2.0 authentication.

#include <Arduino.h>
#if defined(ESP32) || defined(PICO_RP2040)
#include <WiFi.h>
#elif defined(ESP8266)
#include <ESP8266WiFi.h>
#endif

#include <Firebase_ESP_Client.h>

// Provide the token generation process info.
#include <addons/TokenHelper.h>

/* 1. Define the WiFi credentials */
#define WIFI_SSID "ssid"
#define WIFI_PASSWORD "pw"


/* 2. Define the API Key */
#define API_KEY "xyz"

/* 3. Define the project ID */
#define FIREBASE_PROJECT_ID "project_id"

/* 4. Define the user Email and password that alreadey registerd or added in your project */
#define USER_EMAIL "mail"
#define USER_PASSWORD "pw"


// dht
// for DHT11, 
//      VCC: 5V or 3V
//      GND: GND
//      DATA: 2
int pinDHT11 = 21;
SimpleDHT11 dht11(pinDHT11);

// time shit
const char* ntpServer = "pool.ntp.org";
const long  gmtOffset_sec = 0; //3600; // scheinbar schaut firebase wo ich bin und passt das selber an?
const int   daylightOffset_sec = 0; // 3600; // ist grad winter

// Define Firebase Data object
FirebaseData fbdo;

FirebaseAuth auth;
FirebaseConfig config;

unsigned long dataMillis = 0;

#if defined(ARDUINO_RASPBERRY_PI_PICO_W)
WiFiMulti multi;
#endif

// The Firestore payload upload callback function
void fcsUploadCallback(CFS_UploadStatusInfo info)
{
    if (info.status == fb_esp_cfs_upload_status_init)
    {
        Serial.printf("\nUploading data (%d)...\n", info.size);
    }
    else if (info.status == fb_esp_cfs_upload_status_upload)
    {
        Serial.printf("Uploaded %d%s\n", (int)info.progress, "%");
    }
    else if (info.status == fb_esp_cfs_upload_status_complete)
    {
        Serial.println("Upload completed ");
    }
    else if (info.status == fb_esp_cfs_upload_status_process_response)
    {
        Serial.print("Processing the response... ");
    }
    else if (info.status == fb_esp_cfs_upload_status_error)
    {
        Serial.printf("Upload failed, %s\n", info.errorMsg.c_str());
    }
}

void setup()
{

    Serial.begin(115200);

#if defined(ARDUINO_RASPBERRY_PI_PICO_W)
    multi.addAP(WIFI_SSID, WIFI_PASSWORD);
    multi.run();
#else
    WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
#endif

    Serial.print("Connecting to Wi-Fi");
    unsigned long ms = millis();
    while (WiFi.status() != WL_CONNECTED)
    {
        Serial.print(".");
        delay(300);
#if defined(ARDUINO_RASPBERRY_PI_PICO_W)
        if (millis() - ms > 10000)
            break;
#endif
    }
    Serial.println();
    Serial.print("Connected with IP: ");
    Serial.println(WiFi.localIP());
    Serial.println();

    // time shit
    configTime(gmtOffset_sec, daylightOffset_sec, ntpServer);

    //firebase shit
    Serial.printf("Firebase Client v%s\n\n", FIREBASE_CLIENT_VERSION);

    /* Assign the api key (required) */
    config.api_key = API_KEY;

    /* Assign the user sign in credentials */
    auth.user.email = USER_EMAIL;
    auth.user.password = USER_PASSWORD;

    // The WiFi credentials are required for Pico W
    // due to it does not have reconnect feature.
#if defined(ARDUINO_RASPBERRY_PI_PICO_W)
    config.wifi.clearAP();
    config.wifi.addAP(WIFI_SSID, WIFI_PASSWORD);
#endif

    /* Assign the callback function for the long running token generation task */
    config.token_status_callback = tokenStatusCallback; // see addons/TokenHelper.h

#if defined(ESP8266)
    // In ESP8266 required for BearSSL rx/tx buffer for large data handle, increase Rx size as needed.
    fbdo.setBSSLBufferSize(2048 /* Rx buffer size in bytes from 512 - 16384 */, 2048 /* Tx buffer size in bytes from 512 - 16384 */);
#endif

    // Limit the size of response payload to be collected in FirebaseData
    fbdo.setResponseSize(2048);

    Firebase.begin(&config, &auth);

    Firebase.reconnectWiFi(true);

    // For sending payload callback
    // config.cfs.upload_callback = fcsUploadCallback;
}

void loop()
{

    // Firebase.ready() should be called repeatedly to handle authentication tasks.

    // 60.000 -> 1 min
    // 3.600.000 -> hoffentlich 1h
    if (Firebase.ready() && (millis() - dataMillis > 3600000 || dataMillis == 0))
    {
        dataMillis = millis();

        // For the usage of FirebaseJson, see examples/FirebaseJson/BasicUsage/Create.ino
        FirebaseJson content;



        // DHT stuff
        Serial.println("Sample DHT");
        byte temperature = 0;
        byte humidity = 0;
        int err = SimpleDHTErrSuccess;
        if ((err = dht11.read(&temperature, &humidity, NULL)) != SimpleDHTErrSuccess) {
          Serial.print("Read DHT11 failed, err="); Serial.print(SimpleDHTErrCode(err));
          Serial.print(","); Serial.println(SimpleDHTErrDuration(err)); delay(1000);
          return;
        }
        Serial.print("Sample OK: ");
        Serial.print((int)temperature); Serial.print(" *C, "); 
        Serial.print((int)humidity); Serial.println(" H");
        // END DHT stuff

        // integer
        content.set("fields/temp/integerValue", String((int)temperature));
        content.set("fields/humi/integerValue", String((int)humidity));


        // time shit
        struct tm timeinfo;
        time_t now;
        if(!getLocalTime(&timeinfo)){
          Serial.println("Failed to obtain time");
          return;
        }
        //"2014-10-02T15:01:23Z" // RFC3339 UTC "Zulu" format
        //Serial.println(&timeinfo, "%Y-%m-%dT%H:%M:%SZ");
        char timestamp[21];
        strftime(timestamp, 21, "%Y-%m-%dT%H:%M:%SZ", &timeinfo);
        Serial.println(timestamp);
        time(&now);
        Serial.println(now);
        // END time shit

        // timestamp
        content.set("fields/myTimestamp/timestampValue", timestamp); 


        // We will create the nested document in the parent path "a0/b0/c0
        // a0 is the collection id, b0 is the document id in collection a0 and c0 is the collection id in the document b0.
        // and d? is the document id in the document collection id c0 which we will create.
        String documentPath = "temphumi_data/temphumi_" + String(now);

        Serial.print("Create a document... ");

        if (Firebase.Firestore.createDocument(&fbdo, FIREBASE_PROJECT_ID, "" /* databaseId can be (default) or empty */, documentPath.c_str(), content.raw()))
            Serial.printf("ok\n%s\n\n", fbdo.payload().c_str());
        else
            Serial.println(fbdo.errorReason());
    }
}
