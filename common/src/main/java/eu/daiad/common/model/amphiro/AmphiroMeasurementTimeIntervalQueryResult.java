package eu.daiad.common.model.amphiro;

import java.util.ArrayList;

import eu.daiad.common.model.RestResponse;
import eu.daiad.common.model.error.ErrorCode;

public class AmphiroMeasurementTimeIntervalQueryResult extends RestResponse {

	private ArrayList<AmphiroDataSeries> series = null;

	public AmphiroMeasurementTimeIntervalQueryResult() {
		super();
		
		this.series = new ArrayList<AmphiroDataSeries>();
	}

	public AmphiroMeasurementTimeIntervalQueryResult(ErrorCode code, String description) {
		super(code, description);
	}

	public void setSeries(ArrayList<AmphiroDataSeries> value) {
		this.series = value;
	}

	public ArrayList<AmphiroDataSeries> getSeries() {
		return this.series;
	}

}
