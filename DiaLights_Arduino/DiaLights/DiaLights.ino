#include <FastLED.h>
//mode: 0 = static, 1 =  gradient pulse, 2 = rainbow/pallete pulse
#include <SoftwareSerial.h>
#define NUM_LEDS 45
#define DATA_PIN 5
#define BRIGHTNESS1 100

CRGB leds[NUM_LEDS];
char data = 0;
String red_ar[4] = {"", "", "", ""}; //Storage for assigned LED values 
String green_ar[4] = {"", "", "", ""};
String blue_ar[4] = {"", "", "", ""};
int small_rand = 0; 
String control = "";
int oscillate_value[3]; //oscillate on red (0), green (1), or blue (2);
bool power = false;
bool twinkle_trigger = false;

void setup(){
  randomSeed(analogRead(5));
  FastLED.addLeds<NEOPIXEL, DATA_PIN>(leds, 45);
  power = true;
  LEDS.showColor(CRGB(0, 0, 0));
  delay(500);
  pinMode(5, OUTPUT);        //Sets digital pin 13 as output pin
  Serial.begin(9600);         //Sets the data rate in bits per second (baud) for serial data transmission
}

void loop(){
  if(Serial.available() > 0){  // Send data only when you receive data:
    data = (char)Serial.read();      //Read the incoming data and store it into variable data
    if(data == ')'){ //End of command
      control += data;
      processCommand();
    }else{
      control += data; //Build control string
    }
  }
  if(!power){ 
    FastLED.clear();
  }
  if(twinkle_trigger){
    twinkle();
  }
   FastLED.show();
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
  char panel_char = control[1]; 
  int panel_no = panel_char - '0';
  Serial.write(panel_no);
  if(control[1] != '4'){
      red_ar[panel_no] = red;
      blue_ar[panel_no] = blue;
      green_ar[panel_no] = green;
  }
  Serial.write(control[1]);
  //Need to store the RBG value into the array for turning ON the lights 
  switch(control[1]){
    case '0': fill_solid(leds + 30, 15, CRGB(red.toInt(), green.toInt(), blue.toInt())); power = true; break; //top left panel
    case '1': fill_solid(leds + 15, 15, CRGB(red.toInt(), green.toInt(), blue.toInt())); power = true; break; //right panel
    case '2': fill_solid(leds, 15, CRGB(red.toInt(), green.toInt(), blue.toInt())); power = true; break;//bottom left panel
    case '3': fill_solid(leds, 45, CRGB(red.toInt(), green.toInt(), blue.toInt())); power = true; break;//all panels
    case '4': setLightPower(); break;
    case '5': twinkle_trigger = !twinkle_trigger; if(twinkle_trigger){ set_twinkle_oscillation();} break; 
  }
  control = ""; //Reset
}

void setLightPower(){
  if(power){
    power = false; 
  }else{
    power = true;
    //Set the lights back to their colors 
    fill_solid(leds + 30, 15, CRGB(red_ar[0].toInt(), green_ar[0].toInt(), blue_ar[0].toInt())); //top left panel
    fill_solid(leds + 15, 15, CRGB(red_ar[1].toInt(), green_ar[1].toInt(), blue_ar[1].toInt())); //right panel
    fill_solid(leds, 15, CRGB(red_ar[2].toInt(), green_ar[2].toInt(), blue_ar[2].toInt()));  //bottom left panel
  }
}

void twinkle(){
  for(uint8_t i = 0; i < 3; i++){
    small_rand = random(2);
    int color_wave = -1 * (beatsin16(10, 10, 255)); //generate color wave
    int brightness_wave = -1 * (beatsin8(10, 10, 255, i*50, (i * 20000)));

    switch(oscillate_value[i]){
      case 0: fill_solid(leds + (15*i), 15, CRGB(color_wave, green_ar[i].toInt(), blue_ar[i].toInt())); break;
      case 1: fill_solid(leds + (15*i), 15, CRGB(red_ar[i].toInt(), color_wave, blue_ar[i].toInt())); break;
      case 2: fill_solid(leds + (15*i), 15, CRGB(red_ar[i].toInt(), green_ar[i].toInt(), color_wave)); break;
    }
    for(int j = 0; j < 15; j++){
      leds[(i*15) + j].nscale8_video(brightness_wave);
    }
  }
}
void set_twinkle_oscillation(){
  if (twinkle_trigger){
    for(int i = 0; i < 3; i++){
      red_ar[i] = leds[i*15].r;
      green_ar[i] = leds[i*15].g;
      blue_ar[i] = leds[i*15].b;
      //}
    }
  }
  for(uint8_t i = 0; i < 3; i++){
    small_rand = random(2);
    int color_wave = -1 * (beatsin16(10, 10, 255)); //generate color wave
    //Find the max value of the RGB values to find value to oscillate on
    if(red_ar[i].toInt() == 0 && blue_ar[i].toInt() == 0 && green_ar[i].toInt() == 0){ //turned off, do nothing
      oscillate_value[i] = -1; //do nothing
    }else{
      if(red_ar[i].toInt() > blue_ar[i].toInt() && red_ar[i].toInt() > green_ar[i].toInt()){ //red is biggest
        oscillate_value[i] = 2;  //red -> blue
      }else if(blue_ar[i].toInt() > red_ar[i].toInt() && blue_ar[i].toInt() > green_ar[i].toInt()){ //blue is biggest
        oscillate_value[i] = 1; //blue -> green
      }else if(green_ar[i].toInt() > red_ar[i].toInt() && green_ar[i].toInt() > blue_ar[i].toInt()){ //green is biggest
        oscillate_value[i] = 0; //green -> red
      }else{ //if two values are the same, choose a random one between the two equals to oscillate on (don't want white glow i.e all max)
      if(red_ar[i].toInt() == green_ar[i].toInt()){
        switch(small_rand){
          case 0: oscillate_value[i] = 0; break; //red
          case 1: oscillate_value[i] = 1; break; //green
        }
      }else if(red_ar[i].toInt() == blue_ar[i].toInt()){
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
