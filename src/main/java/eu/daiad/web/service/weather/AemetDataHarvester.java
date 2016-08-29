package eu.daiad.web.service.weather;

import java.util.List;

import javax.xml.transform.Source;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.xml.xpath.Jaxp13XPathTemplate;
import org.springframework.xml.xpath.XPathOperations;

import eu.daiad.web.domain.application.WeatherServiceUtilityEntity;
import eu.daiad.web.domain.application.WeatherServiceUtilityParameterEntity;

@Service
public class AemetDataHarvester extends BaseWeatherDataHarvester {

    private static final Log logger = LogFactory.getLog(AemetDataHarvester.class);

    private static final String PARAMETER_URL_KEY = "url";

    private static final String XPATH_EXPRESSION_CREATION = "//elaborado";

    private static final String XPATH_EXPRESSION_DAY = "//dia";

    @Override
    public WeatherHarvesterResult harvest() {
        RestTemplate restTemplate;
        XPathOperations xpathTemplate;

        WeatherHarvesterResult result = new WeatherHarvesterResult(service.getId());

        if (service == null) {
            logger.warn(String.format("Service of type [%s] was not found.", AemetDataHarvester.class.toString()));

            return result;
        }

        for (WeatherServiceUtilityEntity weatherServiceUtilityEntity : service.getUtilities()) {
            String resourceUrl = getResourceUrl(weatherServiceUtilityEntity);

            if (StringUtils.isBlank(resourceUrl)) {
                logger.warn(String.format("Parameter [%s] is not set for utility with id [%d].", PARAMETER_URL_KEY,
                                weatherServiceUtilityEntity.getUtility().getId()));

                // Skip this utility
                continue;
            }

            // Get data in XML format
            restTemplate = new RestTemplate();
            xpathTemplate = new Jaxp13XPathTemplate();
            

            // Get date and time at which the weather data has been generated
            Source source = restTemplate.getForObject(resourceUrl, Source.class);
                       

            DateTime createdOn = new DateTime(xpathTemplate.evaluateAsString(XPATH_EXPRESSION_CREATION, source),
                            DateTimeZone.forID("Europe/Madrid"));

            // TODO : Find a better way for reseting stream instead of
            // downloading the document again

            // Get actual weather data
            source = restTemplate.getForObject(resourceUrl, Source.class);

            List<DailyWeatherData> data = xpathTemplate.evaluate(XPATH_EXPRESSION_DAY, source, new AemetNodeMapper());

            // Update response with specific utility weather data 
            WeatherUtilityHarvestedData utilityData = new WeatherUtilityHarvestedData(weatherServiceUtilityEntity.getUtility().getId());
            
            utilityData.setCreatedOn(createdOn);
            utilityData.setData(data);
            

            result.getUtilities().add(utilityData);
        }

        return result;
    }

    private String getResourceUrl(WeatherServiceUtilityEntity utility) {
        for (WeatherServiceUtilityParameterEntity parameter : utility.getParameters()) {
            if (parameter.getKey().equals(PARAMETER_URL_KEY)) {
                return parameter.getValue();
            }
        }

        return null;
    }

}
