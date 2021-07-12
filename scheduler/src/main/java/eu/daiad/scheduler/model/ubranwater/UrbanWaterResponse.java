package eu.daiad.scheduler.model.ubranwater;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UrbanWaterResponse<T> {

	private int status;
	
	@JsonProperty("statusmessage")
	private String statusMessage;
	
	private String message;
	
	private boolean error;
	
	private T data;
	
}
