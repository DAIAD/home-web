package eu.daiad.web.model;

import java.util.ArrayList;

public class MeterDeviceRegistrationRequest extends DeviceRegistrationRequest {

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
