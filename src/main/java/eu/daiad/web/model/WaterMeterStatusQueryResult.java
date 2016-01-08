package eu.daiad.web.model;

import java.util.ArrayList;

public class WaterMeterStatusQueryResult extends RestResponse {

	private ArrayList<WaterMeterStatus> devices = null;

	public WaterMeterStatusQueryResult() {
		super();

		this.devices = new ArrayList<WaterMeterStatus>();
	}

	public WaterMeterStatusQueryResult(int code, String description) {
		super(code, description);
	}

	public ArrayList<WaterMeterStatus> getDevices() {
		return devices;
	}

	public void setDevices(ArrayList<WaterMeterStatus> value) {
		this.devices = value;
	}

}
