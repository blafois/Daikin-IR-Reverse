package com.lafois.domo.daikin;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lafois.domo.daikin.DaikinUnitState.AC_MODE;
import com.lafois.domo.daikin.DaikinUnitState.FAN_MODE;
import com.lafois.domo.daikin.DaikinUnitState.TIMER_MODE;

public class DaikinUnitTransmitter {

	///////////////////////////////////////////////////////

	private Logger logger = LogManager.getLogger(getClass());
	
	///////////////////////////////////////////////////////

	
	///////////////////////////////////////////////////////

	private DaikinUnitState unitState;

	private boolean autoTransmit = true;
	private boolean isStateAcurate;

	private long timerStartTime;
	private long powerfulStartTime;

	///////////////////////////////////////////////////////

	/**
	 * Initial state
	 * Power: OFF
	 * Temperature: 20
	 * Fan: AUTO
	 * Mode: AUTO
	 */
	public DaikinUnitTransmitter(boolean autoTransmit) {
		this.unitState 		= new DaikinUnitState();
		this.autoTransmit 	= autoTransmit;
		isStateAcurate 		= false;	
	}

	///////////////////////////////////////////////////////

	public void powerOn() { 
		this.unitState.setPower(true);

		if(autoTransmit) transmit();
	}

	///////////////////////////////////////////////////////	

	public void powerOff() {
		this.unitState.setPower(false);

		if(autoTransmit) transmit();
	}

	///////////////////////////////////////////////////////	

	public void setAcMode(AC_MODE mode) { 
		this.unitState.setAcMode(mode);

		if(autoTransmit) transmit();
	}

	///////////////////////////////////////////////////////	

	public void setFanMode(FAN_MODE mode, int speed) {
		// If mode == MANUAL, speed should be provided, >= 1 and <= 5
		if(mode == FAN_MODE.MANUAL) {
			if(speed >= 1 && speed <= 5) {
				this.unitState.setFanMode(mode);
				this.unitState.setFanSpeed(speed);
			}
			else {
				return;
			}
		}
		else {
			this.unitState.setFanMode(mode);
		}

		if(autoTransmit) transmit();
	}

	///////////////////////////////////////////////////////	

	public void setSwing(boolean swing) {
		this.unitState.setSwing(swing);

		if(autoTransmit) transmit();
	}

	///////////////////////////////////////////////////////	

	public void setComfort(boolean comfort) { 
		this.unitState.setComfort(comfort);

		if(autoTransmit) transmit();
	}

	///////////////////////////////////////////////////////	

	public void setEcono(boolean econo) { 
		this.unitState.setEcono(econo);

		if(autoTransmit) transmit();
	}

	///////////////////////////////////////////////////////	

	/**
	 * Enables maximum power on current mode for 20 minutes
	 * @param on if "true", enables it. Otherwize, disables it.
	 */
	public void setPowerful(boolean powerful) { 
		if(powerful) {
			powerfulStartTime = System.currentTimeMillis();
		}

		this.unitState.setPowerful(powerful);

		if(autoTransmit) transmit();
	}

	///////////////////////////////////////////////////////	
	
	public void setPower(boolean power) {
		this.unitState.setPower(power);
		
		if(autoTransmit) transmit();
	}

	///////////////////////////////////////////////////////	

	public void setTimer(TIMER_MODE mode, int duration) {
		if(duration < 1 || duration > 9) {
			return;
		}

		this.unitState.setTimer(mode);
		this.unitState.setTimerDuration(duration);

		timerStartTime = System.currentTimeMillis();
		if(autoTransmit) transmit();
	}

	///////////////////////////////////////////////////////	

	public void cancelTimer() { }

	///////////////////////////////////////////////////////	

	public void setTemperature(int temperature) { 
		// Should be >= 10 and <= 30
		if(temperature >= 10 && temperature <= 30) {
			this.unitState.setTemperature(temperature);
		}

		if(autoTransmit) transmit();
	}

	///////////////////////////////////////////////////////

	/**
	 * Transmit order to AC unit, according to current values
	 */
	public void transmit() {
		isStateAcurate = true;

		// Frame 1
		int[] frame1 = buildFrame1();

		// Frame 2
		int[] frame2 = buildFrame2();

		// Frame 3
		int[] frame3 = buildFrame3();
		
		
		logger.debug("Frame1: {}", BinaryUtils.bytesToString(frame1));
		logger.debug("Frame2: {}", BinaryUtils.bytesToString(frame2));
		logger.debug("Frame3: {}", BinaryUtils.bytesToString(frame3));
		
	}

