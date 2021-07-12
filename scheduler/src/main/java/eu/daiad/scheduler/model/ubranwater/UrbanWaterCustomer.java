package eu.daiad.scheduler.model.ubranwater;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UrbanWaterCustomer {

	@JsonProperty("customerid")
	private Integer customerId;

	private List<UrbanWaterMeter> devices;

}
