package eu.daiad.web.model.security;

import eu.daiad.web.model.RestResponse;
import eu.daiad.web.model.Runtime;
import eu.daiad.web.model.profile.Profile;

public class AuthenticationResponse extends RestResponse {

	private Runtime runtime;

	private Profile profile;

	public AuthenticationResponse(Runtime runtime, Profile profile) {
		super();

		this.runtime = runtime;
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

	public Runtime getRuntime() {
		return runtime;
	}

}
