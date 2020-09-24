package eu.daiad.common.model.device;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import eu.daiad.common.model.AuthenticatedRequest;

public class DeviceRegistrationQuery extends AuthenticatedRequest {

	@JsonDeserialize(using = EnumDeviceType.Deserializer.class)
	private EnumDeviceType type;

	public DeviceRegistrationQuery() {

	}

	public DeviceRegistrationQuery(EnumDeviceType type) {
		this.type = type;
	}

	public EnumDeviceType getType() {
		if (this.type == null) {
			return EnumDeviceType.UNDEFINED;
		}
		return this.type;
	}

	public void setType(EnumDeviceType value) {
		this.type = value;
	}

}
