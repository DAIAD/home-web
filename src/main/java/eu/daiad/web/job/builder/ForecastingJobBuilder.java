package eu.daiad.web.job.builder;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersIncrementer;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.listener.ExecutionContextPromotionListener;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.daiad.web.flink.EnumFlinkParameter;
import eu.daiad.web.job.task.CopyFileTask;
import eu.daiad.web.job.task.CreateDirectoryTask;
import eu.daiad.web.job.task.DeleteDirectoryTask;
import eu.daiad.web.job.task.ExportForecastingQueryToFileTask;
import eu.daiad.web.job.task.ExportMeterDataToFileTask;
import eu.daiad.web.job.task.ImportForecastingDataToHBaseTask;
import eu.daiad.web.job.task.YarnFlinkJobTask;
import eu.daiad.web.service.scheduling.Constants;

/**
 * Initializes and submits an Apache Flink job to a YARN cluster for computing
 * water consumption forecasting data.
 */
@Component
public class ForecastingJobBuilder extends BaseJobBuilder implements IJobBuilder {

    /**
     * Name of the step that creates the local working directory.
     */
    private static final String STEP_CREATE_LOCAL_WORK_DIR = "create-local-work-dir";

    /**
     * Name of the step that creates the HDFS working directory.
     */
    private static final String STEP_CREATE_HDFS_WORK_DIR = "create-hdfs-work-dir";

    /**
     * Water meter data export step name.
     */
    private static final String STEP_EXPORT_METER_DATA = "export-data";

    /**
     * Query creation step name.
     */
    private static final String STEP_CREATE_QUERY = "create-query";

    /**
     * Name of step that copies all local files to the working directory.
     */
    private static final String STEP_COPY_LOCAL_FILES = "copy-local-files";

    /**
     * Name of step that copies all local files to the working directory.
     */
    private static final String STEP_COPY_REMOTE_FILES = "copy-remote-files";

    /**
     * Name of step for Flink forecasting algorithm phase 1.
     */
    private static final String STEP_FLINK_PHASE_1 = "flink-p1";

    /**
     * Name of step for Flink forecasting algorithm phase 2.
     */
    private static final String STEP_FLINK_PHASE_2 = "flink-p2";

    /**
     * Name of step that imports forecasting data to HBase
     */
    private static final String STEP_IMPORT_RESULT = "import-result";

    /**
     * Name of the step that creates the local working directory.
     */
    private static final String STEP_DELETE_LOCAL_WORK_DIR = "delete-local-work-dir";

    /**
     * Name of the step that creates the HDFS working directory.
     */
    private static final String STEP_DELETE_HDFS_WORK_DIR = "delete-hdfs-work-dir";

    /**
     * Task for creating local working directory.
     */
    @Autowired
    private CreateDirectoryTask createLocalWorkingDir;

    /**
     * Task for creating HDFS working directory.
     */
    @Autowired
    private CreateDirectoryTask createHdfsWorkingDir;

    /**
     * Task for exporting water meter data to a temporary folder. The generated
     * file is used for training the forecasting algorithm.
     */
    @Autowired
    private ExportMeterDataToFileTask exportMeterDataTask;

    /**
     * Task for generating the query used by the forecasting algorithm.
     */
    @Autowired
    private ExportForecastingQueryToFileTask exportForecastingQueryToFileTask;

    /**
     * Task for copying all local files to the working directory.
     */
    @Autowired
    private CopyFileTask copyLocalFilesTask;

    /**
     * Task for copying all local files to the HDFS working directory.
     */
    @Autowired
    private CopyFileTask copyRemoteFilesTask;

    /**
     * Forecasting algorithm phase 1.
     */
    @Autowired
    private YarnFlinkJobTask yarnFlinkJobTask1;

    /**
     * Forecasting algorithm phase 2.
     */
    @Autowired
    private YarnFlinkJobTask yarnFlinkJobTask2;

