package eu.daiad.web.model.profile;

import eu.daiad.web.model.RestResponse;
import eu.daiad.web.model.Runtime;

public class ProfileResponse extends RestResponse {

	private Runtime runtime;

	private Profile profile;

	public ProfileResponse(Runtime runtime, Profile profile) {
		this.runtime = runtime;
		this.profile = profile;
	}

	public ProfileResponse(String code, String description) {
		super(code, description);
	}

	public Profile getProfile() {
		return profile;
	}

	public Runtime getRuntime() {
		return runtime;
	}

}
