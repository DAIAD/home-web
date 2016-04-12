package eu.daiad.web.model.profile;

import java.util.UUID;

public class ProfileDeactivateRequest {
	
	private UUID userDeactId;

	public UUID getUserDeactId() {
		return userDeactId;
	}

	public void setUserDeactId(UUID userDeactId) {
		this.userDeactId = userDeactId;
	}

}