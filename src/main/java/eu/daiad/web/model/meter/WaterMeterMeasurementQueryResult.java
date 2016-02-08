package eu.daiad.web.model.meter;

import java.util.ArrayList;

import eu.daiad.web.model.RestResponse;

public class WaterMeterMeasurementQueryResult extends RestResponse {

	private ArrayList<WaterMeterDataSeries> series = new ArrayList<WaterMeterDataSeries>();

	public WaterMeterMeasurementQueryResult() {
		super();
	}

	public WaterMeterMeasurementQueryResult(String code, String description) {
		super(code, description);
	}

	public ArrayList<WaterMeterDataSeries> getSeries() {
		return series;
	}
}
