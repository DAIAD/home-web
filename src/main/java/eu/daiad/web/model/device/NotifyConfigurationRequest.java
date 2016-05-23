package eu.daiad.web.model.device;

import java.util.UUID;

import eu.daiad.web.model.AuthenticatedRequest;

public class NotifyConfigurationRequest extends AuthenticatedRequest {

	private long updatedOn;

	private UUID version;

	private UUID deviceKey;

	public long getUpdatedOn() {
		return updatedOn;
	}

	public void setUpdatedOn(long updatedOn) {
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
