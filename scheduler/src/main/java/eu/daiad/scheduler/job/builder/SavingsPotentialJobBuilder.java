package eu.daiad.scheduler.job.builder;

import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersIncrementer;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.listener.ExecutionContextPromotionListener;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.daiad.common.model.query.savings.EnumSavingScenarioStatus;
import eu.daiad.common.model.scheduling.Constants;
import eu.daiad.scheduler.flink.EnumFlinkParameter;
import eu.daiad.scheduler.job.task.CopyFileTask;
import eu.daiad.scheduler.job.task.CreateDirectoryTask;
import eu.daiad.scheduler.job.task.DeleteDirectoryTask;
import eu.daiad.scheduler.job.task.ExportMeterDataToFileTask;
import eu.daiad.scheduler.job.task.SavingsPotentialComputeTask;
import eu.daiad.scheduler.job.task.SavingsPotentialExportMeterSerialTask;
import eu.daiad.scheduler.job.task.SavingsPotentialImportDataTask;
import eu.daiad.scheduler.job.task.YarnFlinkJobTask;
import eu.daiad.common.repository.application.ISavingsPotentialRepository;
import eu.daiad.scheduler.service.scheduling.ISchedulerService;

/**
 * Initializes and submits an Apache Flink job to a YARN cluster for computing
 * savings potential and Water IQ data.
 */
@Component
public class SavingsPotentialJobBuilder extends BaseJobBuilder implements IJobBuilder {

    /**
     * Service for querying, scheduling and launching jobs.
     */
    @Autowired
    private ISchedulerService schedulerService;

    /**
     * Repository for updating savings potential scenarios.
     */
    @Autowired
    private ISavingsPotentialRepository savingsPotentialRepository;

    /**
     * Name of the step that creates the local working directory.
     */
    private static final String STEP_CREATE_LOCAL_WORK_DIR = "create-local-work-dir";

    /**
     * Name of the step that creates the HDFS working directory.
     */
    private static final String STEP_CREATE_HDFS_WORK_DIR = "create-hdfs-work-dir";

    /**
     * Creates a file with all meter serial numbers for a given savings potential scenario.
     */
    private static final String STEP_CREATE_SCENARIO_METER_FILE = "create-scenario-meter-file";

    /**
     * Water meter data export step name.
     */
    private static final String STEP_EXPORT_METER_DATA = "export-data";

    /**
     * Name of step that copies all local files to the HDFS working directory.
     */
    private static final String STEP_COPY_REMOTE_FILES = "copy-remote-files";

    /**
     * Name of step for Flink saving potential algorithm.
     */
    private static final String STEP_FLINK = "flink";

    /**
     * Name of step that imports savings potential and Water IQ data to HBase/PostgreSQL
     */
    private static final String STEP_IMPORT_RESULT = "import-result";

    /**
     * Computes the savings potential per user for a given scenario.
     */
    private static final String STEP_COMPUTE_SAVINGS = "compute-scenario-savings";

    /**
     * Name of the step that deletes the local working directory.
     */
    private static final String STEP_DELETE_LOCAL_WORK_DIR = "delete-local-work-dir";

    /**
     * Name of the step that deletes the HDFS working directory.
     */
    private static final String STEP_DELETE_HDFS_WORK_DIR = "delete-hdfs-work-dir";

    /**
     * Exported SWM serial file name.
     */
    private static final String METER_SERIAL_FILENAME = "SerialData";

    /**
     * Exported SWM data file name.
     */
    private static final String METER_DATA_FILENAME = "MeterData";

    /**
     * Water IQ execution mode.
     */
    public static final String MODE_WATER_IQ = "WATER_IQ";

    /**
     * Savings potential scenario execution mode.
     */
    public static final String MODE_SAVINGS = "SAVINGS";

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
     * Task for exporting water meter serial numbers to a temporary folder for the given savings potential scenario.
     */
    @Autowired
    private SavingsPotentialExportMeterSerialTask savingsPotentialExportMeterSerialTask;

    /**
     * Task for exporting water meter data to a temporary folder. The generated
     * file is used for computing savings potential and Water IQ values.
     */
    @Autowired
    private ExportMeterDataToFileTask exportMeterDataTask;

