package eu.daiad.web.model.device;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import eu.daiad.web.model.AuthenticatedRequest;
import eu.daiad.web.util.DeviceTypeDeserializer;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes({
		@Type(value = AmphiroDeviceRegistrationRequest.class, name = "AMPHIRO"),
		@Type(value = WaterMeterDeviceRegistrationRequest.class, name = "METER") })
public class DeviceRegistrationRequest extends AuthenticatedRequest {

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
		return this.type;
	}

	public void setType(EnumDeviceType value) {
		this.type = value;
	}
}
