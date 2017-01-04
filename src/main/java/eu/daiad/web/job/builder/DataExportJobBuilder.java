package eu.daiad.web.job.builder;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersIncrementer;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.StoppableTasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.daiad.web.model.utility.UtilityInfo;
import eu.daiad.web.repository.application.IExportRepository;
import eu.daiad.web.repository.application.IUtilityRepository;
import eu.daiad.web.service.etl.EnumDataSource;
import eu.daiad.web.service.etl.IDataExportService;
import eu.daiad.web.service.etl.UtilityDataExportQuery;
import eu.daiad.web.service.etl.UtilityDataExportQuery.EnumExportMode;
import eu.daiad.web.service.scheduling.Constants;

/**
 * Helper builder class for initializing a job that exports amphiro b1 and smart
 * water meter data for one or more utilities.
 */
@Component
public class DataExportJobBuilder extends BaseJobBuilder implements IJobBuilder {

    /**
     * Logger instance for writing events using the configured logging API.
     */
    private static final Log logger = LogFactory.getLog(DataExportJobBuilder.class);

    /**
     * Name of the step that deletes stale data files.
     */
    private static final String STEP_CLEAN = "clean";

    /**
     * Data export step name.
     */
    private static final String STEP_EXPORT = "export";

    /**
     * Repository for accessing export file data.
     */
    @Autowired
    private IExportRepository exportRepository;

    /**
     * Repository for accessing utility data.
     */
    @Autowired
    private IUtilityRepository utilityRepository;

    /**
     * Service for exporting utility and user data.
     */
    @Autowired
    private IDataExportService exportService;

    /**
     * Creates a step for deleting expired export data files.
     *
     * @return the step.
     */
    private Step cleanExpiredDataFiles() {
        return stepBuilderFactory.get(STEP_CLEAN).tasklet(new StoppableTasklet() {
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                try {
                    Map<String, Object> parameters = chunkContext.getStepContext().getJobParameters();

                    String[] utilityId = StringUtils.split((String) parameters.get(STEP_CLEAN + Constants.PARAMETER_NAME_DELIMITER + "utility.id"), ',');

                    String days = (String) parameters.get(STEP_CLEAN + Constants.PARAMETER_NAME_DELIMITER + "expire.interval");

                    for (int index = 0, count = utilityId.length; index < count; index++) {
                        exportRepository.deleteExpiredExportFiles(Integer.parseInt(utilityId[index]), Integer.parseInt(days));
                    }
                } catch (Exception ex) {
                    logger.fatal("Data export has failed.", ex);

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
     * Creates a step for exporting utility smart water meter and amphiro b1 data.
     *
     * @return the step.
     */
    private Step exportData() {
        return stepBuilderFactory.get(STEP_EXPORT).tasklet(new StoppableTasklet() {
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                try {
                    Map<String, Object> parameters = chunkContext.getStepContext().getJobParameters();

                    String[] utilityId = StringUtils.split((String) parameters.get(STEP_EXPORT + Constants.PARAMETER_NAME_DELIMITER + "utility.id"), ',');

                    String workingDirectory = (String) parameters.get(STEP_EXPORT + Constants.PARAMETER_NAME_DELIMITER + "working.directory");

                    String outputDirectory = (String) parameters.get(STEP_EXPORT + Constants.PARAMETER_NAME_DELIMITER + "output.directory");

                    String dateFormat = (String) parameters.get(STEP_EXPORT + Constants.PARAMETER_NAME_DELIMITER + "format.date");

                    int defaultUtilityId = Integer.parseInt((String) parameters.get(STEP_EXPORT + Constants.PARAMETER_NAME_DELIMITER + "meter.default.utility"));

                    // Export data for every utility
                    for (int index = 0, count = utilityId.length; index < count; index++) {
                        UtilityInfo utility = utilityRepository.getUtilityById(Integer.parseInt(utilityId[index]));

                        UtilityDataExportQuery query = new UtilityDataExportQuery(utility, outputDirectory);

                        query.setWorkingDirectory(workingDirectory);
                        query.setDateFormat(dateFormat);

                        // Export meter data for trial users only
                        query.setFilename(String.format("meter-%s-trial",utility.getName()).toLowerCase());
                        query.setMode(EnumExportMode.ALL_TRIAL);
                        query.setDescription(String.format("Meter data for all trial users in [%s].", utility.getName()));
                        query.setSource(EnumDataSource.METER);
                        exportService.export(query);

                        // Export meter data for all users if this is the default utility
                        if (utility.getId() == defaultUtilityId) {
                            query.setFilename(String.format("meter-%s-all", utility.getName()).toLowerCase());
                            query.setMode(EnumExportMode.METER_UTILITY);
                            query.setDescription(String.format("Meter data for all users in [%s].", utility.getName()));
                            query.setSource(EnumDataSource.METER);
                            exportService.export(query);
                        }

                        // Export amphiro data for trial users
                        query.setFilename(String.format("amphiro-%s-trial",utility.getName()).toLowerCase());
                        query.setDescription(String.format("Amphiro b1 data for trial users in [%s].", utility.getName()));
                        query.setSource(EnumDataSource.AMPHIRO);
                        exportService.export(query);
                    }
                } catch (Exception ex) {
                    logger.fatal("Data export has failed.", ex);

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
        return jobBuilderFactory.get(name)
                                .incrementer(incrementer)
                                .start(cleanExpiredDataFiles())
                                .next(exportData())
                                .build();
    }

}
