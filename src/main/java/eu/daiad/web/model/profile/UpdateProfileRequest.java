package eu.daiad.web.model.profile;

import eu.daiad.web.model.AuthenticatedRequest;

public class UpdateProfileRequest extends AuthenticatedRequest {

	private String profile;

	public String getProfile() {
		return this.profile;
	}

	public void setProfile(String value) {
		this.profile = value;
	}

}
