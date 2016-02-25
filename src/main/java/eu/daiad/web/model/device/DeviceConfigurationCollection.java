package eu.daiad.web.model.device;

import java.util.ArrayList;
import java.util.UUID;

public class DeviceConfigurationCollection {

	private UUID key;

	private String macAddress;

	private ArrayList<DeviceAmphiroConfiguration> configurations = new ArrayList<DeviceAmphiroConfiguration>();

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

	public ArrayList<DeviceAmphiroConfiguration> getConfigurations() {
		return configurations;
	}
}
