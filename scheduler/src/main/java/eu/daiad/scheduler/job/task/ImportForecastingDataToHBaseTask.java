package eu.daiad.scheduler.job.task;

import java.util.Map;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.StoppableTasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.daiad.common.model.error.SchedulerErrorCode;
import eu.daiad.common.model.loader.EnumUploadFileType;
import eu.daiad.common.model.utility.UtilityInfo;
import eu.daiad.common.repository.application.IUtilityRepository;
import eu.daiad.common.service.IWaterMeterDataLoaderService;

/**
 * Task for importing forecasting data to HBase.
 */
@Component
public class ImportForecastingDataToHBaseTask extends BaseTask implements StoppableTasklet {

    /**
     * Repository for accessing utility data.
     */
    @Autowired
    private IUtilityRepository utilityRepository;

    /**
     * Service for parsing files with meter or forecasting data and importing it to HBase.
     */
    @Autowired
    private IWaterMeterDataLoaderService waterMeterDataLoaderService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        try {
            // Get all step parameters
            Map<String, String> parameters = getStepParameters(chunkContext.getStepContext());

            int utilityId = Integer.parseInt(parameters.get(EnumInParameter.UTILITY_ID.getValue()));
            UtilityInfo utility = utilityRepository.getUtilityById(utilityId);

            waterMeterDataLoaderService.parse(parameters.get(EnumInParameter.INPUT_FILENAME.getValue()),
                                              utility.getTimezone(),
                                              EnumUploadFileType.METER_DATA_FORECAST,
                                              parameters.get(EnumInParameter.HDFS_PATH.getValue()));
        } catch (Throwable t) {
            throw wrapApplicationException(t, SchedulerErrorCode.SCHEDULER_JOB_STEP_FAILED)
                    .set("step", chunkContext.getStepContext().getStepName());
        }

        return RepeatStatus.FINISHED;
    }

    @Override
    public void stop() {
        // TODO: Add business logic for stopping processing
    }

    /**
     * Enumeration of job input parameters.
     */
    public static enum EnumInParameter {
        /**
         * Utility id for which the data is imported.
         */
        UTILITY_ID("utility.id"),
        /**
         * Input filename directory
         */
        INPUT_FILENAME("input.filename"),
        /**
         * HDFS path.
         */
        HDFS_PATH("fs.defaultFS"),
        /**
         * Aggregate results
         */
        EXECUTE_MR_AGGREGATE_JOB("aggregate.results");

        private final String value;

        public String getValue() {
            return value;
        }

        private EnumInParameter(String value) {
            this.value = value;
        }
    }

}
