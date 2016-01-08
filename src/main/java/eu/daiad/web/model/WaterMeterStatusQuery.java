package eu.daiad.web.model;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class WaterMeterStatusQuery extends AuthenticatedRequest {

	@JsonIgnore
	private UUID userKey;

	private UUID deviceKey[];

	public UUID getUserKey() {
		return userKey;
	}

	public void setUserKey(UUID userKey) {
		this.userKey = userKey;
	}

	public UUID[] getDeviceKey() {
		return deviceKey;
	}

	public void setDeviceKey(UUID deviceKey[]) {
		this.deviceKey = deviceKey;
	}

}
