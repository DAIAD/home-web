package eu.daiad.web.model.query;

import java.util.ArrayList;

import org.joda.time.DateTimeZone;

import eu.daiad.web.model.RestResponse;

public class DataQueryResponse extends RestResponse {

	private String timezone;

	private ArrayList<GroupDataSeries> devices;

	private ArrayList<GroupDataSeries> meters;

	public DataQueryResponse() {
		this.timezone = DateTimeZone.UTC.toString();
	}

	public DataQueryResponse(String timezone) {
		this.timezone = timezone;
	}

	public DataQueryResponse(DateTimeZone timezone) {
		this.timezone = timezone.toString();
	}

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

	public String getTimezone() {
		return timezone;
	}

}
