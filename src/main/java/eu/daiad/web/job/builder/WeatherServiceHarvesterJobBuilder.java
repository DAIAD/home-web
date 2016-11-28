package eu.daiad.web.job.builder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersIncrementer;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.StoppableTasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import eu.daiad.web.domain.application.WeatherServiceEntity;
import eu.daiad.web.service.weather.IWeatherDataHarvester;
import eu.daiad.web.service.weather.IWeatherRepository;
import eu.daiad.web.service.weather.WeatherHarvesterResult;
import eu.daiad.web.service.weather.WeatherUtilityHarvestedData;

@Component
public class WeatherServiceHarvesterJobBuilder implements IJobBuilder {

    private static final Log logger = LogFactory.getLog(WeatherServiceHarvesterJobBuilder.class);

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private IWeatherRepository weatherRepository;

    public WeatherServiceHarvesterJobBuilder() {

    }

    private Step transferData() {
        return stepBuilderFactory.get("transferData").tasklet(new StoppableTasklet() {
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
                try {
                    // Get all job parameters
                    Map<String, String> properties = new HashMap<String, String>();

                    for (String key : chunkContext.getStepContext().getJobParameters().keySet()) {
                        if (chunkContext.getStepContext().getJobParameters().get(key) instanceof String) {
                            properties.put(key, (String) chunkContext.getStepContext().getJobParameters().get(key));
                        }
                    }

                    // Enumerate all registered weather services and invoke all
                    // active ones
                    List<WeatherServiceEntity> services = weatherRepository.getServices();
                    for (WeatherServiceEntity service : services) {
                        if (service.isActive()) {
                            // Construct weather service from the application
                            // context and initialize using the job parameters
                            IWeatherDataHarvester weatherDataHarvester = (IWeatherDataHarvester) applicationContext
                                            .getBean(service.getBean());

                            weatherDataHarvester.initialize(service.getId(), properties);

                            // Fetch data and update database
                            WeatherHarvesterResult result = weatherDataHarvester.harvest();

                            for (WeatherUtilityHarvestedData utilityData : result.getUtilities()) {
                                weatherRepository.update(result.getServiceId(), utilityData.getUtilityId(), utilityData.getCreatedOn(), utilityData.getData());
                            }
                        }
                    }
                } catch (Exception ex) {
                    logger.fatal("Failed to weather data.", ex);

                    throw ex;
                }
                return RepeatStatus.FINISHED;
            }

            @Override
            public void stop() {

            }
        }).build();
    }

    @Override
    public Job build(String name, JobParametersIncrementer incrementer) throws Exception {
        return jobBuilderFactory.get(name).incrementer(incrementer).start(transferData()).build();
    }
}
