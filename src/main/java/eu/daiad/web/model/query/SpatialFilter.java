package eu.daiad.web.model.query;

import com.vividsolutions.jts.geom.Geometry;

public class SpatialFilter {

	private EnumSpatialFilterType type;

	private Geometry geometry;

	private Double distance;

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

	public Double getDistance() {
		if (this.type == EnumSpatialFilterType.DISTANCE) {
			return null;
		}
		return distance;
	}

	public void setDistance(Double distance) {
		this.distance = distance;
	}

	@Override
	public String toString() {
		return "SpatialFilter [type=" + type + ", geometry=" + geometry + ", distance=" + distance + "]";
	}

}
