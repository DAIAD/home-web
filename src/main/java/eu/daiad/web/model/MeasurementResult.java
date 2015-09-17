package eu.daiad.web.model;

import java.util.ArrayList;

public class MeasurementResult extends RestResponse {

	private ArrayList<DataSeries> series = null;

	public MeasurementResult() {
		super();
		
		this.series = new ArrayList<DataSeries>();
	}

	public MeasurementResult(int code, String description) {
		super(code, description);
	}

	public void setSeries(ArrayList<DataSeries> value) {
		this.series = value;
	}

	public ArrayList<DataSeries> getSeries() {
		return this.series;
	}

}
