package eu.daiad.web.model.query;

import com.vividsolutions.jts.geom.Geometry;

public class SpatialDataPoint extends DataPoint {

	private Geometry geometry;

	public Geometry getGeometry() {
		return geometry;
	}

	public void setGeometry(Geometry geometry) {
		this.geometry = geometry;
	}
}
