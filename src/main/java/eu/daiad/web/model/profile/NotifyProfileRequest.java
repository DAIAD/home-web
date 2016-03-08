package eu.daiad.web.model.profile;

import java.util.UUID;

import org.joda.time.DateTime;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import eu.daiad.web.model.AuthenticatedRequest;
import eu.daiad.web.model.EnumApplication;
import eu.daiad.web.util.ApplicationTypeDeserializer;

public class NotifyProfileRequest extends AuthenticatedRequest {

	@JsonDeserialize(using = ApplicationTypeDeserializer.class)
	private EnumApplication application;
	
	private DateTime updatedOn;

	private UUID version;
	
	public DateTime getUpdatedOn() {
		return updatedOn;
	}

	public void setUpdatedOn(DateTime updatedOn) {
		this.updatedOn = updatedOn;
	}

	public EnumApplication getApplication() {
		return application;
	}

	public void setApplication(EnumApplication application) {
		this.application = application;
	}

	public UUID getVersion() {
		return version;
	}

	public void setVersion(UUID version) {
		this.version = version;
	}

}
