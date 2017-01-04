package eu.daiad.web.job.task;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.StoppableTasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.daiad.web.domain.application.WeatherServiceEntity;
import eu.daiad.web.service.weather.IWeatherDataHarvester;
import eu.daiad.web.service.weather.IWeatherRepository;
import eu.daiad.web.service.weather.WeatherHarvesterResult;
import eu.daiad.web.service.weather.WeatherUtilityHarvestedData;

/**
 * Task for collecting weather data for one or more registered weather services.
 */
@Component
public class WeatherDataHarvestTask extends BaseTask implements StoppableTasklet {

    /**
     * Logger instance for writing events using the configured logging API.
     */
    private static final Log logger = LogFactory.getLog(WeatherDataHarvestTask.class);

    /**
     * Repository for querying weather service meta data and storing weather data.
     */
    @Autowired
    private IWeatherRepository weatherRepository;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        try {
            // Get step parameters
            Map<String, String> parameters = getStepParameters(chunkContext.getStepContext());

            // Enumerate all registered weather services and invoke all
            // active ones
            List<WeatherServiceEntity> services = weatherRepository.getServices();

            for (WeatherServiceEntity service : services) {
                if (service.isActive()) {
                    // Construct weather service from the application
                    // context and initialize using the job parameters
                    IWeatherDataHarvester weatherDataHarvester = (IWeatherDataHarvester) applicationContext.getBean(service.getBean());

                    weatherDataHarvester.initialize(service.getId(), parameters);

                    // Fetch data and update database
                    WeatherHarvesterResult result = weatherDataHarvester.harvest();

                    for (WeatherUtilityHarvestedData utilityData : result.getUtilities()) {
                        weatherRepository.update(result.getServiceId(),
                                                 utilityData.getUtilityId(),
                                                 utilityData.getCreatedOn(),
                                                 utilityData.getData());
                    }
                }
            }
        } catch (Exception ex) {
            logger.fatal("Failed to load weather data.", ex);

            throw ex;
        }
        return RepeatStatus.FINISHED;
    }

    @Override
    public void stop() {
        // TODO: Add business logic for stopping processing
    }

}
