#include <Blynk.h> //Interface with app
#include <SoftwareSerial.h> //For debug serial
#include "FastLED.h" //LED control library

//BLINK SETUP
#define BLYNK_USE_DIRECT_CONNECT
#define BLYNK_PRINT DebugSerial

#define DATA_PIN 5 //Data pin for LEDs
#define NUM_LEDS 45
#define NUM_PANEL 3

SoftwareSerial DebugSerial(2, 3); // RX, TX

char auth[] = SECRET_PASS; //Blynk token
int small_rand = 0;
CRGB leds[NUM_LEDS]; //Initialize LEDs
bool twinkle_trigger = false;
int red[NUM_PANEL]; //USed to store RGB values of first LED in each panel
int green[NUM_PANEL];
int blue[NUM_PANEL];
int oscillate_value[3]; //Oscillate on red (0), green (1), or blue (2);
//TO DO - change this to enum for readability

void setup()
{
  randomSeed(analogRead(5));
  FastLED.addLeds<NEOPIXEL, DATA_PIN>(leds, NUM_LEDS);
  delay(500);
  DebugSerial.begin(9600);
  pinMode(DATA_PIN, OUTPUT);
  Serial.begin(9600);
  Blynk.begin(Serial, auth);
  delay(600);
  FastLED.show();

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
  fill_solid(leds, 15, CRGB(param[0], param[1], param[2]));
}

//zeRGBa, Select static color for upper right diamond, LEDS 15-29
BLYNK_WRITE(V1){
  int pindata = param.asInt();
  fill_solid(leds + 15, 15, CRGB(param[0], param[1], param[2]));
}

//zeRGBa, Select static color for lower right diamond, LEDS 31-45
BLYNK_WRITE(V2){
  int pindata = param.asInt();
  fill_solid(leds + 30, 15, CRGB(param[0], param[1], param[2]));
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
    }
  }
  for(uint8_t i = 0; i < 3; i++){
    small_rand = random(2);
    int color_wave = -1 * (beatsin16(10, 10, 255)); //generate color wave
    //Find the max value of the RGB values to find value to oscillate on
    if(red[i] == 0 && blue[i] == 0 && green[i] == 0){
      oscillate_value[i] = -1; //do nothing if lights are turned off
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
    int color_wave = -1 * (beatsin16(10, 10, 255));
    //To-do: randomly generate offset for sin wave 
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
