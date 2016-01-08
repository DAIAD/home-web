package eu.daiad.web.model;

import java.util.ArrayList;

public class WaterMeterMeasurementQueryResult extends RestResponse {

	private ArrayList<WaterMeterDataSeries> series = new ArrayList<WaterMeterDataSeries>();

	public WaterMeterMeasurementQueryResult() {
		super();
	}

	public WaterMeterMeasurementQueryResult(int code, String description) {
		super(code, description);
	}

	public ArrayList<WaterMeterDataSeries> getSeries() {
		return series;
	}
}
