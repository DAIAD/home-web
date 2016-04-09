package eu.daiad.web.model.query;

import java.util.ArrayList;

import eu.daiad.web.model.RestResponse;

public class DataQueryResponse extends RestResponse {

	ArrayList<DataPoint> points = new ArrayList<DataPoint>();

	public ArrayList<DataPoint> getPoints() {
		return points;
	}

}
