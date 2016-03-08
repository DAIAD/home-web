package eu.daiad.web.model.device;

import java.util.UUID;

import org.joda.time.DateTime;

import eu.daiad.web.model.AuthenticatedRequest;

public class NotifyConfigurationRequest extends AuthenticatedRequest {

	private DateTime updatedOn;

	private UUID version;

	private UUID deviceKey;

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

	public UUID getDeviceKey() {
		return deviceKey;
	}

	public void setDeviceKey(UUID deviceKey) {
		this.deviceKey = deviceKey;
	}

}
