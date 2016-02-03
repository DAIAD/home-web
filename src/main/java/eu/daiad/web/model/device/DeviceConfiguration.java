package eu.daiad.web.model.device;

import java.util.ArrayList;
import java.util.UUID;

import eu.daiad.web.model.KeyValuePair;

public class DeviceConfiguration {

	private UUID key;
	
	private String macAddress;
	
	private ArrayList<KeyValuePair> properties = new ArrayList<KeyValuePair>();

	public ArrayList<KeyValuePair> getProperties() {
		return properties;
	}
	
	public void add(String key, String value) {
		this.properties.add(new KeyValuePair(key, value));
	}

	public UUID getKey() {
		return key;
	}

	public void setKey(UUID key) {
		this.key = key;
	}

	public String getMacAddress() {
		return macAddress;
	}

	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}
	
}
