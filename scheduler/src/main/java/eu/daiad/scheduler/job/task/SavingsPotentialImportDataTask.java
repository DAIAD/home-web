package eu.daiad.scheduler.job.task;

import java.util.Map;
import java.util.UUID;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.StoppableTasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.daiad.common.domain.application.SavingsPotentialScenarioEntity;
import eu.daiad.common.model.error.SchedulerErrorCode;
import eu.daiad.common.model.utility.UtilityInfo;
import eu.daiad.common.repository.application.ISavingsPotentialRepository;
import eu.daiad.common.repository.application.IUtilityRepository;
import eu.daiad.scheduler.service.savings.ISavingsPotentialDataLoaderService;

/**
 * Task for importing forecasting data to HBase.
 */
@Component
public class SavingsPotentialImportDataTask extends BaseTask implements StoppableTasklet {

    /**
     * Repository for accessing utility data.
     */
    @Autowired
    private IUtilityRepository utilityRepository;

    /**
     * Repository for updating savings potential scenario data.
     */
    @Autowired
    private ISavingsPotentialRepository savingsPotentialRepository;

    /**
     * Service for parsing files with meter or forecasting data and importing it to HBase.
     */
    @Autowired
    private ISavingsPotentialDataLoaderService savingsPotentialDataLoaderService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        try {
            // Get all step parameters
            Map<String, String> parameters = getStepParameters(chunkContext.getStepContext());

            long jobId = chunkContext.getStepContext().getStepExecution().getJobExecution().getJobId();

            int utilityId = Integer.parseInt(parameters.get(EnumInParameter.UTILITY_ID.getValue()));
            UtilityInfo utility = utilityRepository.getUtilityById(utilityId);

            switch (EnumExecutionMode.fromString(parameters.get(EnumInParameter.EXECUTION_MODE.getValue()))) {
                case SAVINGS:
                    UUID scenarioKey = UUID.fromString(parameters.get(EnumInParameter.SCENARIO_KEY.getValue()));
                    SavingsPotentialScenarioEntity scenario = savingsPotentialRepository.getScenarioByKey(scenarioKey);

                    savingsPotentialDataLoaderService.parseSavingsPotential(
                        scenario.getId(),
                        jobId,
                        parameters.get(EnumInParameter.INPUT_FILENAME_SAVINGS.getValue()),
                        parameters.get(EnumInParameter.INPUT_FILENAME_WATER_IQ.getValue()),
                        utility.getTimezone(),
                        parameters.get(EnumInParameter.HDFS_PATH.getValue()));
                    break;
                case WATER_IQ:
                    savingsPotentialDataLoaderService.parseWaterIq(
                        utility.getId(),
                        jobId,
                        parameters.get(EnumInParameter.INPUT_FILENAME_SAVINGS.getValue()),
                        parameters.get(EnumInParameter.INPUT_FILENAME_WATER_IQ.getValue()),
                        utility.getTimezone(),
                        parameters.get(EnumInParameter.HDFS_PATH.getValue()));
                    break;
                default:
                    // Do nothing
            }
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
         * When in SAVINGS mode, this parameters stores the corresponding
         * savings potential scenario key.
         */
        SCENARIO_KEY("scenario.key"),
        /**
         * Input filename directory
         */
        INPUT_FILENAME_SAVINGS("input.filename.savings"),
        /**
         * Input filename directory
         */
        INPUT_FILENAME_WATER_IQ("input.filename.water-iq"),
        /**
         * HDFS path.
         */
        HDFS_PATH("fs.defaultFS"),
        /**
         * Execution mode: Can be SAVINGS or WATER_IQ.
         */
        EXECUTION_MODE("execution.mode"),
        /**
         * Compute consumption clusters if value is equal to {@code true}.
         */
        COMPUTE_CONSUMPTION_CLUSTERS("compute.consumption.clusters");

        private final String value;

        public String getValue() {
            return value;
        }

        private EnumInParameter(String value) {
            this.value = value;
        }
    }

    public enum EnumExecutionMode {
        UNDEFINED(""), SAVINGS("SAVINGS"), WATER_IQ("WATER_IQ");

        private final String value;

        private EnumExecutionMode(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static EnumExecutionMode fromString(String value) {
            for (EnumExecutionMode item : EnumExecutionMode.values()) {
                if (item.getValue().equalsIgnoreCase(value)) {
                    return item;
                }
            }
            return EnumExecutionMode.UNDEFINED;
        }

    }

}
