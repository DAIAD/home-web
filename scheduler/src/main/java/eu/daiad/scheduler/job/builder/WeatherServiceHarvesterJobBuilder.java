package eu.daiad.scheduler.job.builder;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersIncrementer;
import org.springframework.batch.core.Step;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.daiad.scheduler.job.task.WeatherDataHarvestTask;

/**
 * Builds a job for collecting weather data from one or more weather services.
 */
@Component
public class WeatherServiceHarvesterJobBuilder extends BaseJobBuilder implements IJobBuilder {

    /**
     * Weather data collection step name.
     */
    private static final String STEP_FETCH_DATA = "fetch-data";

    /**
     * Task for collecting weather data from registered weather services.
     */
    @Autowired
    private WeatherDataHarvestTask weatherDataHarvestTask;

    /**
     * Build weather data collection step
     *
     * @return the configured step.
     */
    private Step fetchData() {
        return stepBuilderFactory.get(STEP_FETCH_DATA)
                                 .tasklet(weatherDataHarvestTask)
                                 .build();
    }

    @Override
    public Job build(String name, JobParametersIncrementer incrementer) throws Exception {
        return jobBuilderFactory.get(name)
                                .incrementer(incrementer)
                                .start(fetchData()).build();
    }

}
