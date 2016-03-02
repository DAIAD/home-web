package eu.daiad.web.model.profile;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import eu.daiad.web.model.AuthenticatedRequest;
import eu.daiad.web.model.EnumApplication;
import eu.daiad.web.util.ApplicationTypeDeserializer;

public class UpdateProfileRequest extends AuthenticatedRequest {

	@JsonDeserialize(using = ApplicationTypeDeserializer.class)
	private EnumApplication application;

	private String configuration;

	public EnumApplication getApplication() {
		return application;
	}

	public void setApplication(EnumApplication application) {
		this.application = application;
	}

	public String getConfiguration() {
		return configuration;
	}

	public void setConfiguration(String configuration) {
		this.configuration = configuration;
	}

}
