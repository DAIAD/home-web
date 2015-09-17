package eu.daiad.web.model;

import java.util.ArrayList;

public class SessionCollectionResult extends RestResponse {

	ArrayList<DeviceSessionCollection> devices = null;

	public SessionCollectionResult() {
		super();

		this.devices = new ArrayList<DeviceSessionCollection>();
	}

	public SessionCollectionResult(int code, String description) {
		super(code, description);
	}

	public ArrayList<DeviceSessionCollection> getDevices() {
		return devices;
	}

	public void setDevices(ArrayList<DeviceSessionCollection> devices) {
		this.devices = devices;
	}

}
