package eu.daiad.web.model.query;

import java.util.UUID;

public class UserDataPoint {

	private UUID key;

	private String label;

	public UserDataPoint(UUID key, String label) {
		this.key = key;
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	public UUID getKey() {
		return key;
	}

}
