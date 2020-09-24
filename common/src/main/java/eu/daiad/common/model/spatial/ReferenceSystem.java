package eu.daiad.common.model.spatial;

public class ReferenceSystem {

	private int srid = 4326;

	public ReferenceSystem(int srid) {
		this.srid = srid;
	}

	public int getSrid() {
		return srid;
	}

	@Override
	public String toString() {
		return "EPSG:" + srid;
	}
}
