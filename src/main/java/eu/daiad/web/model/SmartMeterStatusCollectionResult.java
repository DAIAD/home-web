package eu.daiad.web.model;

import java.util.ArrayList;

public class SmartMeterStatusCollectionResult extends RestResponse {

	private ArrayList<WaterMeterStatus> devices = null;

	public SmartMeterStatusCollectionResult() {
		super();

		this.devices = new ArrayList<WaterMeterStatus>();
	}

	public SmartMeterStatusCollectionResult(int code, String description) {
		super(code, description);
	}

	public ArrayList<WaterMeterStatus> getDevices() {
		return devices;
	}

	public void setDevices(ArrayList<WaterMeterStatus> value) {
		this.devices = value;
	}

}
