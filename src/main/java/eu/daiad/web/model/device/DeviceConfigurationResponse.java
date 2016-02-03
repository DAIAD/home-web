package eu.daiad.web.model.device;

import java.util.ArrayList;

import eu.daiad.web.model.RestResponse;

public class DeviceConfigurationResponse extends RestResponse {

	private ArrayList<DeviceConfiguration> devices = new ArrayList<DeviceConfiguration>();

	public ArrayList<DeviceConfiguration> getDevices() {
		return devices;
	}

	public void setDevices(ArrayList<DeviceConfiguration> devices) {
		this.devices = devices;
	}
}
