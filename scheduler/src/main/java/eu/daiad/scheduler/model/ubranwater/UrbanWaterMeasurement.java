package eu.daiad.scheduler.model.ubranwater;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UrbanWaterMeasurement {

	@JsonProperty("date")
	private long timestamp;
	
	@JsonProperty("dateTxt")
	private ZonedDateTime date;

	@JsonProperty("value")
	private float volume;

}
