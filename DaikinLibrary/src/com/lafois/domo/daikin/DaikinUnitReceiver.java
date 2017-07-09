package com.lafois.domo.daikin;


import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lafois.domo.daikin.DaikinUnitState.AC_MODE;
import com.lafois.domo.daikin.DaikinUnitState.FAN_MODE;
import com.lafois.domo.daikin.DaikinUnitState.TIMER_MODE;

public class DaikinUnitReceiver {

	///////////////////////////////////////////////////////

	private Logger logger = LogManager.getLogger(getClass());
	
	///////////////////////////////////////////////////////
	
	private static final int[] DAIKIN_MESSAGE_HEADER = 
		{ 0x11, 0xda, 0x27, 0x00 };
	
	///////////////////////////////////////////////////////
	
	private DaikinUnitState unitState;
	
	private boolean isStateAcurate;

	///////////////////////////////////////////////////////
	
	public DaikinUnitReceiver() {
		isStateAcurate = false;
		unitState = new DaikinUnitState();
	}

	///////////////////////////////////////////////////////
	
	public void onInputMessage(int[] message) {
		String s = "";
		for(int b : message) {
			s+= Integer.toHexString(b) + " ";
		}
		logger.info("New message arrived: {}", BinaryUtils.bytesToString(message));
		
		
		if(message == null || message.length < 8) {
			logger.error("Invalid message arrived. Ignoring.");
			// Invalid Message
			return;
		}
		
		int[] messageHeader = Arrays.copyOfRange(message, 0, 4);
		if(Arrays.equals(DAIKIN_MESSAGE_HEADER, messageHeader)) {
			// Valid daikin message header
			// Is checksum valid ?
			if(isCksumValid(message)) {
				switch(message[4]) {
				case 0xc5: onFrame1Message(message); break;
				case 0x42: onFrame2Message(message); break;
				case 0x00: onFrame3Message(message); break;
				default: break;
				}
			}
			else {
				// Invalid checksum
				logger.error("Invalid message checksum. Ignoring.");
				return;
			}
		}
		else {
			// Unknown header
			logger.error("Unknown message header. Ignoring.");
			return;
		}
	}

	///////////////////////////////////////////////////////

	private void onFrame1Message(int[] message) {
		logger.info("Message of type 1");
		
		if((message[6] & 0x10) == 0) {
			this.unitState.setComfort(false);
		}
		else {
			this.unitState.setComfort(true);
		}
	}

	///////////////////////////////////////////////////////

	private void onFrame2Message(int[] message) {
		logger.info("Message of type 2");
		
		// Nothing to do yet
	}

	///////////////////////////////////////////////////////

	private void onFrame3Message(int[] message) {
		logger.info("Message of type 3");
		
		// BYTE 5
		byte mode = (byte) (message[5] >> 4);
		unitState.setAcMode(AC_MODE.forCode(mode));
		
		if((message[5] & 0x01) == 0x01)
			unitState.setPower(true);
		else
			unitState.setPower(false);
		
		if((message[5] & 0x04) == 0x04)
			unitState.setTimer(TIMER_MODE.OFF);

		if((message[5] & 0x02) == 0x02)
			unitState.setTimer(TIMER_MODE.ON);

		// BYTE 6
		unitState.setTemperature(message[6]/2);
		
		// BYTE 8
		byte fanMode = (byte) (message[8] >> 4);
		if(fanMode == FAN_MODE.SILENT.getCode())
			unitState.setFanMode(FAN_MODE.SILENT);
		else if(fanMode == FAN_MODE.AUTOMATIC.getCode())
			unitState.setFanMode(FAN_MODE.AUTOMATIC);
		else {
			byte fanSpeed = (byte) (fanMode - FAN_MODE.MANUAL.getCode());
			unitState.setFanMode(FAN_MODE.MANUAL);
			unitState.setFanSpeed(fanSpeed);
		}
		
		if((message[8] & 0x0f) == 0x0f) {
			unitState.setSwing(true);
		}
		else {
			unitState.setSwing(false);
		}
		
		// BYTES A AND B AND C
		if(unitState.getTimer() == TIMER_MODE.ON) {
			// A and B
			int timerDuration = (0x100 * message[0xb] + message[0xa]) / 60;
			unitState.setTimerDuration(timerDuration);
		}
		else if(unitState.getTimer() == TIMER_MODE.OFF) {
			// B and C
			int timerDuration = ((message[0xc] << 4) | (message[0xb] >> 4)) / 60;
			unitState.setTimerDuration(timerDuration);
		}
		
		// BYTE D
		if((message[0xd] & 0x01) == 0x01) {
			unitState.setPowerful(true);
		}
		else {
			unitState.setPowerful(false);
		}
		
		// BYTE 10
		if((message[0x10] & 0x04) == 0x04) {
			unitState.setEcono(true);
		}
		else {
			unitState.setEcono(false);
		}
		
		logState();
		
		isStateAcurate = true;
	}

	///////////////////////////////////////////////////////
	
	private void logState() {
		logger.info("Power: {}", unitState.isPower());
		logger.info("AC Mode: {}", unitState.getAcMode());
		logger.info("Fan Mode: {}", unitState.getFanMode());
		
		if(unitState.getFanMode() == FAN_MODE.MANUAL) {
			logger.info("Fan Speed: {}", unitState.getFanSpeed());
		}
		
		logger.info("Swing Mode: {}", unitState.isSwing());
		logger.info("Comfort Mode: {}", unitState.isComfort());
		logger.info("Econo Mode: {}", unitState.isEcono());
		
		if(unitState.isPowerful()) {
			logger.info("Is Powerful: {}", unitState.isPowerful());
		}
		else {
			logger.info("Temperature: {}", unitState.getTemperature());	
		}
		
		logger.info("Timer Mode: {}", unitState.getTimer());
		logger.info("Timer duration: {}", unitState.getTimerDuration());
		
	}

	///////////////////////////////////////////////////////
	
	private boolean isCksumValid(int[] message) {
		int sum = 0;
		for(int i = 0; i < message.length - 1; i++) {
			sum += message[i];
		}
		sum = sum & 0xff;
		
		logger.debug("Computed CS: {}, Received CS: {}", sum, message[message.length-1]);
		
		if(message[message.length-1] < 0) {
			sum -= 256;
		}
		
		return sum == message[message.length-1];
	}

	///////////////////////////////////////////////////////
	
	public boolean isStateAcurate() {
		return isStateAcurate;
	}

	///////////////////////////////////////////////////////
		
}
