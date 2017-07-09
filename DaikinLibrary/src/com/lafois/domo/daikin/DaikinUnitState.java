package com.lafois.domo.daikin;


public class DaikinUnitState {

	///////////////////////////////////////////////////////
	
	public enum AC_MODE {
		HEAT(4),
		COOL(3),
		DRY(2),
		FAN(6),
		AUTO(0);
		
		private int code;
		AC_MODE(int code) {
			this.code = code;
		}
		public int getCode() { return code; }
		public static AC_MODE forCode(byte code) {
			AC_MODE[] allValues = AC_MODE.values();
			for(AC_MODE acm : allValues) {
				if(acm.getCode() == code)
					return acm;
			}
			return null;
		}
	}

	///////////////////////////////////////////////////////
	
	public enum FAN_MODE {
		MANUAL(2),
		AUTOMATIC(0xa),
		SILENT(0xb);
		
		private int code;
		FAN_MODE(int code) { 
			this.code = code;
		}
		public int getCode() { return code; }
	}

	///////////////////////////////////////////////////////
	
	public enum TIMER_MODE {
		ON,OFF
	}
	
	///////////////////////////////////////////////////////
	
	private boolean 	power;
	private AC_MODE 	acMode;
	private FAN_MODE 	fanMode;
	private int 		fanSpeed;
	private boolean 	swing;
	private boolean 	econo;
	private boolean 	comfort;
	private boolean 	powerful;
	private TIMER_MODE 	timer;
	private int 		timerDuration;
	private int 		temperature;

	///////////////////////////////////////////////////////
	
	public DaikinUnitState() {
		this.power 			= false;
		this.temperature	= 20;
		this.fanMode 		= FAN_MODE.AUTOMATIC;
		this.acMode 		= AC_MODE.AUTO;
			
	}

	///////////////////////////////////////////////////////
	
	public boolean isPower() {
		return power;
	}

	public void setPower(boolean power) {
		this.power = power;
	}

	public AC_MODE getAcMode() {
		return acMode;
	}

	public void setAcMode(AC_MODE acMode) {
		this.acMode = acMode;
	}

	public FAN_MODE getFanMode() {
		return fanMode;
	}

	public void setFanMode(FAN_MODE fanMode) {
		this.fanMode = fanMode;
	}

	public int getFanSpeed() {
		return fanSpeed;
	}

	public void setFanSpeed(int fanSpeed) {
		this.fanSpeed = fanSpeed;
	}

	public boolean isSwing() {
		return swing;
	}

	public void setSwing(boolean swing) {
		this.swing = swing;
	}

	public boolean isEcono() {
		return econo;
	}

	public void setEcono(boolean econo) {
		this.econo = econo;
	}

	public boolean isComfort() {
		return comfort;
	}

	public void setComfort(boolean comfort) {
		this.comfort = comfort;
	}

	public boolean isPowerful() {
		return powerful;
	}

	public void setPowerful(boolean powerful) {
		this.powerful = powerful;
	}

	public TIMER_MODE getTimer() {
		return timer;
	}

	public void setTimer(TIMER_MODE timer) {
		this.timer = timer;
	}

	public int getTimerDuration() {
		return timerDuration;
	}

	public void setTimerDuration(int timerDuration) {
		this.timerDuration = timerDuration;
	}

	public int getTemperature() {
		return temperature;
	}

	public void setTemperature(int temperature) {
		this.temperature = temperature;
	}

	///////////////////////////////////////////////////////
	
}
