package eu.daiad.common.model.device;

import java.util.UUID;

import eu.daiad.common.model.AuthenticatedRequest;

public class DeviceConfigurationRequest extends AuthenticatedRequest {

	private UUID deviceKey[];

	public UUID[] getDeviceKey() {
		return deviceKey;
	}

	public void setDeviceKey(UUID[] deviceKey) {
		this.deviceKey = deviceKey;
	}

}
