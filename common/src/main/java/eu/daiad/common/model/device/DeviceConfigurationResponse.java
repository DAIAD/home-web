package eu.daiad.common.model.device;

import java.util.ArrayList;
import java.util.List;

import eu.daiad.common.model.RestResponse;

public class DeviceConfigurationResponse extends RestResponse {

	private List<DeviceConfigurationCollection> devices = new ArrayList<DeviceConfigurationCollection>();

	public List<DeviceConfigurationCollection> getDevices() {
		return devices;
	}

	public void setDevices(List<DeviceConfigurationCollection> devices) {
		this.devices = devices;
	}
}
