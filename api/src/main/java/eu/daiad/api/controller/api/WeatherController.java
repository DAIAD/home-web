package eu.daiad.api.controller.api;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import eu.daiad.api.controller.BaseRestController;
import eu.daiad.common.domain.application.UtilityEntity;
import eu.daiad.common.domain.application.WeatherServiceEntity;
import eu.daiad.common.domain.application.WeatherServiceUtilityEntity;
import eu.daiad.common.model.RestResponse;
import eu.daiad.common.model.error.SharedErrorCode;
import eu.daiad.common.model.error.WeatherErrorCode;
import eu.daiad.common.model.security.AuthenticatedUser;
import eu.daiad.common.model.security.Credentials;
import eu.daiad.common.model.utility.UtilityInfo;
import eu.daiad.common.model.weather.DailyWeatherData;
import eu.daiad.common.model.weather.HourlyWeatherData;
import eu.daiad.common.model.weather.WeatherQueryResponse;
import eu.daiad.common.model.weather.WeatherService;
import eu.daiad.common.model.weather.WeatherServiceResponse;
import eu.daiad.common.repository.application.IWeatherRepository;

/**
 * Provides actions for querying weather service and data.
 */
@RestController
public class WeatherController extends BaseRestController {

    /**
     * Logger instance for writing events using the configured logging API.
     */
    private static final Log logger = LogFactory.getLog(WeatherController.class);

    /**
     * Repository for accessing weather data.
     */
    @Autowired
    private IWeatherRepository weatherRepository;

    /**
     * Returns the registered weather services.
     *
     * @param credentials the user credentials.
     * @return the registered weather services.
     */
    @PostMapping(value = "/api/v1/weather/service")
    public RestResponse getWeatherService(@RequestBody Credentials credentials) {
        try {
            AuthenticatedUser user = authenticate(credentials);

            WeatherServiceResponse weatherServiceResponse = new WeatherServiceResponse();

            List<WeatherServiceEntity> services = weatherRepository.getServices();

            for (WeatherServiceEntity serviceEntity : services) {
                WeatherService service = new WeatherService();

                service.setId(serviceEntity.getId());
                service.setName(serviceEntity.getName());

                for (WeatherServiceUtilityEntity utilityEntity : serviceEntity.getUtilities()) {
                    if (user.canAccessUtility(utilityEntity.getUtility().getId())) {
                        service.getUtilities().add(new UtilityInfo(utilityEntity.getUtility()));
                    }
                }

                weatherServiceResponse.getServices().add(service);
            }

            return weatherServiceResponse;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            return new RestResponse(getError(ex));
        }
    }

    /**
     * Queries weather data from a specific weather service for the given
     * utility and time interval.
     *
     * @param credentials the user credentials.
     * @param service the weather service id or name.
     * @param utility the utility id or name.
     * @param interval the type of time interval e.g. {@code day} or {@code hour}.
     * @param from the time interval start date formatted as {@code yyyyMMdd}.
     * @param to the time interval end date formatted as {@code yyyyMMdd}.
     * @return the weather data.
     */
    @PostMapping(value = "/api/v1/weather/{service}/{utility}/{interval}/{from}/{to}")
    public RestResponse getWeatherData(
		@RequestBody Credentials credentials,
		@PathVariable String service,
		@PathVariable String utility,
		@PathVariable String interval,
		@PathVariable String from,
		@PathVariable String to
	) {
        try {
            AuthenticatedUser user = authenticate(credentials);

            WeatherQueryResponse weatherQueryResponse = new WeatherQueryResponse();

            // Check if service exists
            WeatherServiceEntity weatherServiceEntity;

            if (StringUtils.isNumeric(service)) {
                weatherServiceEntity = weatherRepository.getServiceById(Integer.parseInt(service));
            } else {
                weatherServiceEntity = weatherRepository.getServiceByName(service);
            }
            if (weatherServiceEntity == null) {
                weatherQueryResponse.add(this.getError(WeatherErrorCode.SERVICE_NOT_FOUND));

                return weatherQueryResponse.toRestResponse();
            }

            // Check if utility is supported by the service
            UtilityEntity utilityEntity = null;

            if (!StringUtils.isBlank(utility)) {
                Integer utilityId = null;

                if (StringUtils.isNumeric(utility)) {
                    utilityId = Integer.parseInt(utility);
                }

                for (WeatherServiceUtilityEntity weatherServiceUtilityEntity : weatherServiceEntity.getUtilities()) {
                    if (utilityId != null) {
                        if (weatherServiceUtilityEntity.getUtility().getId() == utilityId) {
                            utilityEntity = weatherServiceUtilityEntity.getUtility();
                            break;
                        }
                    } else {
                        if (weatherServiceUtilityEntity.getUtility().getName().toLowerCase().equals(utility.toLowerCase())) {
                            utilityEntity = weatherServiceUtilityEntity.getUtility();
                            break;
                        }
                    }
                }
            }

            if (utilityEntity == null) {
                weatherQueryResponse.add(this.getError(WeatherErrorCode.UTILITY_NOT_FOUND));

                return weatherQueryResponse.toRestResponse();
            }

            if(!user.canAccessUtility(utilityEntity.getId())) {
                weatherQueryResponse.add(this.getError(SharedErrorCode.AUTHORIZATION));

                return weatherQueryResponse.toRestResponse();
            }

            // Parse dates and get weather data
            DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyyMMdd").withZone(DateTimeZone.UTC);

            List<DailyWeatherData> days = null;
            List<HourlyWeatherData> hours = null;

            switch (interval.toLowerCase()) {
                case "day":
                    days = weatherRepository.getDailyData(weatherServiceEntity.getId(),
                                                          utilityEntity.getId(),
                                                          formatter.parseDateTime(from),
                                                          formatter.parseDateTime(to));
                    break;
                case "hour":
                    hours = weatherRepository.getHourlyData(weatherServiceEntity.getId(),
                                                            utilityEntity.getId(),
                                                            formatter.parseDateTime(from),
                                                            formatter.parseDateTime(to));
                    break;
            }

            weatherQueryResponse.setDays(days);
            weatherQueryResponse.setHours(hours);

            return weatherQueryResponse;
        } catch (IllegalArgumentException ex) {
            logger.error(ex.getMessage(), ex);

            return new RestResponse(getError(WeatherErrorCode.INVALID_DATETIME));
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            return new RestResponse(getError(ex));
        }
    }

}
