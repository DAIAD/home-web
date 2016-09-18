package eu.daiad.web.model.device;

import java.util.UUID;

import eu.daiad.web.model.RestResponse;

public class DeviceRegistrationResponse extends RestResponse {

	private UUID deviceKey;

	public DeviceRegistrationResponse() {
		super();
	}

	public DeviceRegistrationResponse(String code, String description) {
		super(code, description);
	}

	public UUID getDeviceKey() {
		return this.deviceKey;
	}

	public void setDeviceKey(UUID value) {
		this.deviceKey = value;
	}
}
