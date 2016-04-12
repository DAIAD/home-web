package eu.daiad.web.model.profile;

import java.util.UUID;

import org.joda.time.DateTime;

import eu.daiad.web.model.AuthenticatedRequest;

public class NotifyProfileRequest extends AuthenticatedRequest {

	private DateTime updatedOn;

	private UUID version;

	public DateTime getUpdatedOn() {
		return updatedOn;
	}

	public void setUpdatedOn(DateTime updatedOn) {
		this.updatedOn = updatedOn;
	}

	public UUID getVersion() {
		return version;
	}

	public void setVersion(UUID version) {
		this.version = version;
	}

}
