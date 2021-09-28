package eu.daiad.scheduler.model.ubranwater;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UrbanWaterMeter {

	@JsonProperty("deviceid")
	private Integer deviceId;

	private String eui;
	
	private long totalVolume;

}