    /**
     * Task for copying all local files to the HDFS working directory.
     */
    @Autowired
    private CopyFileTask copyRemoteFilesTask;

    /**
     * Savings potential Flink job.
     */
    @Autowired
    private YarnFlinkJobTask yarnFlinkJobTask;

    /**
     * Task for importing savings potential data to PostgreSQL/HBase.
     */
    @Autowired
    private SavingsPotentialImportDataTask savingsPotentialImportDataTask;

    /**
     * Task for computing savings potential per user.
     */
    @Autowired
    private SavingsPotentialComputeTask savingsPotentialComputeTask;

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

                                        // Share local working directory with other tasks
                                        String stepContextKey = STEP_CREATE_LOCAL_WORK_DIR +
                                                                Constants.PARAMETER_NAME_DELIMITER +
                                                                CreateDirectoryTask.EnumOutParameter.NEW_DIRECTORY.getValue();
                                        String localWorkingDir = (String) stepContext.get(stepContextKey);

                                        // Configure export meter serial task
                                        String jobContextKey = STEP_CREATE_SCENARIO_METER_FILE +
                                                               Constants.PARAMETER_NAME_DELIMITER +
                                                               ExportMeterDataToFileTask.EnumInParameter.WORKING_DIRECTORY.getValue();
                                        jobContext.put(jobContextKey, localWorkingDir);

                                        // Configure export meter data task
                                        jobContextKey = STEP_EXPORT_METER_DATA +
                                                        Constants.PARAMETER_NAME_DELIMITER +
                                                        ExportMeterDataToFileTask.EnumInParameter.WORKING_DIRECTORY.getValue();
                                        jobContext.put(jobContextKey, localWorkingDir);

                                        // Configure copy HDFS files task
                                        jobContextKey = STEP_COPY_REMOTE_FILES +
                                                        Constants.PARAMETER_NAME_DELIMITER +
                                                        CopyFileTask.EnumInParameter.SOURCE_PATH.getValue();
                                        jobContext.put(jobContextKey, localWorkingDir);

                                        // Configure delete local files task
                                        jobContextKey = STEP_DELETE_LOCAL_WORK_DIR +
                                                        Constants.PARAMETER_NAME_DELIMITER +
                                                        DeleteDirectoryTask.EnumInParameter.INPUT_DIRECTORY.getValue();
                                        jobContext.put(jobContextKey, localWorkingDir);

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

                                        String stepContextKey = STEP_CREATE_HDFS_WORK_DIR +
                                                                Constants.PARAMETER_NAME_DELIMITER +
                                                                CreateDirectoryTask.EnumOutParameter.NEW_DIRECTORY.getValue();
                                        String hdfsWorkingDir =  (String) stepContext.get(stepContextKey);

                                        // Configure copy HDFS files task
                                        String jobContextKey = STEP_COPY_REMOTE_FILES +
                                                               Constants.PARAMETER_NAME_DELIMITER +
                                                               CopyFileTask.EnumInParameter.TARGET_PATH.getValue();

                                        if (stepContext.containsKey(stepContextKey)) {
                                            jobContext.put(jobContextKey, hdfsWorkingDir);
                                        }

                                        // Configure flink tasks
                                        jobContextKey = STEP_FLINK +
                                                        Constants.PARAMETER_NAME_DELIMITER +
                                                        EnumFlinkParameter.WORKING_DIRECTORY.getValue();
                                        jobContext.put(jobContextKey, hdfsWorkingDir);

                                        String meterDataFilename = hdfsWorkingDir + METER_DATA_FILENAME;

                                        jobContextKey = STEP_FLINK +
                                                        Constants.PARAMETER_NAME_DELIMITER +
                                                        "input.file";
                                        jobContext.put(jobContextKey, meterDataFilename);

                                        // Configure output filenames for import task
                                        String savingsFilename = hdfsWorkingDir + "SavingsPotential";


