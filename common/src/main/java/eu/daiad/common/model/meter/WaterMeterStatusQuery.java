package eu.daiad.common.model.meter;

import java.util.UUID;

import eu.daiad.common.model.AuthenticatedRequest;

public class WaterMeterStatusQuery extends AuthenticatedRequest {

	private UUID userKey;

	private UUID deviceKey[];

	public UUID[] getDeviceKey() {
		return deviceKey;
	}

	public void setDeviceKey(UUID deviceKey[]) {
		this.deviceKey = deviceKey;
	}

	public UUID getUserKey() {
		return userKey;
	}

	public void setUserKey(UUID userKey) {
		this.userKey = userKey;
	}

}
