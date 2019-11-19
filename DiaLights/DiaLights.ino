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
          
void setup(){
  FastLED.addLeds<NEOPIXEL, DATA_PIN>(leds, 45);
  // Initial RGB flash
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
      setLightColor();
      FastLED.show(); 
    }else{
      control += data; //Build control string 
    }
  }                             
}

//Command format example: "(aR255B255C255)"
//<a, b, c> to signify which panel is being controlled 
//<R, G, B> to control RGB value followed by 0-255 value
//Parenthesis used to delimit command
void setLightColor(){
  int red_begin = control.indexOf('R');
  int green_begin = control.indexOf('G');
  int blue_begin = control.indexOf('B');
  int end_begin = control.indexOf(')');
  char panel = control[1];
  String red = control.substring(red_begin + 1, green_begin); 
  String green = control.substring(green_begin + 1, blue_begin);
  String blue = control.substring(blue_begin + 1, end_begin);

  switch(panel){
    case 'a': fill_solid(leds + 30, 15, CRGB(red.toInt(), green.toInt(), blue.toInt())); break; //top left panel 
    case 'b': fill_solid(leds + 15, 15, CRGB(red.toInt(), green.toInt(), blue.toInt())); break; //right panel 
    case 'c': fill_solid(leds, 15, CRGB(red.toInt(), green.toInt(), blue.toInt())); break;//bottom left panel  
    case 'd': fill_solid(leds, 45, CRGB(red.toInt(), green.toInt(), blue.toInt())); break;//all panels 
  }
  control = "";
}
