package eu.daiad.web.model.device;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import eu.daiad.web.model.AuthenticatedRequest;
import eu.daiad.web.util.DeviceTypeDeserializer;

public class DeviceRegistrationQuery extends AuthenticatedRequest {

	@JsonDeserialize(using = DeviceTypeDeserializer.class)
	private EnumDeviceType type;

	private String deviceId;

	public String getDeviceId() {
		return this.deviceId;
	}

	public void setDeviceId(String value) {
		this.deviceId = value;
	}

	public EnumDeviceType getType() {
		if(this.type == null) {
			return EnumDeviceType.UNDEFINED;
		}
		return this.type;
	}

	public void setType(EnumDeviceType value) {
		this.type = value;
	}

}
