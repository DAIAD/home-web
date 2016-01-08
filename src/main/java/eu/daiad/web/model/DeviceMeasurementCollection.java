package eu.daiad.web.model;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import eu.daiad.web.model.amphiro.AmphiroMeasurementCollection;
import eu.daiad.web.model.device.EnumDeviceType;
import eu.daiad.web.model.meter.WaterMeterMeasurementCollection;
import eu.daiad.web.util.DeviceTypeDeserializer;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes({
		@Type(value = AmphiroMeasurementCollection.class, name = "AMPHIRO"),
		@Type(value = WaterMeterMeasurementCollection.class, name = "METER") })
public class DeviceMeasurementCollection extends AuthenticatedRequest {

	@JsonDeserialize(using = DeviceTypeDeserializer.class)
	private EnumDeviceType type;

	@JsonIgnore
	private UUID userKey;

	private UUID deviceKey;

	public void setUserKey(UUID value) {
		this.userKey = value;
	}

	public UUID getUserKey() {
		return this.userKey;
	}

	public UUID getDeviceKey() {
		return deviceKey;
	}

	public void setDeviceKey(UUID deviceKey) {
		this.deviceKey = deviceKey;
	}

	public EnumDeviceType getType() {
		return this.type;
	}

	public void setType(EnumDeviceType value) {
		this.type = value;
	}

}
