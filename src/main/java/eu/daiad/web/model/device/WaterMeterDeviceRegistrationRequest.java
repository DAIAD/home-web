package eu.daiad.web.model.device;

import java.util.UUID;

public class WaterMeterDeviceRegistrationRequest extends DeviceRegistrationRequest {

	private UUID userKey;

	private String serial;

	public String getSerial() {
		return serial;
	}

	public void setSerial(String serial) {
		this.serial = serial;
	}

	@Override
	public EnumDeviceType getType() {
		return EnumDeviceType.METER;
	}

	public UUID getUserKey() {
		return userKey;
	}

	public void setUserKey(UUID userKey) {
		this.userKey = userKey;
	}
}
