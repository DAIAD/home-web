package eu.daiad.web.model;

import java.util.ArrayList;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import eu.daiad.web.util.DeviceTypeDeserializer;

public class DeviceRegistrationRequest extends AuthenticatedRequest {

	@JsonDeserialize(using = DeviceTypeDeserializer.class)
	private EnumDeviceType type;
	
	private String deviceId;
	
	private String name;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	private ArrayList<KeyValuePair> properties;
	
	public String getDeviceId() {
		return this.deviceId;
	}
	
	public void setDeviceId(String value) {
		this.deviceId = value;
	}

	public ArrayList<KeyValuePair> getProperties() {
		return this.properties;
	}
	
	public void setProperties(ArrayList<KeyValuePair> value) {
		this.properties = value;
	}

	public EnumDeviceType getType() {
		return this.type;
	}
	
	public void setType(EnumDeviceType value) {
		this.type = value;
	}
}
