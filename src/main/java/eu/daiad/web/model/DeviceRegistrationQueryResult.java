package eu.daiad.web.model;

import java.util.ArrayList;

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
