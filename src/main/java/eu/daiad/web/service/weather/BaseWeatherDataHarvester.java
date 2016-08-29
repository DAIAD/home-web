package eu.daiad.web.service.weather;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;

import eu.daiad.web.domain.application.WeatherServiceEntity;
import eu.daiad.web.domain.application.WeatherServiceUtilityEntity;

public abstract class BaseWeatherDataHarvester implements IWeatherDataHarvester {

    protected WeatherServiceEntity service;

    @Autowired
    protected IWeatherRepository weatherRepository;

    @Override
    public void initialize(int serviceId, Map<String, String> properties) {
        this.service = weatherRepository.getServiceById(serviceId);
    }

    @Override
    public int[] getSupportedUtilities() {
        List<Integer> utilities = new ArrayList<Integer>();

        for (WeatherServiceUtilityEntity utility : service.getUtilities()) {
            utilities.add(utility.getId());
        }

        return ArrayUtils.toPrimitive(utilities.toArray(new Integer[utilities.size()]));
    }

    @Override
    public abstract WeatherHarvesterResult harvest();

}
