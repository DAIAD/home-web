package eu.daiad.web.model;

import eu.daiad.web.model.profile.Profile;

public class AuthenticationResponse extends RestResponse {

	private Profile profile;

	public AuthenticationResponse(Profile profile) {
		super();

		this.profile = profile;
	}

	public AuthenticationResponse(String code, String description) {
		super(code, description);
	}

	public Profile getProfile() {
		return profile;
	}

	public void setProfile(Profile profile) {
		this.profile = profile;
	}

}
