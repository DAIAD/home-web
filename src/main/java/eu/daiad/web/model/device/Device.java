package eu.daiad.web.model.device;

import java.util.ArrayList;
import java.util.UUID;

import eu.daiad.web.model.KeyValuePair;

public abstract class Device {

	private UUID key;

	private String id;

	public Device(UUID key, String id) {
		this.id = id;
		this.key = key;
		this.properties = new ArrayList<KeyValuePair>();
	}

	public Device(UUID key, String id, ArrayList<KeyValuePair> properties) {
		this.id = id;
		this.key = key;
		this.setProperties(properties);
	}

	public UUID getKey() {
		return key;
	}

	private ArrayList<KeyValuePair> properties;

	public String getDeviceId() {
		return this.id;
	}

	public ArrayList<KeyValuePair> getProperties() {
		return this.properties;
	}

	public void setProperties(ArrayList<KeyValuePair> properties) {
		if (properties == null) {
			this.properties = new ArrayList<KeyValuePair>();
		} else {
			this.properties = properties;
		}
	}

	public abstract EnumDeviceType getType();

	public abstract DeviceRegistration toDeviceRegistration();

	@Override
	public String toString() {
		return "Device [id=" + id + ", key=" + key + ", properties="
				+ properties + "]";
	}
}
