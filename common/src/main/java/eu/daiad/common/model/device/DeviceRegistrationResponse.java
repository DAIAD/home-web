package eu.daiad.common.model.device;

import java.util.UUID;

import eu.daiad.common.model.RestResponse;
import eu.daiad.common.model.error.ErrorCode;

public class DeviceRegistrationResponse extends RestResponse {

	private UUID deviceKey;

	public DeviceRegistrationResponse() {
		super();
	}

	public DeviceRegistrationResponse(ErrorCode code, String description) {
		super(code, description);
	}

	public UUID getDeviceKey() {
		return this.deviceKey;
	}

	public void setDeviceKey(UUID value) {
		this.deviceKey = value;
	}
}
