package eu.daiad.web.model;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(  
    use = JsonTypeInfo.Id.NAME,  
    include = JsonTypeInfo.As.EXISTING_PROPERTY,  
    property = "type")   
@JsonSubTypes({  
	@Type(value = DeviceMeasurementCollection.class, name = "AMPHIRO"),
	@Type(value = MeterMeasurementCollection.class, name = "METER")
})
public class MeasurementCollection extends AuthenticatedRequest {

	private EnumDeviceType type;
	
	private UUID userKey;

	public void setUserKey(UUID value) {
		this.userKey = value;
	}
	
	public UUID getUserKey() {
		return this.userKey;
	}
	
	public EnumDeviceType getType() {
		return this.type;
	}
	
	public void setType(EnumDeviceType value) {
		this.type = value;
	}
}
