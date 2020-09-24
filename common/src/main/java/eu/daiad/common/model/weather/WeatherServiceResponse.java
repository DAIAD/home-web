package eu.daiad.common.model.weather;

import java.util.ArrayList;
import java.util.List;

import eu.daiad.common.model.RestResponse;

public class WeatherServiceResponse extends RestResponse {

    private List<WeatherService> services = new ArrayList<WeatherService>();

    public List<WeatherService> getServices() {
        return services;
    }

}
