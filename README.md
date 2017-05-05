# Daikin-IR-Reverse

## Motivation

The motivation of reversing the Daikin Infrared protocol was to enable my AC system in HomeKit using HomeBridge.

## Approach

Daikin AC Infrared remote control protocol reverse

To reverse this protocol, I used an Arduino Uno, 38Khz Vishay infrared receiver (Chinese ebay copy), a simple Sketch and a Java program to plot the data and decode RAW data issued from Arduino.

The Arduino sketch is very basic, and stores the duration in microseconds of states (1 or 0). It waits for a button being started and IR transmission to start, and stops after a timeout.

## Analysis

Each remote-control transmission produces 3 frames. The 2 first frames are X bytes long while the 3rd one is 19 bytes.

The IR protocol is using Pulse Distance Encoding. This was guessed by observing the HIGH states duration is the same all the time.
Measures made with Arduino are as follow (not necessarily accurate, Arduino is not a precise logic analyzer!).

| STATE | Duration (microseconds) |
| ------------- | -----:|
| HIGH      | 412-444 |
| LOW LONG (1)  | 1496-1536 |
| LOW SHORT (0) | 412-444 |

Bytes are coded using Least Significant Bit (LSB).

11 da 27 00 00 49 3c 00 50 00 00 06 60 00 00 c1 80 00 8e 
```
00:     11 da 27 00 00 49 3c 00 
08:     50 00 00 06 60 00 00 c1 
10:     80 00 8e 
```

```
Offset  Description         Length     Example        Decoding
========================================================================================================
0-3     Header              4?         88 5b e4 00	
5       Mode                1          49             49 = Heat.
6       Temperature         1          30             It is temperature x2. 0x30 = 48 / 2 = 24°C
8       Fan Speed + Swing   1          30             30 = Fan 1/5 No Swing. 3F = Fan 1/5 + Swing. 
12      Checksum            1          8e             Add all previous bytes and do a OR with mask 0xff
```