                                        jobContextKey = STEP_IMPORT_RESULT +
                                                        Constants.PARAMETER_NAME_DELIMITER +
                                                        SavingsPotentialImportDataTask.EnumInParameter.INPUT_FILENAME_SAVINGS.getValue();

                                        jobContext.put(jobContextKey, savingsFilename);

                                        String waterIqFilename = hdfsWorkingDir + "WaterIQ";

                                        jobContextKey = STEP_IMPORT_RESULT +
                                                        Constants.PARAMETER_NAME_DELIMITER +
                                                        SavingsPotentialImportDataTask.EnumInParameter.INPUT_FILENAME_WATER_IQ.getValue();

                                        jobContext.put(jobContextKey, waterIqFilename);

                                        // Configure delete HDFS files task
                                        jobContextKey = STEP_DELETE_HDFS_WORK_DIR +
                                                        Constants.PARAMETER_NAME_DELIMITER +
                                                        DeleteDirectoryTask.EnumInParameter.INPUT_DIRECTORY.getValue();
                                        jobContext.put(jobContextKey, hdfsWorkingDir);

                                        stepContextKey = STEP_CREATE_HDFS_WORK_DIR +
                                                         Constants.PARAMETER_NAME_DELIMITER +
                                                         CreateDirectoryTask.EnumOutParameter.HDFS_PATH.getValue();

                                        jobContextKey = STEP_DELETE_HDFS_WORK_DIR +
                                                        Constants.PARAMETER_NAME_DELIMITER +
                                                        DeleteDirectoryTask.EnumInParameter.HDFS_PATH.getValue();

                                        if (stepContext.containsKey(stepContextKey)) {
                                            jobContext.put(jobContextKey, stepContext.get(stepContextKey));
                                        }

