package eu.daiad.web.model.profile;

import eu.daiad.web.model.RestResponse;

public class ProfileResponse extends RestResponse {

	private Profile profile;

	public ProfileResponse() {
	}

	public ProfileResponse(int code, String description) {
		super(code, description);
	}
	
	public Profile getProfile() {
		return profile;
	}

	public void setProfile(Profile profile) {
		this.profile = profile;
	}
	
}