    /**
     * Task for importing forecasting data to HBase.
     */
    @Autowired
    private ImportForecastingDataToHBaseTask importForecastingDataToHBaseTask;

    /**
     * Task for deleting local working directory.
     */
    @Autowired
    private DeleteDirectoryTask deleteLocalWorkingDir;

    /**
     * Task for deleting HDFS working directory.
     */
    @Autowired
    private DeleteDirectoryTask deleteHdfsWorkingDir;

    /**
     * Builds a step for creating the local working directory.
     *
     * @return the configured step.
     */
    private Step createLocalWorkingDir() {
        return stepBuilderFactory.get(STEP_CREATE_LOCAL_WORK_DIR)
                                 .tasklet(createLocalWorkingDir)
                                 .listener(new ExecutionContextPromotionListener() {

                                     @Override
                                     public ExitStatus afterStep(StepExecution stepExecution) {
                                        ExecutionContext stepContext = stepExecution.getExecutionContext();
                                        ExecutionContext jobContext = stepExecution.getJobExecution().getExecutionContext();

                                        String prevStepKey = STEP_CREATE_LOCAL_WORK_DIR +
                                                             Constants.PARAMETER_NAME_DELIMITER +
                                                             CreateDirectoryTask.EnumOutParameter.NEW_DIRECTORY.getValue();

                                        // Configure export meter data task
                                        String nextStepKey = STEP_EXPORT_METER_DATA +
                                                             Constants.PARAMETER_NAME_DELIMITER +
                                                             ExportMeterDataToFileTask.EnumInParameter.WORKING_DIRECTORY.getValue();

                                        if (stepContext.containsKey(prevStepKey)) {
                                            jobContext.put(nextStepKey, stepContext.get(prevStepKey));
                                        }

                                        // Configure query creation task
                                        nextStepKey = STEP_CREATE_QUERY +
                                                      Constants.PARAMETER_NAME_DELIMITER +
                                                      ExportForecastingQueryToFileTask.EnumInParameter.WORKING_DIRECTORY.getValue();

                                        if (stepContext.containsKey(prevStepKey)) {
                                            jobContext.put(nextStepKey, stepContext.get(prevStepKey));
                                        }

                                        // Configure copy local files task
                                        nextStepKey = STEP_COPY_LOCAL_FILES +
                                                      Constants.PARAMETER_NAME_DELIMITER +
                                                      CopyFileTask.EnumInParameter.TARGET_PATH.getValue();

                                        if (stepContext.containsKey(prevStepKey)) {
                                            jobContext.put(nextStepKey, stepContext.get(prevStepKey));
                                        }

                                        // Configure copy HDFS files task
                                        nextStepKey = STEP_COPY_REMOTE_FILES +
                                                      Constants.PARAMETER_NAME_DELIMITER +
                                                      CopyFileTask.EnumInParameter.SOURCE_PATH.getValue();

                                        if (stepContext.containsKey(prevStepKey)) {
                                            jobContext.put(nextStepKey, stepContext.get(prevStepKey));
                                        }

                                        // Configure delete local files task
                                        nextStepKey = STEP_DELETE_LOCAL_WORK_DIR +
                                                      Constants.PARAMETER_NAME_DELIMITER +
                                                      DeleteDirectoryTask.EnumInParameter.INPUT_DIRECTORY.getValue();

                                        if (stepContext.containsKey(prevStepKey)) {
                                            jobContext.put(nextStepKey, stepContext.get(prevStepKey));
                                        }

                                        return null;
                                     }

                                 })
                                 .build();
    }

