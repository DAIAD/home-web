package eu.daiad.web.model.query;

import java.util.ArrayList;

import eu.daiad.web.model.RestResponse;

public class DataQueryResponse extends RestResponse {

	ArrayList<GroupDataSeries> devices;

	ArrayList<GroupDataSeries> meters;

	public ArrayList<GroupDataSeries> getDevices() {
		return devices;
	}

	public ArrayList<GroupDataSeries> getMeters() {
		return meters;
	}

	public void setDevices(ArrayList<GroupDataSeries> devices) {
		this.devices = devices;
	}

	public void setMeters(ArrayList<GroupDataSeries> meters) {
		this.meters = meters;
	}

}
