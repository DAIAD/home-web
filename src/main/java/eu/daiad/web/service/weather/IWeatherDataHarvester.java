package eu.daiad.web.service.weather;

import java.util.Map;

/**
 * Interface for weather data harvesters.
 */
public interface IWeatherDataHarvester {

    /**
     * Initializes a weather data harvester instance.
     *  
     * @param serviceId Id of the weather service to load meta data for.
     * @param properties A map of key/value properties.
     */
    void initialize(int serviceId, Map<String, String> properties);
    
    /**
     * Returns the unique identifiers of the supported utilities
     * 
     * @return An array of unique identifiers
     */
    int[] getSupportedUtilities();
    
    /**
     * Harvests data from a remote weather service.
     * 
     * @return The harvested weather data.
     */
    WeatherHarvesterResult harvest();

}
