package eu.daiad.web.model;

import java.util.ArrayList;

public class WaterMeterMeasurementCollection extends DeviceMeasurementCollection {

	private ArrayList<WaterMeterMeasurement> measurements;

	public void setMeasurements(ArrayList<WaterMeterMeasurement> value) {
		this.measurements = value;
	}

	public ArrayList<WaterMeterMeasurement> getMeasurements() {
		return this.measurements;
	}

	@Override
	public EnumDeviceType getType() {
		return EnumDeviceType.METER;
	}

}
