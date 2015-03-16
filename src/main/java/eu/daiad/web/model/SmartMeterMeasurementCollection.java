package eu.daiad.web.model;

import java.util.ArrayList;
import java.util.UUID;

public class SmartMeterMeasurementCollection {

	private UUID applicationKey;
	
	private String deviceId;
	
	private ArrayList<SmartMeterMeasurement> measurements;
	
	public void setApplicationKey(UUID value) {
		this.applicationKey = value;
	}
	
	public UUID getApplicationKey() {
		return this.applicationKey;
	}
	
	public void setDeviceId(String value) {
		this.deviceId = value;
	}
	
	public String getDeviceId() {
		return this.deviceId;
	}
	
	public void setMeasurements(ArrayList<SmartMeterMeasurement> value) {
		this.measurements = value;
	}
	
	public ArrayList<SmartMeterMeasurement> getMeasurements() {
		return this.measurements;
	}
	
}
