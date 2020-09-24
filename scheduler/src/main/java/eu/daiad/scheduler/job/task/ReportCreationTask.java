package eu.daiad.scheduler.job.task;

import java.io.File;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.logging.log4j.core.util.FileUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.StoppableTasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.daiad.scheduler.job.builder.CommandExecutor;
import eu.daiad.common.model.security.AuthenticatedUser;
import eu.daiad.common.model.utility.UtilityInfo;
import eu.daiad.common.repository.application.IUserRepository;
import eu.daiad.common.repository.application.IUtilityRepository;

/**
 * Task for generating reports
 */
@Component
public class ReportCreationTask extends BaseTask implements StoppableTasklet {

    /**
     * Logger instance for writing events using the configured logging API.
     */
    private static final Log logger = LogFactory.getLog(ReportCreationTask.class);

    /**
     * Repository for accessing utility data.
     */
    @Autowired
    private IUtilityRepository utilityRepository;

    /**
     * Repository for accessing user data.
     */
    @Autowired
    private IUserRepository userRepository;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        try {
            Map<String, String> parameters = getStepParameters(chunkContext.getStepContext());

            // The utilities for which reports will be generated.
            String[] utilityId = StringUtils.split(parameters.get(EnumParameter.UTILITY_ID.getValue()), ',');
            // Optional user key filter
            String[] filteredUserKeys = StringUtils.split(parameters.get(EnumParameter.FILTER_USER_KEYS.getValue()), ',');
            // Credentials for accessing the utilities. The order of the
            // credentials must match that of the utility identifiers.
            String[] utilityUsername = StringUtils.split(parameters.get(EnumParameter.ADMIN_ACOUNT.getValue()), ',');
            String[] utilityPassword = StringUtils.split(parameters.get(EnumParameter.ADMIN_PASSWORD.getValue()), ',');
            // Working directory
            String workingDirectory = parameters.get(EnumParameter.WORKING_DIRECTORY.getValue());
            // Output directory
            String outputDirectory = parameters.get(EnumParameter.OUTPUT_DIRECTORY.getValue());
            // Report generation timeout.
            long timeout = Integer.parseInt(parameters.get(EnumParameter.REPORT_TIMEOUT.getValue())) * 1000;
            // Report generation service endpoint.
            String endpointReport = parameters.get(EnumParameter.REPORT_ENDPOINT.getValue());
            // DAIAD API endpoint.
            String endpointApi = parameters.get(EnumParameter.API_ENDPOINT.getValue());
            // Script to execute.
            String schellScript = parameters.get(EnumParameter.REPORT_COMMAND.getValue());
            // Message indicating that a report generation operation was
            // successful. The job parses the command shell output and
            // attempts to resolve report generation result.
            String successMessage = parameters.get(EnumParameter.SUCCESS_LITERAL.getValue());

            for (int index = 0, count = utilityId.length; index < count; index++) {
                UtilityInfo utility = utilityRepository.getUtilityById(Integer.parseInt(utilityId[index]));

                DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyyMMdd");

                DateTime localDateTime = DateTime.now(DateTimeZone.forID(utility.getTimezone()));

                if ((!StringUtils.isBlank(parameters.get(EnumParameter.REFERENCE_DATE_FORMAT.getValue()))) &&
                    (!StringUtils.isBlank(parameters.get(EnumParameter.REFERENCE_DATE_VALUE.getValue())))) {
                    DateTimeFormatter refDateformatter = DateTimeFormat.forPattern(parameters.get(EnumParameter.REFERENCE_DATE_FORMAT.getValue()))
                                                                       .withZone(DateTimeZone.forID(utility.getTimezone()));
                    localDateTime = refDateformatter.parseDateTime(parameters.get(EnumParameter.REFERENCE_DATE_VALUE.getValue()));
                }

                String from = localDateTime.minusMonths(1).dayOfMonth().withMinimumValue().toString(formatter);
                String to = localDateTime.minusMonths(1).dayOfMonth().withMaximumValue().toString(formatter);

                outputDirectory = Paths.get(outputDirectory,
                                            Integer.toString(localDateTime.minusMonths(1)
                                                                          .dayOfMonth()
                                                                          .withMinimumValue().getYear()),
                                            Integer.toString(localDateTime.minusMonths(1)
                                                                          .dayOfMonth()
                                                                          .withMinimumValue().getMonthOfYear())).toString();

                FileUtils.mkdir(new File(outputDirectory), true);

                for (UUID userKey : userRepository.getUserKeysForUtility(utility.getKey())) {
                    if (ignoreUserKey(filteredUserKeys, userKey)) {
                        continue;
                    }
                    AuthenticatedUser user = userRepository.getUserByKey(userKey);

                    try {
                        String filename = Paths.get(outputDirectory, user.getUsername() + "-" + from + "-" + to + ".pdf").toString();

                        /*
                            Script    Shell script that executes the report generation process
                            WorkDir   Working directory for the script execution

                            URL       The NODE server URL
                            API       The API endpoint
                            locale    The locale for page rendering (one of en, el, es)
                            username  User name for API authentication
                            password  User password for API authentication
                            userKey   The user UUID key for which to create report
                            from      The beginning of the period date in ISO-8061 form (YYYYMMDD)
                            to        The end of the period date in ISO-8061 form (YYYYMMDD)
                            output    The output filename (the extension can be one of pdf, png)
                        */

                        String command = String.format("%s %s %s %s %s %s %s %s %s %s %s",
                                                       schellScript,
                                                       workingDirectory,
                                                       endpointReport,
                                                       endpointApi,
                                                       resolveLocale(user.getLocale(), utility.getLocale()),
                                                       utilityUsername[index],
                                                       utilityPassword[index],
                                                       user.getKey().toString(),
                                                       from,
                                                       to,
                                                       filename);

                        CommandExecutor commandExecutor = new CommandExecutor(command, timeout, workingDirectory);

                        boolean success = false;
                        String[] lines = null;
                        String errorMessage = "";

                        if (commandExecutor.execute() == ExitStatus.COMPLETED) {
                            lines = commandExecutor.getOutput();
                            if (lines.length > 0) {
                                if (lines[lines.length - 1].equalsIgnoreCase(successMessage)) {
                                    success = true;
                                } else {
                                    errorMessage = lines[lines.length - 1];
                                }
                            }
                        }
                        if (!success) {
                            if (StringUtils.isBlank(errorMessage)) {
                                logger.warn(String.format("Failed to generated report for user [%s].",
                                                          user.getUsername()));
                            } else {
                                logger.warn(String.format("Failed to generated report for user [%s]. Reason : %s",
                                                          user.getUsername(),
                                                          errorMessage));
                            }
                        }
                    } catch (Exception ex) {
                        logger.error(String.format("Failed to generated report for user [%s].",
                                                   user.getUsername()), ex);
                    }
                }
            }
        } catch (Exception ex) {
            logger.fatal("Report creation has failed.", ex);

            throw ex;
        }
        return RepeatStatus.FINISHED;
    }

    @Override
    public void stop() {

    }

    /**
     * Skip report creation if the user key is not in a valid list of user keys.
     *
     * @param filteredUserKeys a list of user keys.
     * @param userKey the user key to search.
     * @return true if report creation must be skipped.
     */
    private boolean ignoreUserKey(String[] filteredUserKeys, UUID userKey) {
        if (filteredUserKeys == null) {
            return false;
        }
        return ((filteredUserKeys.length > 0) && (!ArrayUtils.contains(filteredUserKeys, userKey.toString())));
    }

    /**
     * Resolves locale.
     *
     * @param userLocale user locale from user profile.
     * @param utilityLocale utility locale.
     * @return the locale.
     */
    private String resolveLocale(String userLocale, String utilityLocale) {
        if (!StringUtils.isBlank(userLocale)) {
            return userLocale;
        }

        if (!StringUtils.isBlank(utilityLocale)) {
            return utilityLocale;
        }

        return "en";
    }

    /**
     * Enumeration of job parameters.
     */
    public static enum EnumParameter {
        /**
         * Not supported parameter.
         */
        NOT_SUPPORTED(""),
        /**
         * Array of utility identifiers for which the reports are created.
         */
        UTILITY_ID("utility.id"),
        /**
         * Array of user keys for which the reports are created. This filter is
         * used in combination with {@link EnumParameter#UTILITY_ID} filter.
         */
        FILTER_USER_KEYS("filter.user.keys"),
        /**
         * Administrative accounts for accessing the utilities. One account must
         * be declared for every utility id.
         */
        ADMIN_ACOUNT("utility.username"),
        /**
         * Administrative account passwords.
         */
        ADMIN_PASSWORD("utility.password"),
        /**
         * Temporary working directory.
         */
        WORKING_DIRECTORY("working.directory"),
        /**
         * Output directory.
         */
        OUTPUT_DIRECTORY("output.directory"),
        /**
         * Report generation timeout.
         */
        REPORT_TIMEOUT("timeout"),
        /**
         * Report server endpoint.
         */
        REPORT_ENDPOINT("endpoint.report"),
        /**
         * Application HTTP API endpoint.
         */
        API_ENDPOINT("endpoint.api"),
        /**
         * External script for invoking report creation.
         */
        REPORT_COMMAND("shell.script"),
        /**
         * Reference date format.
         */
        REFERENCE_DATE_FORMAT("reference.date.format"),
        /**
         * Reference date for selecting the month interval. The date is
         * formatted using the pattern
         * {@link EnumParameter#REFERENCE_DATE_FORMAT} and the generated reports
         * refer to the previous month.
         */
        REFERENCE_DATE_VALUE("reference.date.value"),
        /**
         * Message representing the success of the external script execution.
         */
        SUCCESS_LITERAL("success");

        private final String value;

        public String getValue() {
            return value;
        }

        private EnumParameter(String value) {
            this.value = value;
        }

        public static EnumParameter fromString(String value) {
            for (EnumParameter item : EnumParameter.values()) {
                if (item.getValue().equalsIgnoreCase(value)) {
                    return item;
                }
            }
            return NOT_SUPPORTED;
        }
    }

}
