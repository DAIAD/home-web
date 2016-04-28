package eu.daiad.web.model.amphiro;

import java.util.ArrayList;

import eu.daiad.web.model.DeviceMeasurementCollection;
import eu.daiad.web.model.device.EnumDeviceType;

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

	@Override
	public EnumDeviceType getType() {
		return EnumDeviceType.AMPHIRO;
	}

	public void removeSession(int index) {
		if (measurements != null) {
			for (int i = measurements.size() - 1; i >= 0; i--) {
				if (measurements.get(i).getSessionId() == sessions.get(index).getId()) {
					measurements.remove(i);
				}
			}
		}
		sessions.remove(index);
	}

}
