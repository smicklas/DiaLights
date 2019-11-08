#include <Blynk.h>

//BLINK SETUP
#define BLYNK_USE_DIRECT_CONNECT
#include <SoftwareSerial.h>
#include "FastLED.h"

SoftwareSerial DebugSerial(2, 3); // RX, TX
#define BLYNK_PRINT DebugSerial
#include <BlynkSimpleSerialBLE.h>
char auth[] = SECRET_PASS;

int small_rand = 0;
#define DATA_PIN 5
#define NUM_LEDS 45
CRGB leds[NUM_LEDS];
bool twinkle_trigger = false;
int red[3];
int green[3];
int blue[3];
int oscillate_value[3]; //oscillate on red (0), green (1), or blue (2);
//TO DO - change this to enum for readability

void setup()
{
  randomSeed(analogRead(5));
  //Show color, test
  FastLED.addLeds<NEOPIXEL, DATA_PIN>(leds, NUM_LEDS);
  //LEDS.showColor(CRGB(255, 0, 0));
  delay(500);
  DebugSerial.begin(9600);
  pinMode(DATA_PIN, OUTPUT);
  Serial.begin(9600);
  Blynk.begin(Serial, auth);

  delay(600);
  FastLED.show();
  //END TEST
}

void loop(){
  FastLED.show();
  Blynk.run();
  if(twinkle_trigger){
    twinkle();
  }
}

//zeRGBa, Select static color for left diamond, LEDS 0 - 14
BLYNK_WRITE(V0){
  int pindata = param.asInt();
  for (uint8_t i = 0; i < 15; i++) {
    byte r, g, b;
    r = param[0];
    g = param[1];
    b = param[2];
    leds[i].r = r;
    leds[i].g = g;
    leds[i].b = b;
  }
}

//zeRGBa, Select static color for upper right diamond, LEDS 15-29
BLYNK_WRITE(V1){
  int pindata = param.asInt();
  for (uint8_t i = 15; i < 30; i++) {
    byte r, g, b;
    r = param[0];
    g = param[1];
    b = param[2];
    leds[i].r = r;
    leds[i].g = g;
    leds[i].b = b;
  }
}

//zeRGBa, Select static color for lower right diamond, LEDS 31-45
BLYNK_WRITE(V2){
  int pindata = param.asInt();
  for (uint8_t i = 30; i < 45; i++) {
    byte r, g, b;
    r = param[0];
    g = param[1];
    b = param[2];
    leds[i].r = r;
    leds[i].g = g;
    leds[i].b = b;
  }
}

//Button, enable twinkle for lights

BLYNK_WRITE(V3){
int pindata = param.asInt();
  if(pindata == 1){
    twinkle_trigger = !twinkle_trigger;
  }
  if (twinkle_trigger){
    for(int i = 0; i < 3; i++){
      red[i] = leds[i*15].r;
      green[i] = leds[i*15].g;
      blue[i] = leds[i*15].b;
      //}
    }
  }
  for(uint8_t i = 0; i < 3; i++){
    small_rand = random(2);
    int color_wave = -1 * (beatsin16(10, 10, 255)); //generate color wave
    //Find the max value of the RGB values to find value to oscillate on
    if(red[i] == 0 && blue[i] == 0 && green[i] == 0){ //turned off, do nothing
      oscillate_value[i] = -1; //do nothing
    }else{
      if(red[i] > blue[i] && red[i] > green[i]){ //red is biggest
        oscillate_value[i] = 2;  //red -> blue
      }else if(blue[i] > red[i] && blue[i] > green[i]){ //blue is biggest
        oscillate_value[i] = 1; //blue -> green
      }else if(green[i] > red[i] && green[i] > blue[i]){ //green is biggest
        oscillate_value[i] = 0; //green -> red
      }else{ //if two values are the same, choose a random one between the two equals to oscillate on (don't want white glow i.e all max)
      if(red[i] == green[i]){
        switch(small_rand){
          case 0: oscillate_value[i] = 0; break; //red
          case 1: oscillate_value[i] = 1; break; //green
        }
      }else if(red[i] == blue[i]){
        switch(small_rand){
          case 0: oscillate_value[i] = 0; break; //red
          case 1: oscillate_value[i] = 2; break; //blue
        }
      }else{ //blue == green
        switch(small_rand){
          case 0: oscillate_value[i] = 1; break; //green
          case 1: oscillate_value[i] = 2; break; //blue
        }
      }
    }
  }
}
}

void twinkle(){
  for(uint8_t i = 0; i < 3; i++){
    small_rand = random(2);
    int color_wave = -1 * (beatsin16(10, 10, 255)); //generate color wave
    int brightness_wave = -1 * (beatsin8(10, 10, 255, i*50, (i * 20000)));

    switch(oscillate_value[i]){
      case 0: fill_solid(leds + (15*i), 15, CRGB(color_wave, green[i], blue[i])); break;
      case 1: fill_solid(leds + (15*i), 15, CRGB(red[i], color_wave, blue[i])); break;
      case 2: fill_solid(leds + (15*i), 15, CRGB(red[i], green[i], color_wave)); break;
    }
    for(int j = 0; j < 15; j++){
      leds[(i*15) + j].nscale8_video(brightness_wave);
    }
  }
}
