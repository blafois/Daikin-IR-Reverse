# Daikin-IR-Reverse

## Motivation

The motivation of reversing the Daikin Infrared protocol was to enable my AC system in HomeKit using HomeBridge.

## Approach


To reverse this protocol, I used an Arduino Uno, 38Khz Vishay infrared receiver (Chinese ebay copy), a simple Sketch and a Java program to plot the data and decode RAW data issued from Arduino.
Of course, I used a Daikin infrared remote control: **ARC470A1**
The Arduino sketch is very basic, and stores the duration in microseconds of states (1 or 0). It waits for a button being started and IR transmission to start, and stops after a timeout.

![Arduino Wiring](https://github.com/blafois/Daikin-IR-Reverse/raw/master/doc/Analysis-Sketch.png)

## Analysis

Each remote-control transmission produces 3 frames. The 2 first frames are 8 bytes long while the 3rd one is 19 bytes.

The IR protocol is using Pulse Distance Encoding. This was guessed by observing the HIGH states duration is the same all the time.
Measures made with Arduino are as follow (not necessarily accurate, Arduino is not a precise logic analyzer!).

| State | Duration (microseconds) |
| ------------- | -----:|
| HIGH      | 412-444 |
| LOW LONG (1)  | 1496-1536 |
| LOW SHORT (0) | 412-444 |

Bytes are coded using Least Significant Bit (LSB).

## Protocol documentation

Based on multiple tests, here is what I was able to determine on the protocol used by Daikin.

### Frame 1 (Code 0xc5)

The first frame is always the same, except when "comfort" mode is enabled.
```
11 da 27 00 c5 00 00 d7 
```

```
Offset  Description            Length     Example        Decoding
========================================================================================================
0-3     Header                 4          11 da 27 00
6       Comfort mode           1          10
7       Checksum               1          d7	
```

Comfort mode:
```
11 da 27 00 c5 00 10 e7       Comfort mode ENABLED
11 da 27 00 c5 00 00 d7       Comfort mode DISABLED
```

### Frame 2 (Code 0x42)

```
11 da 27 00 42 00 00 54 
```

### Frame 3 (Code 0x00)

Sample frame transmitted:
```
11 da 27 00 00 49 3c 00 50 00 00 06 60 00 00 c1 80 00 8e
```

```
00:     11 da 27 00 00 49 3c 00 
08:     50 00 00 06 60 00 00 c1 
10:     80 00 8e 
```

Frame specification:
```
Offset  Description            Length     Example        Decoding
========================================================================================================
00-03   Header                 4          11 da 27 00
04      Message Identifier     1          00	
05      Mode, On/Off, Timer    1          49             49 = Heat, On, No Timer
06      Temperature            1          30             It is temperature x2. 0x30 = 48 / 2 = 24°C
08      Fan / Swing / Comfort  1          30             30 = Fan 1/5 No Swing. 3F = Fan 1/5 + Swing. 
0a-0c   Timer delay            3          3c 00 60           
0d      Powerful               1          01             Powerful enabled
10      Econo                  1          84             4 last bits
12      Checksum               1          8e             Add all previous bytes and do a OR with mask 0xff
```

#### Mode
The various mode supported by my remote control are:
* Heater
* Cooler
* Fan
* Automatic
* Dry

Only the first 4 bits of this byte is changing:

```
0-   AUTO
2-   DRY
3-   COLD
4-   HEAT
6-   FAN
```

**Nota bene:** in *DRY* and *FAN* mode, the temperature transmitted is 25°C (it is not relevant).

#### Timers and Power

To manage the On/Off state as well as timer modes, the last 4 bits are used.

```
Bit     Value    Description
=======================================
0       8        Always "1"        
1       4        Timer OFF enabled
2       2        Timer ON enabled
3       1        Current State. 1 = On, 0 = Off
```

The timer mode can be "delayed power on" or "delayed power off". The timer delay is explained later.

Few examples:
```
-8   Off, No timer
-9   On, No timer
-a   Off, Timer "On"
-c   Off, Timer "Off"
-d   On, Timer "Off"
```

#### Temperature
My remote control supports temperature between 10 and 30 degrees. Coding of temperature is quite easy to reverse: take the temperature in Celsius, multiply by 2, and code it in heax.
For example:

```
Desired temperature in °C:     20
Multiply it by 2:              40
Convert it in hex:           0x28
```

#### Fan Speed, Swing, Commfort
The remote control supports 3 modes:
* Manual from 1 to 5
* Silent
* Auto

The remote also has a "Swing" mode.

The top 4 bits are used to code the fan-mode, and the 4 last bits are used to code the swing function.

Modes:
```
3    Fan 1/5
4    Fan 2/5
5    Fan 3/5
6    Fan 4/5
7    Fan 5/5
A    Automatic	
B    Silent	
```

Swing:
```
0    Swing disabled
F    Swing enabled
```

The comfort mode automatically sets the position of air flow. It also sets the fan speed to Automatic.
```

```

Few examples
```
3F   Fan 1/5 and Swing enabled
70   Fan 5/5 and Swing disabled
```

#### Timer

There are 2 times: timer for Power On and timer for Power Off.
The type of timer is coded at offset 5

The timer delay position and coding defers depending of timer type.

For Timer *ON*, the delay is on offset 0a and 0b, with the following coding:

Few examples:
```
4H = 4 * 60 = 240 minutes = 0x00f0. This will be coded as f0 00
5H = 5 * 60 = 300 minutes = 0x012c. This will be coded as 2c 01
```

In Java, this can be decoded using the following code snippet:
```
int timerDuration = (0x100 * message[0xb] + message[0xa]) / 60;
```

For Timer *OFF*, the delay is on offset 0b and 0c, with the following coding:

Few examples:
```
1H = 1 * 60 = 60 minutes = 0x003C. This will be coded as c6 03
5H = 5 * 60 = 300 minutes = 0x012c. This will be coded as 2c 01
```

In Java, this can be decoded using the following code snippet:
```Java
int timerDuration = ((message[0xc] << 4) | (message[0xb] >> 4)) / 60;
``


#### Powerful

The Powerful mode sets the AC at his maximum capacity for 20 minutes, and then reverts back to the previous state.
The previous state is transmitted in the frame. So basically, the Powerful state is just 1 bit in addition to a normal frame.

For example, the following frame will set the powerful mode during 20 minutes in heat state, and then will heat normally at 20°C with fan speed 3:
```
11 da 27 00 00 49 28 00 50 00 00 06 60 01 00 c1 80 00 7b 
```

#### Econo

The 4 last bits are used to determine if econo mode is enabled or not

```
80   Econo is DISABLED
84   Econo is ENABLED
```

#### Checksum
The last byte of frame is a checksum. All previous bytes are added and only the 2 bytes are kept. If you compute it with modern languages, simply apply 0xFF mask.

Example with this frame:
```
11 da 27 00 00 49 3c 00 50 00 00 06 60 00 00 c1 80 00
```

```
11 + +da + 27 + 00 + 00 + 49 + 3c + 00 + 50 + 00 + 00 + 06 + 60 + 00 + 00 + c1 + 80 + 00 
   = 38e

  03 8e
& 00 ff 
   = 8e 
```


