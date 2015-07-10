package eu.daiad.web.model;

import java.util.ArrayList;
import java.util.UUID;

public class DeviceMeasurementCollection extends MeasurementCollection {

	private UUID deviceKey;
	
	private ArrayList<SessionData> sessions;
	
	private ArrayList<Measurement> measurements;

	public void setDeviceKey(UUID value) {
		this.deviceKey = value;
	}
	
	public UUID getDeviceKey() {
		return this.deviceKey;
	}
	
	public void setSessions(ArrayList<SessionData> value) {
		this.sessions = value;
	}
	
	public ArrayList<SessionData> getSessions() {
		return this.sessions;
	}

	public void setMeasurements(ArrayList<Measurement> value) {
		this.measurements = value;
	}
	
	public ArrayList<Measurement> getMeasurements() {
		return this.measurements;
	}
	
	@Override public EnumDeviceType getType() {
		return EnumDeviceType.AMPHIRO;
	}

}
