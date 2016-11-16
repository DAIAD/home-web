package eu.daiad.web.job.builder;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersIncrementer;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
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

/**
 * Builder for creating a data export job.
 */
@Component
public class DataExportJobBuilder implements IJobBuilder {

    /**
     * Logger instance for writing events using the configured logging API.
     */
    private static final Log logger = LogFactory.getLog(DataExportJobBuilder.class);

    /**
     * Job builder factory.
     */
    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    /**
     * Step builder factory.
     */
    @Autowired
    private StepBuilderFactory stepBuilderFactory;

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
        return stepBuilderFactory.get("clean").tasklet(new StoppableTasklet() {
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                try {
                    String[] utilityId = StringUtils.split((String) chunkContext.getStepContext().getJobParameters().get("utility.id"), ',');

                    String days = (String) chunkContext.getStepContext().getJobParameters().get("expire.interval");

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
        return stepBuilderFactory.get("export").tasklet(new StoppableTasklet() {
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                try {
                    String[] utilityId = StringUtils.split((String) chunkContext.getStepContext().getJobParameters().get("utility.id"), ',');

                    String workingDirectory = (String) chunkContext.getStepContext().getJobParameters().get("working.directory");

                    String outputDirectory = (String) chunkContext.getStepContext().getJobParameters().get("output.directory");

                    String dateFormat = (String) chunkContext.getStepContext().getJobParameters().get("format.date");

                    int defaultUtilityId = Integer.parseInt((String) chunkContext.getStepContext().getJobParameters().get("meter.default.utility"));

                    // Export data for every utility
                    for (int index = 0, count = utilityId.length; index < count; index++) {
                        UtilityInfo utility = utilityRepository.getUtilityById(Integer.parseInt(utilityId[index]));

                        UtilityDataExportQuery query = new UtilityDataExportQuery(utility, outputDirectory);

                        query.setWorkingDirectory(workingDirectory);
                        query.setDateFormat(dateFormat);

                        // Export meter data for trial users only
                        query.setFilename(String.format("meter-%s-trial",utility.getName()).toLowerCase());
                        query.setExportUserDataOnly(true);
                        query.setDescription(String.format("Meter data for all trial users in [%s].", utility.getName()));
                        query.setSource(EnumDataSource.METER);
                        exportService.export(query);

                        // Export meter data for all users if this is the default utility
                        if (utility.getId() == defaultUtilityId) {
                            query.setFilename(String.format("meter-%s-all", utility.getName()).toLowerCase());
                            query.setExportUserDataOnly(false);
                            query.setDescription(String.format("Meter data for all users in [%s].", utility.getName()));
                            query.setSource(EnumDataSource.METER);
                            exportService.export(query);
                        }

                        // Export amphiro data for trial users
                        query.setFilename(String.format("amphiro-%s-trial",utility.getName()).toLowerCase());
                        query.setExportUserDataOnly(false);
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
