package eu.daiad.web.model.group;

import java.util.UUID;

import org.locationtech.jts.geom.Geometry;

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
