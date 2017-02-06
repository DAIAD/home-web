package eu.daiad.web.job.task;

import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.StoppableTasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.daiad.web.model.error.SchedulerErrorCode;
import eu.daiad.web.model.utility.UtilityInfo;
import eu.daiad.web.repository.application.IUtilityRepository;
import eu.daiad.web.service.etl.EnumDataSource;
import eu.daiad.web.service.etl.IDataExportService;
import eu.daiad.web.service.etl.UtilityDataExportQuery;
import eu.daiad.web.service.etl.UtilityDataExportQuery.EnumExportMode;
import eu.daiad.web.service.scheduling.Constants;

/**
 * Task for exporting smart water meter data for a utility.
 */
@Component
public class ExportMeterDataToFileTask extends BaseTask implements StoppableTasklet {

    /**
     * Repository for accessing utility data.
     */
    @Autowired
    private IUtilityRepository utilityRepository;

    /**
     * Service for exporting water meter data.
     */
    @Autowired
    IDataExportService dataExportService;


    /**
     * Resolves working directory.
     *
     * @param chunkContext current context.
     * @param parameters step parameters.
     * @return a valid directory.
     * @throws Exception if an I/O exception occurs.
     */
    private String resolveWorkingDirectory(ChunkContext chunkContext, Map<String, String> parameters) throws Exception {
        String workingDirectory = parameters.get(EnumInParameter.WORKING_DIRECTORY.getValue());

        if (StringUtils.isBlank(workingDirectory)) {
            workingDirectory = createWokringDirectory(chunkContext.getStepContext().getJobName() + "-");

            // Override input parameter
            parameters.put(EnumInParameter.WORKING_DIRECTORY.getValue(), workingDirectory);
        }

        return workingDirectory;
    }

    /**
     * Exports step parameters to job context.
     *
     * @param chunkContext current context.
     * @param parameters step parameters.
     */
    private void exportParametersToContext(ChunkContext chunkContext, Map<String, String> parameters) {
        // Export working directory
        String key = chunkContext.getStepContext().getStepName() +
                     Constants.PARAMETER_NAME_DELIMITER +
                     EnumOutParameter.UTILITY_ID.getValue();

        chunkContext.getStepContext()
                    .getStepExecution()
                    .getExecutionContext()
                    .put(key, parameters.get(EnumInParameter.UTILITY_ID.getValue()));

        // Export output filename
        key = chunkContext.getStepContext().getStepName() +
              Constants.PARAMETER_NAME_DELIMITER +
              EnumOutParameter.OUTPUT_FILENAME.getValue();

        String filename = FilenameUtils.concat(parameters.get(EnumInParameter.WORKING_DIRECTORY.getValue()),
                                               parameters.get(EnumInParameter.OUTPUT_FILENAME.getValue()));

        chunkContext.getStepContext()
                    .getStepExecution()
                    .getExecutionContext()
                    .put(key, filename);
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        try {
            // Get all step parameters
            Map<String, String> parameters = getStepParameters(chunkContext.getStepContext());

            // Create working directory
            String workingDirectory = resolveWorkingDirectory(chunkContext, parameters);

            // Get utility
            int utilityId = Integer.parseInt(parameters.get(EnumInParameter.UTILITY_ID.getValue()));
            UtilityInfo utility = utilityRepository.getUtilityById(utilityId);

            // Build and execute export query
            UtilityDataExportQuery query = new UtilityDataExportQuery(utility, workingDirectory);

            query.setWorkingDirectory(workingDirectory);
            query.setFilename(parameters.get(EnumInParameter.OUTPUT_FILENAME.getValue()));
            query.setDateFormat(parameters.get(EnumInParameter.DATE_FORMAT.getValue()));
            query.setMode(EnumExportMode.METER_TRIAL);
            query.setComporessed(false);
            query.setDescription(String.format("Meter data for all users in [%s].", utility.getName()));
            query.setSource(EnumDataSource.METER);

            dataExportService.export(query);

            // Export parameters
            exportParametersToContext(chunkContext, parameters);
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
         * Utility id for which the data is exported.
         */
        UTILITY_ID("utility.id"),
        /**
         * Date format.
         */
        DATE_FORMAT("format.date"),
        /**
         * Export file name
         */
        OUTPUT_FILENAME("output.filename"),
        /**
         * Working directory
         */
        WORKING_DIRECTORY("working.directory");


        private final String value;

        public String getValue() {
            return value;
        }

        private EnumInParameter(String value) {
            this.value = value;
        }

    }

    /**
     * Enumeration of job input parameters.
     */
    public static enum EnumOutParameter {
        /**
         * Utility id for which the data is exported.
         */
        UTILITY_ID("utility.id"),
        /**
         * Export file name
         */
        OUTPUT_FILENAME("output.filename");


        private final String value;

        public String getValue() {
            return value;
        }

        private EnumOutParameter(String value) {
            this.value = value;
        }

    }
}
