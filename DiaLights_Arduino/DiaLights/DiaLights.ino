#include "FastLED.h"
//mode: 0 = static, 1 =  gradient pulse, 2 = rainbow/pallete pulse
#include <SoftwareSerial.h>
#define NUM_LEDS 45
#define DATA_PIN 5
#define BRIGHTNESS1 100

CRGB leds[NUM_LEDS];
char data = 0;
int red[3];
int green[3];
int blue[3];
String control = "";
bool power = false;

void setup(){
  FastLED.addLeds<NEOPIXEL, DATA_PIN>(leds, 45);
  power = true;
  LEDS.showColor(CRGB(255, 0, 0));
  delay(500);
  pinMode(5, OUTPUT);        //Sets digital pin 13 as output pin
  Serial.begin(9600);         //Sets the data rate in bits per second (baud) for serial data transmission
}

void loop(){
  if(Serial.available() > 0){  // Send data only when you receive data:
    data = (char)Serial.read();      //Read the incoming data and store it into variable data
    if(data == ')'){ //End of command
      control += data;
      Serial.println(control);
      processCommand();
    }else{
      control += data; //Build control string
    }
  }
  if(power){
    FastLED.show();
  }
}

//Color set command format example: "(0R255B255C255)"
//4 = Power toggle, 5 = Twinkle toggle, 6 = Sync toggle
//<0, 1, 2> to signify which panel is being controlled
//<Rxxx, Gxxx, Bxx> to control RGB value
//Parenthesis used to delimit command
void processCommand(){
  //Refactor this; don't need to set any of this if it is a power/twinkle command
  int red_begin = control.indexOf('R');
  int green_begin = control.indexOf('G');
  int blue_begin = control.indexOf('B');
  int end_begin = control.indexOf(')');
  String red = control.substring(red_begin + 1, green_begin);
  String green = control.substring(green_begin + 1, blue_begin);
  String blue = control.substring(blue_begin + 1, end_begin);

  switch(control[1]){
    case 0: fill_solid(leds + 30, 15, CRGB(red.toInt(), green.toInt(), blue.toInt())); break; //top left panel
    case 1: fill_solid(leds + 15, 15, CRGB(red.toInt(), green.toInt(), blue.toInt())); break; //right panel
    case 2: fill_solid(leds, 15, CRGB(red.toInt(), green.toInt(), blue.toInt())); break;//bottom left panel
    case 3: fill_solid(leds, 45, CRGB(red.toInt(), green.toInt(), blue.toInt())); break;//all panels
    //case 5: //twinkle(), steal this from remote version
    case 4: setLightPower();
  }
  control = ""; //Reset
}

void setLightPower(){
  if(power){
    power = false;
    fill_solid(leds, 45, CRGB(0, 0, 0));
  }else{
    power = true;
  }
}
