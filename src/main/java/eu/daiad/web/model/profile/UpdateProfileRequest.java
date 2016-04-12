package eu.daiad.web.model.profile;

import eu.daiad.web.model.AuthenticatedRequest;

public class UpdateProfileRequest extends AuthenticatedRequest {

	private String configuration;

	public String getConfiguration() {
		return configuration;
	}

	public void setConfiguration(String configuration) {
		this.configuration = configuration;
	}

}
