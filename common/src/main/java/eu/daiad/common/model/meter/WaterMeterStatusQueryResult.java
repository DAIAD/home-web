package eu.daiad.common.model.meter;

import java.util.ArrayList;

import eu.daiad.common.model.RestResponse;
import eu.daiad.common.model.error.ErrorCode;

public class WaterMeterStatusQueryResult extends RestResponse {

	private ArrayList<WaterMeterStatus> devices = null;

	public WaterMeterStatusQueryResult() {
		super();

		this.devices = new ArrayList<WaterMeterStatus>();
	}

	public WaterMeterStatusQueryResult(ErrorCode code, String description) {
		super(code, description);
	}

	public ArrayList<WaterMeterStatus> getDevices() {
		return devices;
	}

	public void setDevices(ArrayList<WaterMeterStatus> value) {
		this.devices = value;
	}

}
