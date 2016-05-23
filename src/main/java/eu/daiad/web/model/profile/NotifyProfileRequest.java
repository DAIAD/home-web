package eu.daiad.web.model.profile;

import java.util.UUID;

import eu.daiad.web.model.AuthenticatedRequest;

public class NotifyProfileRequest extends AuthenticatedRequest {

	private Long updatedOn;

	private UUID version;

	public Long getUpdatedOn() {
		return updatedOn;
	}

	public void setUpdatedOn(Long updatedOn) {
		this.updatedOn = updatedOn;
	}

	public UUID getVersion() {
		return version;
	}

	public void setVersion(UUID version) {
		this.version = version;
	}

}
