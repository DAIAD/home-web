package eu.daiad.scheduler.model.ubranwater;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UrbanWaterMeasurement {

	@JsonProperty("date")
	private long timestamp;

	@JsonProperty("value")
	private float volume;

}
