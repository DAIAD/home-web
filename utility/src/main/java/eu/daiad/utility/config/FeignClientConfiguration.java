package eu.daiad.utility.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(
    basePackageClasses = {
        eu.daiad.utility.feign.client._Marker.class,
    }
)
public class FeignClientConfiguration {

}