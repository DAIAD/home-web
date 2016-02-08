package eu.daiad.web.model.meter;

import java.util.ArrayList;

import eu.daiad.web.model.RestResponse;

public class WaterMeterStatusQueryResult extends RestResponse {

	private ArrayList<WaterMeterStatus> devices = null;

	public WaterMeterStatusQueryResult() {
		super();

		this.devices = new ArrayList<WaterMeterStatus>();
	}

	public WaterMeterStatusQueryResult(String code, String description) {
		super(code, description);
	}

	public ArrayList<WaterMeterStatus> getDevices() {
		return devices;
	}

	public void setDevices(ArrayList<WaterMeterStatus> value) {
		this.devices = value;
	}

}
