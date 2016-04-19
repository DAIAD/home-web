package eu.daiad.web.model.commons;

import java.util.UUID;

import com.vividsolutions.jts.geom.Geometry;

public class Account {

	private UUID key;

	private String username;

	private String fullName;

	private Geometry location;

	public UUID getKey() {
		return key;
	}

	public void setKey(UUID key) {
		this.key = key;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public Geometry getLocation() {
		return location;
	}

	public void setLocation(Geometry location) {
		this.location = location;
	}

}
