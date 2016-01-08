package eu.daiad.web.model.meter;

import java.util.ArrayList;

import eu.daiad.web.model.DeviceMeasurementCollection;
import eu.daiad.web.model.device.EnumDeviceType;

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