	///////////////////////////////////////////////////////

	private int computeChecksum(int[] frame) {
		int cs = 0;
		
		for(int i = 0; i < frame.length; i++) {
			cs += frame[i];
		}
		
		return (cs & 0x00ff);
	}

	///////////////////////////////////////////////////////
	
	private int[] buildFrame1() {
		int[] frame = new int[8];

		// Header
		frame[0] = 0x11;
		frame[1] = 0xda;
		frame[2] = 0x27;
		frame[3] = 0x00;
		// Message ID
		frame[4] = 0xc5;

		// Comfort mode
		if(unitState.isComfort()) {
			frame[6] = 0x10;
		}
		
		// Checksum
		frame[7] = computeChecksum(frame);

		return frame;
	}

	private int[] buildFrame2() {
		int[] frame = new int[8];

		// Header
		frame[0] = 0x11;
		frame[1] = 0xda;
		frame[2] = 0x27;
		frame[3] = 0x00;
		// Message ID
		frame[4] = 0x42;

		// Checksum
		frame[7] = computeChecksum(frame);

		return frame;
	}

	private int[] buildFrame3() {
		int[] frame = new int[19];

		// Header
		frame[0] = 0x11;
		frame[1] = 0xda;
		frame[2] = 0x27;
		frame[3] = 0x00;

		// Message ID
		frame[4] = 0x00;

		// Mode, On-off, timers
		frame[5] = (this.unitState.getAcMode().getCode() << 4) | 0x08;
		if(this.unitState.isPower())
		{	
			frame[5] = frame[5] | 0x01;
		}
		if(this.unitState.getTimer() != null) {
			if(this.unitState.getTimer() == TIMER_MODE.OFF) {
				frame[5] = frame[5] | 0x04;
			}
			else if(this.unitState.getTimer() == TIMER_MODE.ON) {
				frame[5] = frame[5] | 0x02;
			}
		}

		// Temperature
		frame[6] = unitState.getTemperature() * 2;

		// Fan Swing
		frame[8] = 0x00;
		if(unitState.getFanMode() == FAN_MODE.MANUAL) {
			frame[8] = (FAN_MODE.MANUAL.getCode() + unitState.getFanSpeed() << 4);
		}
		else {
			frame[8] = (unitState.getFanMode().getCode() << 4);
		}
		if(unitState.isSwing()) {
			frame[8] = frame[8] | 0x0f;
		}

		// Timer Delay
		frame[0x0a] = 0x00;
		frame[0x0b] = 0x00;
		frame[0x0c] = 0x00;
		
//		if(unitState.getTimer() == null) {
//			frame[0x0b] = 0x06;
//			frame[0x0c] = 0x60;	
//		}
		
		frame[0x0b] = frame[0x0b] | 0x06;
		frame[0x0c] = frame[0x0c] | 0x60;
		
		if(unitState.getTimer() == TIMER_MODE.OFF) {
			int durMin = unitState.getTimerDuration() * 60;
			if(durMin > 0xff) {
				frame[0x0c] = durMin >> 8;
				frame[0x0b] = durMin & 0xff;
			}
			else {
				frame[0x0c] = durMin >> 4;
				frame[0x0b] = frame[0x0b] | (durMin & 0x0f) << 4;
			}
		}
		else if(unitState.getTimer() == TIMER_MODE.ON) {
			int durMin = unitState.getTimerDuration() * 60;
			frame[0x0a] = durMin & 0xff;
			frame[0x0b] = (durMin >> 8) & 0xff;
		}
		
		
		// Powerful
		frame[0x0d] = 0x00;

		// Fixed
		frame[0x0f] = 0xc1;

		// Econo
		frame[0x10] = 0x80;
		if(unitState.isEcono()) {
			frame[0x10] = frame[0x10] | 0x04;
		}

		// Checksum
		frame[0x12] = computeChecksum(frame);

		return frame;
	}

	///////////////////////////////////////////////////////

	public boolean isAutoTransmit() {
		return autoTransmit;
	}

	public long getTimerStartTime() {
		return timerStartTime;
	}

	public long getPowerfulStartTime() {
		return powerfulStartTime;
	}

	///////////////////////////////////////////////////////	

}
