package eu.daiad.web.model.device;

import java.util.ArrayList;
import java.util.List;

import eu.daiad.web.model.RestResponse;

public class DeviceRegistrationQueryResult extends RestResponse {

	private List<Device> devices;

	public DeviceRegistrationQueryResult() {
		this.devices = new ArrayList<Device>();
	}

	public List<Device> getDevices() {
		return devices;
	}

	public void setDevices(List<Device> devices) {
		if (devices == null) {
			this.devices = new ArrayList<Device>();
		} else {
			this.devices = devices;
		}
	}

}
