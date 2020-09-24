package eu.daiad.scheduler.job.task;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.StoppableTasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.daiad.common.model.error.SchedulerErrorCode;
import eu.daiad.common.model.scheduling.Constants;
import eu.daiad.common.model.utility.UtilityInfo;
import eu.daiad.common.repository.application.IUtilityRepository;
import eu.daiad.scheduler.service.etl.EnumDataSource;
import eu.daiad.scheduler.service.etl.IDataExportService;
import eu.daiad.scheduler.service.etl.UtilityDataExportQuery;
import eu.daiad.scheduler.service.etl.UtilityDataExportQuery.EnumExportMode;

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

            query.setSerials(getFilteredMeterSerials(workingDirectory, parameters.get(EnumInParameter.METER_FILTER_FILENAME.getValue())));

            // Optionally export data only for the last year
            if (parameters.containsKey(EnumInParameter.EXPORT_YEARS.getValue())) {
                int years = Integer.parseInt(parameters.get(EnumInParameter.EXPORT_YEARS.getValue()));

                if (years > 0) {
                    DateTimeZone timezone = DateTimeZone.forID(utility.getTimezone());
                    DateTime now = new DateTime(timezone);

                    DateTime end = now.withDayOfMonth(1).minusMonths(1).dayOfMonth().withMaximumValue();
                    DateTime start = end.minusYears(years).dayOfMonth().withMaximumValue().plusDays(1);

                    // Adjust start/end dates
                    start = new DateTime(start.getYear(),
                                         start.getMonthOfYear(),
                                         start.getDayOfMonth(),
                                         0, 0, 0, timezone);

                    end = new DateTime(end.getYear(),
                                       end.getMonthOfYear(),
                                       end.getDayOfMonth(),
                                       23, 59, 59, timezone);

                    query.setStartTimestamp(start.getMillis());
                    query.setEndTimestamp(end.getMillis());
                }
            }
            if (parameters.containsKey(EnumInParameter.MAX_EXPORT_DATE_VALUE.getValue())) {
                String maxDateFormat = parameters.get(EnumInParameter.MAX_EXPORT_DATE_FORMAT.getValue());
                if (StringUtils.isBlank(maxDateFormat)) {
                    maxDateFormat = "dd/MM/yyyy";
                }
                DateTimeZone timezone = DateTimeZone.forID(utility.getTimezone());
                DateTimeFormatter parseFormatter = DateTimeFormat.forPattern(maxDateFormat).withZone(timezone);

                DateTime maxDate = parseFormatter.parseDateTime(parameters.get(EnumInParameter.MAX_EXPORT_DATE_VALUE.getValue()));
                query.setEndTimestamp(maxDate.getMillis());
            }

            dataExportService.export(query);

            // Export parameters
            exportParametersToContext(chunkContext, parameters);
        } catch (Throwable t) {
            throw wrapApplicationException(t, SchedulerErrorCode.SCHEDULER_JOB_STEP_FAILED)
                .set("step", chunkContext.getStepContext().getStepName());
        }

        return RepeatStatus.FINISHED;
    }

    private List<String> getFilteredMeterSerials(String workingDirectory, String filename) throws FileNotFoundException {
        if(StringUtils.isBlank(filename)) {
            return null;
        }
        String path = Paths.get(workingDirectory, filename).toString();

        File file = new File(path);

        if (!file.exists()) {
            return null;
        }

        List<String> serials = new ArrayList<String>();

        String line;
        try (Scanner scan = new Scanner(file)) {
            while (scan.hasNextLine()) {
                line = scan.nextLine();

                if (!StringUtils.isBlank(line)) {
                    serials.add(line.trim());
                }
            }
        }

        return serials;
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
         * A file with a single meter serial number per line. If this file
         * exists, only data for the meters in the file are exported.
         */
        METER_FILTER_FILENAME("meter.filter.filename"),
        /**
         * Export file name
         */
        OUTPUT_FILENAME("output.filename"),
        /**
         * If not set, all data is exported. Otherwise, the data for the
         * selected number of years is exported. In the latter case, the export
         * time interval ends at the last day of the previous month since the
         * execution date.
         */
        EXPORT_YEARS("export.years"),
        /**
         * Max export date format
         */
        MAX_EXPORT_DATE_FORMAT("max.date.format"),
        /**
         * Max export date formatted using the pattern {@link EnumInParameter#MAX_EXPORT_DATE_FORMAT}
         */
        MAX_EXPORT_DATE_VALUE("max.date.value"),
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
