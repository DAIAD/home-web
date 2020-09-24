package eu.daiad.common.model;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import eu.daiad.common.model.amphiro.AmphiroMeasurementCollection;
import eu.daiad.common.model.device.EnumDeviceType;
import eu.daiad.common.model.meter.WaterMeterMeasurementCollection;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes({ @Type(value = AmphiroMeasurementCollection.class, name = "AMPHIRO"),
				@Type(value = WaterMeterMeasurementCollection.class, name = "METER") })
public class DeviceMeasurementCollection extends AuthenticatedRequest {

	@JsonDeserialize(using = EnumDeviceType.Deserializer.class)
	private EnumDeviceType type;

	private UUID deviceKey;

	public UUID getDeviceKey() {
		return deviceKey;
	}

	public void setDeviceKey(UUID deviceKey) {
		this.deviceKey = deviceKey;
	}

	public EnumDeviceType getType() {
		return this.type;
	}

}
