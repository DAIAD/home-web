package eu.daiad.web.model.query;

import com.vividsolutions.jts.geom.Geometry;

public class SpatialFilter {

	private EnumSpatialFilterType type;

	private Geometry geometry;

	public EnumSpatialFilterType getType() {
		return type;
	}

	public void setType(EnumSpatialFilterType type) {
		this.type = type;
	}

	public Geometry getGeometry() {
		return geometry;
	}

	public void setGeometry(Geometry geometry) {
		this.geometry = geometry;
	}

}
