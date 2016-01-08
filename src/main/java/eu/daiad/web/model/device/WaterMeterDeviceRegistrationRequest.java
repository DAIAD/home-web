package eu.daiad.web.model.device;

import java.util.ArrayList;

import eu.daiad.web.model.KeyValuePair;

public class WaterMeterDeviceRegistrationRequest extends DeviceRegistrationRequest {

	private ArrayList<KeyValuePair> properties;

	public ArrayList<KeyValuePair> getProperties() {
		return this.properties;
	}

	public void setProperties(ArrayList<KeyValuePair> value) {
		this.properties = value;
	}

	@Override
	public EnumDeviceType getType() {
		return EnumDeviceType.METER;
	}
}
