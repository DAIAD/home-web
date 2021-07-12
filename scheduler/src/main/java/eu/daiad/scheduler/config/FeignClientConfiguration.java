package eu.daiad.scheduler.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(
    basePackageClasses = {
        eu.daiad.scheduler.feign.client._Marker.class,
    }
)
public class FeignClientConfiguration {

}