package eu.daiad.web.job.builder;

import java.io.File;
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
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersIncrementer;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.StoppableTasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.model.utility.UtilityInfo;
import eu.daiad.web.repository.application.IUserRepository;
import eu.daiad.web.repository.application.IUtilityRepository;

/**
 * Builder for creating a report generation job.
 */
@Component
public class ReportGenerationJobBuilder extends BaseJobBuilder implements IJobBuilder {

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
     * Creates a step for creating reports.
     *
     * @return the step.
     */
    private Step createReports() {
        return stepBuilderFactory.get("createReports").tasklet(new StoppableTasklet() {
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                try {
                    Map<String, Object> jobParameters = chunkContext.getStepContext().getJobParameters();

                    // The utilities for which reports will be generated.
                    String[] utilityId = StringUtils.split((String) jobParameters.get("utility.id"), ',');
                    // Credentials for accessing the utilities. The order of the
                    // credentials must match that of the utility identifiers.
                    String[] utilityUsername = StringUtils.split((String) jobParameters.get("utility.username"), ',');
                    String[] utilityPassword = StringUtils.split((String) jobParameters.get("utility.password"), ',');
                    // Working directory
                    String workingDirectory = (String) jobParameters.get("working.directory");
                    // Output directory
                    String outputDirectory = (String) jobParameters.get("output.directory");
                    // Report generation timeout.
                    long timeout = Integer.parseInt((String) jobParameters.get("timeout")) * 1000;
                    // Report generation service endpoint.
                    String endpointReport = (String) jobParameters.get("endpoint.report");
                    // DAIAD API endpoint.
                    String endpointApi = (String) jobParameters.get("endpoint.api");
                    // Script to execute.
                    String schellScript = (String) jobParameters.get("shell.script");
                    // Message indicating that a report generation operation was
                    // successful. The job parses the command shell output and
                    // attempts to resolve report generation result.
                    String successMessage = (String) jobParameters.get("success");

                    for (int index = 0, count = utilityId.length; index < count; index++) {
                        UtilityInfo utility = utilityRepository.getUtilityById(Integer.parseInt(utilityId[index]));

                        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyyMMdd");

                        DateTime localDateTime = DateTime.now(DateTimeZone.forID(utility.getTimezone()));

                        String from = localDateTime.minusMonths(1).dayOfMonth().withMinimumValue().toString(formatter);
                        String to = localDateTime.minusMonths(1).dayOfMonth().withMaximumValue().toString(formatter);

                        outputDirectory += Integer.toString(localDateTime.minusMonths(1)
                                                                         .dayOfMonth()
                                                                         .withMinimumValue().getYear()) + "/";
                        outputDirectory += Integer.toString(localDateTime.minusMonths(1)
                                                                         .dayOfMonth()
                                                                         .withMinimumValue().getMonthOfYear()) + "/";

                        FileUtils.mkdir(new File(outputDirectory), true);

                        for (UUID userKey : userRepository.getUserKeysForUtility(utility.getKey())) {
                            AuthenticatedUser user = userRepository.getUserByKey(userKey);

                            try {
                                String filename = outputDirectory + user.getUsername() + "-" + from + "-" + to + ".pdf";

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
        }).build();
    }

    /**
     * Builds a job with the given name and {@link JobParametersIncrementer} instance.
     *
     * @param name the job name.
     * @param incrementer the {@link JobParametersIncrementer} instance to be used during job execution.
     *
     * @return a fully configured job.
     * @throws Exception in case the job can not be instantiated.
     */
    @Override
    public Job build(String name, JobParametersIncrementer incrementer) throws Exception {
        return jobBuilderFactory.get(name).incrementer(incrementer).start(createReports()).build();
    }

}
