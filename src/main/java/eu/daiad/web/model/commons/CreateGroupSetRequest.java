package eu.daiad.web.model.commons;

import java.util.UUID;

import com.vividsolutions.jts.geom.Geometry;

public class CreateGroupSetRequest {

	private String name;

	private Geometry geometry;

	private UUID[] members;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Geometry getGeometry() {
		return geometry;
	}

	public void setGeometry(Geometry geometry) {
		this.geometry = geometry;
	}

	public UUID[] getMembers() {
		return members;
	}

	public void setMembers(UUID[] members) {
		this.members = members;
	}

}