    /**
     * Builds a step for creating the HDFS working directory.
     *
     * @return the configured step.
     */
    private Step createHdfsWorkingDir() {
        return stepBuilderFactory.get(STEP_CREATE_HDFS_WORK_DIR)
                                 .tasklet(createHdfsWorkingDir)
                                 .listener(new ExecutionContextPromotionListener() {

                                     @Override
                                     public ExitStatus afterStep(StepExecution stepExecution) {
                                        ExecutionContext stepContext = stepExecution.getExecutionContext();
                                        ExecutionContext jobContext = stepExecution.getJobExecution().getExecutionContext();

                                        String prevStepKey = STEP_CREATE_HDFS_WORK_DIR +
                                                             Constants.PARAMETER_NAME_DELIMITER +
                                                             CreateDirectoryTask.EnumOutParameter.NEW_DIRECTORY.getValue();


                                        // Configure copy HDFS files task
                                        String nextStepKey = STEP_COPY_REMOTE_FILES +
                                                             Constants.PARAMETER_NAME_DELIMITER +
                                                             CopyFileTask.EnumInParameter.TARGET_PATH.getValue();

                                        if (stepContext.containsKey(prevStepKey)) {
                                            jobContext.put(nextStepKey, stepContext.get(prevStepKey));
                                        }

                                        // Configure flink tasks
                                        nextStepKey = STEP_FLINK_PHASE_1 +
                                                      Constants.PARAMETER_NAME_DELIMITER +
                                                      EnumFlinkParameter.WORKING_DIRECTORY.getValue();

                                        if (stepContext.containsKey(prevStepKey)) {
                                            jobContext.put(nextStepKey, stepContext.get(prevStepKey));
                                        }

                                        nextStepKey = STEP_FLINK_PHASE_2 +
                                                      Constants.PARAMETER_NAME_DELIMITER +
                                                      EnumFlinkParameter.WORKING_DIRECTORY.getValue();

                                        if (stepContext.containsKey(prevStepKey)) {
                                            jobContext.put(nextStepKey, stepContext.get(prevStepKey));
                                        }

                                        // Configure input filename
                                        String input = (String) stepContext.get(prevStepKey) + "FinalResults";


                                        nextStepKey = STEP_IMPORT_RESULT +
                                                      Constants.PARAMETER_NAME_DELIMITER +
                                                      ImportForecastingDataToHBaseTask.EnumInParameter.INPUT_FILENAME.getValue();

                                        if (stepContext.containsKey(prevStepKey)) {
                                            jobContext.put(nextStepKey, input);
                                        }

                                        // Configure delete HDFS files task
                                        nextStepKey = STEP_DELETE_HDFS_WORK_DIR +
                                                      Constants.PARAMETER_NAME_DELIMITER +
                                                      DeleteDirectoryTask.EnumInParameter.INPUT_DIRECTORY.getValue();

                                        if (stepContext.containsKey(prevStepKey)) {
                                            jobContext.put(nextStepKey, stepContext.get(prevStepKey));
                                        }

                                        nextStepKey = STEP_DELETE_HDFS_WORK_DIR +
                                                      Constants.PARAMETER_NAME_DELIMITER +
                                                      DeleteDirectoryTask.EnumInParameter.HDFS_PATH.getValue();

                                        prevStepKey = STEP_CREATE_HDFS_WORK_DIR +
                                                      Constants.PARAMETER_NAME_DELIMITER +
                                                      CreateDirectoryTask.EnumOutParameter.HDFS_PATH.getValue();

                                        if (stepContext.containsKey(prevStepKey)) {
                                            jobContext.put(nextStepKey, stepContext.get(prevStepKey));
                                        }

                                        return null;
                                     }

                                 })
                                 .build();
    }

