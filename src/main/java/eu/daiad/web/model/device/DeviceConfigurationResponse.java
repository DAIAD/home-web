package eu.daiad.web.model.device;

import java.util.ArrayList;

import eu.daiad.web.model.RestResponse;

public class DeviceConfigurationResponse extends RestResponse {

	private ArrayList<DeviceConfigurationCollection> devices = new ArrayList<DeviceConfigurationCollection>();

	public ArrayList<DeviceConfigurationCollection> getDevices() {
		return devices;
	}

	public void setDevices(ArrayList<DeviceConfigurationCollection> devices) {
		this.devices = devices;
	}
}
