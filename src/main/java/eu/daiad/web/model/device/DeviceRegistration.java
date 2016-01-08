package eu.daiad.web.model.device;

import java.util.ArrayList;
import java.util.UUID;

import eu.daiad.web.model.KeyValuePair;

public abstract class DeviceRegistration {

	private UUID deviceKey;

	private String deviceId;

	private ArrayList<KeyValuePair> properties;

	public DeviceRegistration() {
		this.properties = new ArrayList<KeyValuePair>();
	}

	public UUID getDeviceKey() {
		return deviceKey;
	}

	public void setDeviceKey(UUID deviceKey) {
		this.deviceKey = deviceKey;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public ArrayList<KeyValuePair> getProperties() {
		return properties;
	}

	public void setProperties(ArrayList<KeyValuePair> properties) {
		this.properties = properties;
		if (this.properties == null) {
			this.properties = new ArrayList<KeyValuePair>();
		}
	}

	public abstract EnumDeviceType getType();
}