    /**
     * Builds a step for exporting water meter data to a temporary file.
     *
     * @return the configured step.
     */
    private Step exportData() {

        return stepBuilderFactory.get(STEP_EXPORT_METER_DATA)
                                 .tasklet(exportMeterDataTask)
                                 .listener(new ExecutionContextPromotionListener() {

                                     @Override
                                     public ExitStatus afterStep(StepExecution stepExecution) {
                                         ExecutionContext stepContext = stepExecution.getExecutionContext();
                                         ExecutionContext jobContext = stepExecution.getJobExecution().getExecutionContext();

                                         // Configure utility id
                                         String prevStepKey = STEP_EXPORT_METER_DATA +
                                                              Constants.PARAMETER_NAME_DELIMITER +
                                                              ExportMeterDataToFileTask.EnumOutParameter.UTILITY_ID.getValue();

                                         String nextStepKey = STEP_IMPORT_RESULT +
                                                              Constants.PARAMETER_NAME_DELIMITER +
                                                              ImportForecastingDataToHBaseTask.EnumInParameter.UTILITY_ID.getValue();

                                         if (stepContext.containsKey(prevStepKey)) {
                                             jobContext.put(nextStepKey, stepContext.get(prevStepKey));
                                         }

                                         return null;
                                     }
                                 })
                                 .build();
    }

    /**
     * Builds a step for creating a forecasting query.
     *
     * @return the configured step.
     */
    private Step createQuery() {
        return stepBuilderFactory.get(STEP_CREATE_QUERY)
                                 .tasklet(exportForecastingQueryToFileTask)
                                 .build();
    }

    /**
     * Builds a step for copying files to the working directory.
     *
     * @return the configured step.
     */
    private Step copyLocalFiles() {
        return stepBuilderFactory.get(STEP_COPY_LOCAL_FILES)
                                 .tasklet(copyLocalFilesTask)
                                 .build();
    }

    /**
     * Builds a step for copying files to HDFS working directory.
     *
     * @return the configured step.
     */
    private Step copyRemoteFiles() {
        return stepBuilderFactory.get(STEP_COPY_REMOTE_FILES)
                                 .tasklet(copyRemoteFilesTask)
                                 .build();
    }

    /**
     * Builds a step for executing forecasting algorithm phase 1.
     *
     * @return the configured step.
     */
    private Step flinkPhase1() {
        return stepBuilderFactory.get(STEP_FLINK_PHASE_1)
                                 .tasklet(yarnFlinkJobTask1)
                                 .build();
    }

    /**
     * Builds a step for executing forecasting algorithm phase 2.
     *
     * @return the configured step.
     */
    private Step flinkPhase2() {
        return stepBuilderFactory.get(STEP_FLINK_PHASE_2)
                                 .tasklet(yarnFlinkJobTask2)
                                 .build();
    }

    /**
     * Builds a step for importing forecasting data to HBase.
     *
     * @return the configured step.
     */
    private Step importForecastingDataToHBase() {
        return stepBuilderFactory.get(STEP_IMPORT_RESULT)
                                 .tasklet(importForecastingDataToHBaseTask)
                                 .build();
    }

    /**
     * Builds a step for deleting the local working directory.
     *
     * @return the configured step.
     */
    private Step deleteLocalWorkingDir() {
        return stepBuilderFactory.get(STEP_DELETE_LOCAL_WORK_DIR)
                                 .tasklet(deleteLocalWorkingDir)
                                 .build();
    }

    /**
     * Builds a step for deleting the HDFS working directory.
     *
     * @return the configured step.
     */
    private Step deleteHdfsWorkingDir() {
        return stepBuilderFactory.get(STEP_DELETE_HDFS_WORK_DIR)
                                 .tasklet(deleteHdfsWorkingDir)
                                 .build();
    }

    @Override
    public Job build(String name, JobParametersIncrementer incrementer) throws Exception {
        return jobBuilderFactory.get(name)
                                .incrementer(incrementer)
                                .start(createLocalWorkingDir())
                                .next(createHdfsWorkingDir())
                                .next(exportData())
                                .next(createQuery())
                                .next(copyLocalFiles())
                                .next(copyRemoteFiles())
                                .next(flinkPhase1())
                                .next(flinkPhase2())
                                .next(importForecastingDataToHBase())
                                .next(deleteLocalWorkingDir())
                                .next(deleteHdfsWorkingDir())
                                .build();
    }

}
