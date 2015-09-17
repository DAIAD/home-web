package eu.daiad.web.model;

import java.util.ArrayList;

public class AmphiroDeviceRegistrationRequest extends DeviceRegistrationRequest {

	private String name;

	private ArrayList<KeyValuePair> properties;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ArrayList<KeyValuePair> getProperties() {
		return this.properties;
	}

	public void setProperties(ArrayList<KeyValuePair> value) {
		this.properties = value;
	}
	
	@Override public EnumDeviceType getType() {
		return EnumDeviceType.AMPHIRO;
	}
}