                                        return null;
                                     }

                                 })
                                 .build();
    }

    /**
     * Builds a step for exporting water meter serial numbers for the selected savings potential scenario.
     *
     * @return the configured step.
     */
    private Step exportSerial() {
        return stepBuilderFactory.get(STEP_CREATE_SCENARIO_METER_FILE)
                                 .tasklet(savingsPotentialExportMeterSerialTask)
                                 .listener(new ExecutionContextPromotionListener() {

                                     @Override
                                     public void beforeStep(StepExecution stepExecution) {
                                         ExecutionContext jobContext = stepExecution.getJobExecution().getExecutionContext();

                                         // Scenario key
                                         String jobContextKey = STEP_CREATE_SCENARIO_METER_FILE +
                                                                Constants.PARAMETER_NAME_DELIMITER +
                                                                SavingsPotentialExportMeterSerialTask.EnumInParameter.SCENARIO_KEY.getValue();
                                         jobContext.put(jobContextKey, getScenarioKey(stepExecution.getJobParameters()));

                                         // Set output filename
                                         jobContextKey = STEP_CREATE_SCENARIO_METER_FILE +
                                                         Constants.PARAMETER_NAME_DELIMITER +
                                                         SavingsPotentialExportMeterSerialTask.EnumInParameter.OUTPUT_FILENAME.getValue();
                                         jobContext.put(jobContextKey, METER_SERIAL_FILENAME);
                                     }

                                     @Override
                                     public ExitStatus afterStep(StepExecution stepExecution) {
                                        ExecutionContext jobContext = stepExecution.getJobExecution().getExecutionContext();

                                        String jobContextKey = STEP_EXPORT_METER_DATA +
                                                               Constants.PARAMETER_NAME_DELIMITER +
                                                               ExportMeterDataToFileTask.EnumInParameter.METER_FILTER_FILENAME.getValue();
                                        jobContext.put(jobContextKey, METER_SERIAL_FILENAME);

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
                                     public void beforeStep(StepExecution stepExecution) {
                                         ExecutionContext jobContext = stepExecution.getJobExecution().getExecutionContext();

                                         // Configure export meter data task
                                         int years = 0;

                                         String mode = getExecutionMode(stepExecution.getJobParameters());
                                         switch(mode) {
                                             case MODE_SAVINGS:
                                                 years = 2;
                                                 break;
                                             default:
                                                 years = 1;
                                                 break;
                                         }

                                         String jobContextKey = STEP_EXPORT_METER_DATA +
                                                                Constants.PARAMETER_NAME_DELIMITER +
                                                                ExportMeterDataToFileTask.EnumInParameter.EXPORT_YEARS.getValue();

                                         jobContext.put(jobContextKey, Integer.toString(years));

                                         jobContextKey = STEP_EXPORT_METER_DATA +
                                                         Constants.PARAMETER_NAME_DELIMITER +
                                                         ExportMeterDataToFileTask.EnumInParameter.OUTPUT_FILENAME.getValue();

                                         jobContext.put(jobContextKey, METER_DATA_FILENAME);
                                     }

                                     @Override
                                     public ExitStatus afterStep(StepExecution stepExecution) {
                                         ExecutionContext stepContext = stepExecution.getExecutionContext();
                                         ExecutionContext jobContext = stepExecution.getJobExecution().getExecutionContext();

                                         // Configure utility id
                                         String stepContextKey = STEP_EXPORT_METER_DATA +
                                                                 Constants.PARAMETER_NAME_DELIMITER +
                                                                 ExportMeterDataToFileTask.EnumOutParameter.UTILITY_ID.getValue();

                                         String jobContextKey = STEP_IMPORT_RESULT +
                                                                Constants.PARAMETER_NAME_DELIMITER +
                                                                SavingsPotentialImportDataTask.EnumInParameter.UTILITY_ID.getValue();

                                         if (stepContext.containsKey(stepContextKey)) {
                                             jobContext.put(jobContextKey, stepContext.get(stepContextKey));
                                         }

                                         return null;
                                     }
                                 })
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
     * Builds a step for executing savings potential algorithm.
     *
     * @return the configured step.
     */
    private Step flinkPhase() {
        return stepBuilderFactory.get(STEP_FLINK)
                                 .tasklet(yarnFlinkJobTask)
                                 .build();
    }

    /**
     * Builds a step for importing results to HBase/PostgreSQL
     *
     * @return the configured step.
     */
    private Step importResults() {
        return stepBuilderFactory.get(STEP_IMPORT_RESULT)
                                 .tasklet(savingsPotentialImportDataTask)
                                 .listener(new ExecutionContextPromotionListener() {

                                     @Override
                                     public void beforeStep(StepExecution stepExecution) {
                                         ExecutionContext jobContext = stepExecution.getJobExecution().getExecutionContext();

                                         String jobContextKey = STEP_IMPORT_RESULT +
                                                                Constants.PARAMETER_NAME_DELIMITER +
                                                                SavingsPotentialImportDataTask.EnumInParameter.EXECUTION_MODE.getValue();
                                         jobContext.put(jobContextKey, getExecutionMode(stepExecution.getJobParameters()));

                                         jobContextKey = STEP_IMPORT_RESULT +
                                                         Constants.PARAMETER_NAME_DELIMITER +
                                                         SavingsPotentialImportDataTask.EnumInParameter.SCENARIO_KEY.getValue();
                                         jobContext.put(jobContextKey, getScenarioKey(stepExecution.getJobParameters()));
                                     }

                                     @Override
                                     public ExitStatus afterStep(StepExecution stepExecution) {
                                         // If execution mode is equal to WATER_IQ, compute consumption clusters
                                         String mode = getExecutionMode(stepExecution.getJobParameters());

                                         if(mode.equals(MODE_WATER_IQ)) {
                                             // If data import is successful, compute consumption clusters
                                             if(stepExecution.getExitStatus().getExitCode().equals(ExitStatus.COMPLETED.getExitCode())) {
                                                 String key = STEP_IMPORT_RESULT +
                                                              Constants.PARAMETER_NAME_DELIMITER +
                                                              SavingsPotentialImportDataTask.EnumInParameter.COMPUTE_CONSUMPTION_CLUSTERS.getValue();
                                                 if(stepExecution.getJobParameters().getString(key, "true").equals("true")) {
                                                     schedulerService.launch("CONSUMPTION-CLUSTERS");
                                                 }
                                             }
                                         }

                                         return null;
                                     }
                                 })
                                 .build();
    }

    /**
     * Builds a step for computing savings potential per user.
     *
     * @return the configured step.
     */
    private Step computSavings() {
        return stepBuilderFactory.get(STEP_COMPUTE_SAVINGS)
                                 .tasklet(savingsPotentialComputeTask)
                                 .listener(new ExecutionContextPromotionListener() {

                                     @Override
                                     public void beforeStep(StepExecution stepExecution) {
                                         ExecutionContext jobContext = stepExecution.getJobExecution().getExecutionContext();

                                         String jobContextKey = STEP_COMPUTE_SAVINGS +
                                                                Constants.PARAMETER_NAME_DELIMITER +
                                                                SavingsPotentialComputeTask.EnumInParameter.SCENARIO_KEY.getValue();
                                         jobContext.put(jobContextKey, getScenarioKey(stepExecution.getJobParameters()));
                                     }

                                     @Override
                                     public ExitStatus afterStep(StepExecution stepExecution) {
                                         return null;
                                     }

                                 })
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

    /**
     * Resolves execution mode.
     *
     * @param parameters job parameters.
     * @return the execution mode.
     */
    private String getExecutionMode(JobParameters parameters) {
        String modeParameterKey = Constants.PARAMETER_NAME_DELIMITER + EnumJobInParameter.MODE.getValue();
        String mode = parameters.getString(modeParameterKey);
        if(StringUtils.isBlank(mode)) {
            mode = MODE_WATER_IQ;
        }

        return mode;
    }

    /**
     * Resolves scenario key.
     *
     * @param parameters job parameters.
     * @return the scenario key.
     */
    private String getScenarioKey(JobParameters parameters) {
        String key = Constants.PARAMETER_NAME_DELIMITER + EnumJobInParameter.SCENARIO_KEY.getValue();
        String value = parameters.getString(key);
        if(StringUtils.isBlank(value)) {
            return "";
        }

        return value;
    }

    @Override
    public Job build(String name, JobParametersIncrementer incrementer) throws Exception {
        return jobBuilderFactory.get(name)
                                .incrementer(incrementer)
                                .listener(new JobExecutionListener() {

                                    @Override
                                    public void beforeJob(JobExecution jobExecution) {
                                        // Update scenario status
                                        String key = getScenarioKey(jobExecution.getJobParameters());
                                        if(!StringUtils.isBlank(key)) {
                                            savingsPotentialRepository.updateJobExecution(UUID.fromString(key),
                                                                                          EnumSavingScenarioStatus.RUNNING,
                                                                                          DateTime.now());
                                        }
                                    }

                                    @Override
                                    public void afterJob(JobExecution jobExecution) {
                                        // Update scenario status
                                        String key = getScenarioKey(jobExecution.getJobParameters());
                                        if(!StringUtils.isBlank(key)) {
                                            EnumSavingScenarioStatus status = EnumSavingScenarioStatus.COMPLETED;
                                            if (!jobExecution.getExitStatus().getExitCode().equals("COMPLETED")) {
                                                status = EnumSavingScenarioStatus.FAILED;
                                            }
                                            savingsPotentialRepository.updateJobExecution(UUID.fromString(key),
                                                                                          status,
                                                                                          DateTime.now());
                                        }
                                    }
                                })
                                .start(createLocalWorkingDir())
                                .next(createHdfsWorkingDir())
                                .next(exportSerial())
                                .next(exportData())
                                .next(copyRemoteFiles())
                                .next(flinkPhase())
                                .next(importResults())
                                .next(computSavings())
                                .next(deleteLocalWorkingDir())
                                .next(deleteHdfsWorkingDir())
                                .build();
    }

    /**
     * Enumeration for builder specific parameters.
     */
    public static enum EnumJobInParameter {
        /**
         * Execution mode: Can be SAVINGS or WATER_IQ
         */
        MODE("execution.mode"),
        /**
         * When in SAVINGS mode, this parameters stores the corresponding
         * savings potential scenario key.
         */
        SCENARIO_KEY("scenario.key");

        private final String value;

        public String getValue() {
            return value;
        }

        private EnumJobInParameter(String value) {
            this.value = value;
        }
    }
}
