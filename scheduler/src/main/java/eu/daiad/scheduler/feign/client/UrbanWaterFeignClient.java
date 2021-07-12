package eu.daiad.scheduler.feign.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;

@FeignClient(
    name = "${daiad.feign.urban-water-service.name}",
    url = "${daiad.feign.urban-water-service.url}"
)
public interface UrbanWaterFeignClient {

	@PostMapping(
        value   = "/api/1.0/account/login",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
        produces = "application/json"
    )
	String login(
		@RequestHeader("apiKey") String apiKey,
		@RequestPart(name = "username", required = true) String userName,
		@RequestPart(name = "password", required = true) String password
	);

	@GetMapping(value = "/api/1.0/customer")
   	String getCustomers(
		@RequestHeader("apiKey") String apiKey,
		@RequestHeader("accessToken") String accessToken,
		@RequestParam("devices") boolean devices
	);
	
	@GetMapping(value = "/api/1.0/device/{deviceId}/values")
   	String getDeviceValues(
		@RequestHeader("apiKey") String apiKey,
		@RequestHeader("accessToken") String accessToken,
		@PathVariable("deviceId") Integer deviceId,
		@RequestParam("from") long from
	);
	
}
