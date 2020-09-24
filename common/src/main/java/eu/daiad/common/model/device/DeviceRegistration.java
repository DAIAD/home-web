package eu.daiad.common.model.device;

import java.util.ArrayList;
import java.util.UUID;

import eu.daiad.common.model.KeyValuePair;

public abstract class DeviceRegistration {

	private UUID deviceKey;

	private long registeredOn;

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

	public long getRegisteredOn() {
		return registeredOn;
	}

	public void setRegisteredOn(long registeredOn) {
		this.registeredOn = registeredOn;
	}
}
