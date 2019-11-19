# DiaLights
Nanoleaf inspired DIY using Arduino Nano, Blynk, and FastLED library

DiaLights is a Nanoleaf inspired DIY project that stemmed from an interest in Arduino development and wanting Nanoleaf without the price tag. 

:heart: Made in Michigan :heart:

## Overview
DiaLights is a set of 3 diamond-shaped panels with strip LEDs lining the insire of the frames. The frames were developed using Autodesk AutoCAD and 3D printed. The panels each consist of 3 layers: a light diffuser to soften LEDs, the 3D printed frame, and a reflective backing to ensure smoothing lighting. 

Arduino Nano is used to control the lights using the [FastLED](https://github.com/FastLED/FastLED) library. The Bluetooth component connected to the Arduino communicates with mobile app Blynk. 

Prior iteration of the project included an IR sensor to allow control with a physical remote. This was removed due to usability issues and a mobile app was swapped in favor of convienence.

## Product List 
- [LAFVIN Nano](https://www.amazon.com/LAFVIN-Board-ATmega328P-Micro-Controller-Arduino/dp/B07G99NNXL/ref=sr_1_2_sspa?crid=2GLCSN9MTIH5X&keywords=lafvin+nano+v3.0%2C+nano+board+atmega328p&qid=1573237159&sprefix=lafvin+nano%2Caps%2C151&sr=8-2-spons&psc=1&spLa=ZW5jcnlwdGVkUXVhbGlmaWVyPUExQlA5VEY2NkdCMzFFJmVuY3J5cHRlZElkPUEwMTk0MzA3WVBXWFFRN0c3WVdCJmVuY3J5cHRlZEFkSWQ9QTA3NDEzMDBGMFZQRkZKTEcxU1cmd2lkZ2V0TmFtZT1zcF9hdGYmYWN0aW9uPWNsaWNrUmVkaXJlY3QmZG9Ob3RMb2dDbGljaz10cnVl "LAFVIN Nano") (requires minor additional set up, lower cost than Arduino nano)
- [RGB LED Strip](https://www.amazon.com/ALITOVE-Addressable-Programmable-Waterproof-Raspberry/dp/B07FVPN3PH/ref=sr_1_2_sspa?keywords=alitove+16.4ft+WS2812b&qid=1573237257&sr=8-2-spons&psc=1&spLa=ZW5jcnlwdGVkUXVhbGlmaWVyPUEyWTY5RU5QTk9JVVM5JmVuY3J5cHRlZElkPUExMDA1MDk5RUZWV1g0VTE3WjVEJmVuY3J5cHRlZEFkSWQ9QTA0ODg4NDIyMEFCNEVRRDFEQ1c4JndpZGdldE5hbWU9c3BfYXRmJmFjdGlvbj1jbGlja1JlZGlyZWN0JmRvTm90TG9nQ2xpY2s9dHJ1ZQ==)
- 3D printed frames (used local Makerspace and [Treatstock](https://www.treatstock.com/))
- [Bluetooth 2.0 Module](https://www.dfrobot.com/product-360.html "Bluetooth 2.0 Module") 
- [Blynk mobile app](https://blynk.io/) (free with premium + paid features)
- Wax paper (diffuser)
- [Transparency film](https://www.amazon.com/OHP-Film-Overhead-Projector-Transparency/dp/B078QYKNKG) (diffuser)

## Schematics & Drawings
### Blynk configuration (retired)
<img src="https://i.imgur.com/rtn2cSx.png" alt="Blynk configuration for DiaLights" width="187" height="341" />

### DiaLights Android Screencaps
<img src="https://i.imgur.com/Apk7QmH.png" alt="Initial screen for DiaLights" width="187" height="341" />
<img src="https://i.imgur.com/reKj9Bb.png" alt="Color picker for DiaLights" width="187" height="341" />
<img src="https://i.imgur.com/zfPpHmL.png" alt="Color selected for DiaLights" width="187" height="341" />

More coming soon! 
