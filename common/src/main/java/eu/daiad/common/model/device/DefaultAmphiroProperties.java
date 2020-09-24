package eu.daiad.common.model.device;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "daiad.amphiro")
public class DefaultAmphiroProperties {

    private Map<String, String> properties = new HashMap<String, String>();

    public Map<String, String> getProperties() {
        return this.properties;
    }
}
