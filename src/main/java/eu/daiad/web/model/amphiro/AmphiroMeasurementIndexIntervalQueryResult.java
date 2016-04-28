package eu.daiad.web.model.amphiro;

import java.util.ArrayList;

import eu.daiad.web.model.RestResponse;

public class AmphiroMeasurementIndexIntervalQueryResult extends RestResponse {

	private ArrayList<AmphiroDataSeries> series = null;

	public AmphiroMeasurementIndexIntervalQueryResult() {
		super();

		this.series = new ArrayList<AmphiroDataSeries>();
	}

	public AmphiroMeasurementIndexIntervalQueryResult(String code, String description) {
		super(code, description);
	}

	public void setSeries(ArrayList<AmphiroDataSeries> value) {
		this.series = value;
	}

	public ArrayList<AmphiroDataSeries> getSeries() {
		return this.series;
	}

}
