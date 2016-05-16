package eu.daiad.web.model.amphiro;

import java.util.ArrayList;

import eu.daiad.web.model.RestResponse;

public class AmphiroMeasurementTimeIntervalQueryResult extends RestResponse {

	private ArrayList<AmphiroDataSeries> series = null;

	public AmphiroMeasurementTimeIntervalQueryResult() {
		super();
		
		this.series = new ArrayList<AmphiroDataSeries>();
	}

	public AmphiroMeasurementTimeIntervalQueryResult(String code, String description) {
		super(code, description);
	}

	public void setSeries(ArrayList<AmphiroDataSeries> value) {
		this.series = value;
	}

	public ArrayList<AmphiroDataSeries> getSeries() {
		return this.series;
	}

}
