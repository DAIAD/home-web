package eu.daiad.web.model;

import java.util.UUID;

public class WaterMeterStatus {

	private UUID deviceKey;
	
	private SmartMeterDataPoint value1;

	private SmartMeterDataPoint value2;

	public UUID getDeviceKey() {
		return deviceKey;
	}

	public void setDeviceKey(UUID deviceKey) {
		this.deviceKey = deviceKey;
	}

	public SmartMeterDataPoint getValue1() {
		return value1;
	}

	public void setValue1(SmartMeterDataPoint value) {
		this.value1 = value;
	}

	public SmartMeterDataPoint getValue2() {
		return value2;
	}

	public void setValue2(SmartMeterDataPoint value) {
		this.value2 = value;
	}
	
}
