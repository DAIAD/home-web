package eu.daiad.web.model;

import java.util.ArrayList;
import java.util.UUID;

public abstract class Device {

	private String id;
	
	private UUID key;
	
	private String name;
	
	public Device(String id, UUID key, String name) {
		this.id = id;
		this.key = key;
		this.name = name;
	}

	public UUID getKey() {
		return key;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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
	}

	public abstract EnumDeviceType getType();

	@Override
	public String toString() {
		return "Device [id=" + id + ", key=" + key + ", name=" + name
				+ ", properties=" + properties + "]";
	}
}
