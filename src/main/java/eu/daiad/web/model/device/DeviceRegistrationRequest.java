package eu.daiad.web.model.device;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import eu.daiad.web.model.AuthenticatedRequest;
import eu.daiad.web.model.KeyValuePair;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes({ @Type(value = AmphiroDeviceRegistrationRequest.class, name = "AMPHIRO"),
				@Type(value = WaterMeterDeviceRegistrationRequest.class, name = "METER") })
public class DeviceRegistrationRequest extends AuthenticatedRequest {

	@JsonDeserialize(using = EnumDeviceType.Deserializer.class)
	private EnumDeviceType type;

	private ArrayList<KeyValuePair> properties;

	public ArrayList<KeyValuePair> getProperties() {
		return this.properties;
	}

	public void setProperties(ArrayList<KeyValuePair> value) {
		this.properties = value;
	}

	public EnumDeviceType getType() {
		return this.type;
	}

	public void setType(EnumDeviceType value) {
		this.type = value;
	}
}
