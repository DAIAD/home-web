package eu.daiad.scheduler.service.weather;

import java.util.ArrayList;
import java.util.List;

public class WeatherHarvesterResult {

    private int serviceId;

    private List<WeatherUtilityHarvestedData> utilities = new ArrayList<WeatherUtilityHarvestedData>();

    public WeatherHarvesterResult(int serviceId) {
        this.serviceId = serviceId;
    }

    public int getServiceId() {
        return serviceId;
    }

    public List<WeatherUtilityHarvestedData> getUtilities() {
        return utilities;
    }

}
