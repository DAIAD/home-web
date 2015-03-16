package eu.daiad.web.model;

import java.util.UUID;
import java.util.ArrayList;

public class MeasurementCollection {

	private UUID applicationKey;
	
	private UUID deviceId;
	
	private ArrayList<Measurement> measurements;
	
	public void setApplicationKey(UUID value) {
		this.applicationKey = value;
	}
	
	public UUID getApplicationKey() {
		return this.applicationKey;
	}
	
	public void setDeviceId(UUID value) {
		this.deviceId = value;
	}
	
	public UUID getDeviceId() {
		return this.deviceId;
	}
	
	public void setMeasurements(ArrayList<Measurement> value) {
		this.measurements = value;
	}
	
	public ArrayList<Measurement> getMeasurements() {
		return this.measurements;
	}
}
