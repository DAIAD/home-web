package eu.daiad.web.model.device;

import java.util.ArrayList;

import eu.daiad.web.model.RestResponse;

public class DeviceRegistrationQueryResult extends RestResponse {

	private ArrayList<Device> devices;

	public DeviceRegistrationQueryResult() {
		this.devices = new ArrayList<Device>();
	}

	public ArrayList<Device> getDevices() {
		return devices;
	}

	public void setDevices(ArrayList<Device> devices) {
		if (devices == null) {
			this.devices = new ArrayList<Device>();
		} else {
			this.devices = devices;
		}
	}

}
