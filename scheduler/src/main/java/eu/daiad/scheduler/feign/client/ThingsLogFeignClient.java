package eu.daiad.scheduler.feign.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import eu.daiad.scheduler.model.thingslog.ThingsLogLoginRequest;
import eu.daiad.scheduler.model.thingslog.ThingsLogMeasurement;

@FeignClient(
    name = "${daiad.feign.things-log-service.name}",
    url = "${daiad.feign.things-log-service.url}"
)
public interface ThingsLogFeignClient {

	@PostMapping(value = "/login")
	ResponseEntity<Void> login(@RequestBody ThingsLogLoginRequest request);

	@GetMapping(value = "/v2/device/{deviceId}/0/readings")
	ResponseEntity<List<ThingsLogMeasurement>> getDeviceValues(
		@RequestHeader("Authorization") String token,
		@PathVariable("deviceId") String deviceId,
		@RequestParam("fromDate") String fromDate,
		@RequestParam("toDate") String toDate
	);
	
}
