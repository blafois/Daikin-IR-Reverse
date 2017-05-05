# Daikin-IR-Reverse
Daikin AC Infrared remote control protocol reverse

```
00:	11 da 27 00 00 49 3c 00 
08:	50 00 00 06 60 00 00 c1 
10: 00 8e 
```

```
Offset  Description         Length      Example			Decoding
========================================================================================================
0-3		Header				4?			88 5b e4 00	
5		Mode				1			49				49 = Heat.
6		Temperature			1			30				It is temperature x2. 0x30 = 48 / 2 = 24°C
8		Fan Speed + Swing	1			30				30 = Fan 1/5 No Swing. 3F = Fan 1/5 + Swing. 
12		Checksum			1			8e				Add all previous bytes and do a OR with mask 0xff
```