package eu.daiad.web.job.task;

import java.io.File;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

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

import eu.daiad.web.job.builder.CommandExecutor;
import eu.daiad.web.job.builder.ReportGenerationJobBuilder;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.model.utility.UtilityInfo;
import eu.daiad.web.repository.application.IUserRepository;
import eu.daiad.web.repository.application.IUtilityRepository;

/**
 * Task for generating reports
 */
@Component
public class ReportCreationTask extends BaseTask implements StoppableTasklet {

    /**
     * Logger instance for writing events using the configured logging API.
     */
    private static final Log logger = LogFactory.getLog(ReportGenerationJobBuilder.class);

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
                    AuthenticatedUser user = userRepository.getUserByKey(userKey);

                    try {
                        String filename = Paths.get(outputDirectory, user.getUsername() + "-" + from + "-" + to + ".pdf").toString();

                        String command = String.format("%s %s %s %s %s %s:%s %s %s %s %s",
                                                       schellScript,
                                                       workingDirectory,
                                                       endpointReport,
                                                       endpointApi,
                                                       resolveLocale(user.getLocale(), utility.getLocale()),
                                                       utilityUsername[index],
                                                       user.getUsername(),
                                                       utilityPassword[index],
                                                       from,
                                                       to,
                                                       filename);

                        CommandExecutor commandExecutor = new CommandExecutor(command, timeout, workingDirectory);

                        boolean success = false;
                        String errorMessage = "";

                        if (commandExecutor.execute() == ExitStatus.COMPLETED) {
                            String[] lines = commandExecutor.getOutput();
                            if (lines.length > 0) {
                                if (lines[lines.length - 1].equals(successMessage)) {
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
                                                          errorMessage,
                                                          user.getUsername()));
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
