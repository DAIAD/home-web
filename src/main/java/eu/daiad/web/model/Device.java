package eu.daiad.web.model;

import java.util.ArrayList;
import java.util.UUID;

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
		this.properties = properties;
		if (this.properties == null) {
			this.properties = new ArrayList<KeyValuePair>();
		}
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

	public void setProperties(ArrayList<KeyValuePair> value) {
		this.properties = value;
		if (this.properties == null) {
			this.properties = new ArrayList<KeyValuePair>();
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
