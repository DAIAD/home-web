package eu.daiad.web.model.commons;

import com.vividsolutions.jts.geom.Geometry;

public class QueryConsumerRequest {

	private String name;

	private int size;

	private Geometry geometry;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public Geometry getGeometry() {
		return geometry;
	}

	public void setGeometry(Geometry geometry) {
		this.geometry = geometry;
	}

}
