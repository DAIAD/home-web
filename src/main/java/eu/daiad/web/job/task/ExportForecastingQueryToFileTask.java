package eu.daiad.web.job.task;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
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

import eu.daiad.web.model.device.Device;
import eu.daiad.web.model.device.DeviceRegistrationQuery;
import eu.daiad.web.model.device.EnumDeviceType;
import eu.daiad.web.model.device.WaterMeterDevice;
import eu.daiad.web.model.error.SchedulerErrorCode;
import eu.daiad.web.model.utility.UtilityInfo;
import eu.daiad.web.repository.application.IDeviceRepository;
import eu.daiad.web.repository.application.IUserRepository;
import eu.daiad.web.repository.application.IUtilityRepository;

/**
 * Task for creating a forecasting query for the users of a utility.
 */
@Component
public class ExportForecastingQueryToFileTask extends BaseTask implements StoppableTasklet {

    /**
     * Delimiter character used for separating values in output file.
     */
    protected static final char DELIMITER = ';';

    /**
     * Repository for accessing utility data.
     */
    @Autowired
    private IUtilityRepository utilityRepository;

    /**
     * Repository for accessing user data.
     */
    @Autowired
    protected IUserRepository userRepository;

    /**
     * Repository for accessing device (smart water meter or amphiro b1) data.
     */
    @Autowired
    protected IDeviceRepository deviceRepository;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        CSVPrinter queryPrinter = null;

        try {
            // Get all step parameters
            Map<String, String> parameters = getStepParameters(chunkContext.getStepContext());

            // Get utility
            int utilityId = Integer.parseInt(parameters.get(EnumInParameter.UTILITY_ID.getValue()));
            UtilityInfo utility = utilityRepository.getUtilityById(utilityId);

            // Configure date/time formatters
            DateTimeZone timezone = DateTimeZone.forID(utility.getTimezone());

            DateTimeFormatter parseFormatter = DateTimeFormat.forPattern(parameters.get(EnumInParameter.DATE_FORMAT_INPUT.getValue()))
                                                             .withZone(timezone);
            DateTimeFormatter outputFormatter = DateTimeFormat.forPattern(parameters.get(EnumInParameter.DATE_FORMAT_OUPUT.getValue()))
                                                              .withZone(timezone);


            // Ensure working directory and create file
            String workingDirectory = parameters.get(EnumInParameter.WORKING_DIRECTORY.getValue());
            ensureDirectory(new File(workingDirectory));

            String outputFilename = FilenameUtils.concat(workingDirectory, parameters.get(EnumInParameter.OUTPUT_FILENAME.getValue()));

            CSVFormat format = CSVFormat.RFC4180.withDelimiter(DELIMITER);

            queryPrinter = new CSVPrinter(
                             new BufferedWriter(
                               new OutputStreamWriter(
                                 new FileOutputStream(outputFilename, true),
                                 Charset.forName("UTF-8").newEncoder())), format);

            // Parse interval
            DateTime minDate, maxDate;

            if((StringUtils.isBlank(parameters.get(EnumInParameter.INTERVAL_FROM.getValue()))) ||
               (StringUtils.isBlank(parameters.get(EnumInParameter.INTERVAL_TO.getValue())))) {
                DateTime now = new DateTime(timezone).minusDays(15);
                minDate = new DateTime(now.getYear(), now.getMonthOfYear(), now.getDayOfMonth(), 0, 0, 0, timezone);

                DateTime nextMonth = now.plusDays(30);
                maxDate = new DateTime(nextMonth.getYear(), nextMonth.getMonthOfYear(), nextMonth.getDayOfMonth(), 23, 0, 0, timezone);
            } else {
                minDate = parseFormatter.parseDateTime(parameters.get(EnumInParameter.INTERVAL_FROM.getValue()));
                minDate = new DateTime(minDate.getYear(), minDate.getMonthOfYear(), minDate.getDayOfMonth(), 0, 0, 0, timezone);

                maxDate = parseFormatter.parseDateTime(parameters.get(EnumInParameter.INTERVAL_TO.getValue()));
                maxDate = new DateTime(maxDate.getYear(), maxDate.getMonthOfYear(), maxDate.getDayOfMonth(), 23, 0, 0, timezone);
            }

            // Create query for every user.
            for (UUID userKey : userRepository.getUserKeysForUtility(utilityId)) {
                DeviceRegistrationQuery deviceQuery = new DeviceRegistrationQuery(EnumDeviceType.METER);

                for (Device meter : deviceRepository.getUserDevices(userKey, deviceQuery)) {
                    DateTime current = maxDate;

                    while(current.isAfter(minDate)) {
                        ArrayList<String> row = new ArrayList<String>();

                        row.add(((WaterMeterDevice) meter).getSerial());
                        row.add(current.toString(outputFormatter));

                        queryPrinter.printRecord(row);

                        current = current.minusHours(1);
                    }
                }
            }

            queryPrinter.flush();
        } catch (Throwable t) {
            throw wrapApplicationException(t, SchedulerErrorCode.SCHEDULER_JOB_STEP_FAILED)
                .set("step", chunkContext.getStepContext().getStepName());
        } finally {
            if (queryPrinter != null) {
                IOUtils.closeQuietly(queryPrinter);
            }
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
         * Working directory
         */
        WORKING_DIRECTORY("working.directory"),
        /**
         * Date format for input parameters
         */
        DATE_FORMAT_INPUT("format.date.input"),
        /**
         * Date format for output
         */
        DATE_FORMAT_OUPUT("format.date.output"),
        /**
         * Export file name
         */
        OUTPUT_FILENAME("output.filename"),
        /**
         * Interval start date formatted using the pattern {@link EnumParameter#DATE_FORMAT_INPUT}
         */
        INTERVAL_FROM("interval.from"),
        /**
         * Interval end date formatted using the pattern {@link EnumParameter#DATE_FORMAT_INPUT}
         */
        INTERVAL_TO("interval.to");


        private final String value;

        public String getValue() {
            return value;
        }

        private EnumInParameter(String value) {
            this.value = value;
        }
    }

}
