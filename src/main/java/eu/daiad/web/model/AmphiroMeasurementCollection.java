package eu.daiad.web.model;

import java.util.ArrayList;

public class AmphiroMeasurementCollection extends DeviceMeasurementCollection {

	private ArrayList<AmphiroSession> sessions;
	
	private ArrayList<AmphiroMeasurement> measurements;

	public void setSessions(ArrayList<AmphiroSession> value) {
		this.sessions = value;
	}
	
	public ArrayList<AmphiroSession> getSessions() {
		return this.sessions;
	}

	public void setMeasurements(ArrayList<AmphiroMeasurement> value) {
		this.measurements = value;
	}
	
	public ArrayList<AmphiroMeasurement> getMeasurements() {
		return this.measurements;
	}
	
	@Override public EnumDeviceType getType() {
		return EnumDeviceType.AMPHIRO;
	}

}
