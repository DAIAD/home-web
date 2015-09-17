package eu.daiad.web.model;

import java.util.ArrayList;

public class SmartMeterDataSeriesCollectionResult extends RestResponse {

	private ArrayList<WaterMeterDataSeries> series = new ArrayList<WaterMeterDataSeries>();

	public SmartMeterDataSeriesCollectionResult() {
		super();
	}

	public SmartMeterDataSeriesCollectionResult(int code, String description) {
		super(code, description);
	}

	public ArrayList<WaterMeterDataSeries> getSeries() {
		return series;
	}
}
