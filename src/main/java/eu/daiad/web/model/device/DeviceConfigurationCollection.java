package eu.daiad.web.model.device;

import java.util.ArrayList;
import java.util.UUID;

public class DeviceConfigurationCollection {

	private UUID key;

	private String macAddress;

	private ArrayList<DeviceConfiguration> configurations = new ArrayList<DeviceConfiguration>();

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

	public ArrayList<DeviceConfiguration> getConfigurations() {
		return configurations;
	}
}
