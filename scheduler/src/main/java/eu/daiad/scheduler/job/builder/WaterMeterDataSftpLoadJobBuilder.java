package eu.daiad.scheduler.job.builder;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersIncrementer;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.listener.ExecutionContextPromotionListener;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.daiad.common.model.scheduling.Constants;
import eu.daiad.scheduler.job.task.CreateDirectoryTask;
import eu.daiad.scheduler.job.task.DeleteDirectoryTask;
import eu.daiad.scheduler.job.task.ImportMeterDataTask;
import eu.daiad.scheduler.job.task.SftpTransferTask;

/**
 * Job for downloading smart water meter data from a remote SFTP server and
 * storing it to HBase.
 */
@Component
public class WaterMeterDataSftpLoadJobBuilder extends BaseJobBuilder implements IJobBuilder {

    /**
     * Name of the step that creates the local working directory.
     */
    private static final String STEP_CREATE_WORK_DIR = "create-work-dir";

    /**
     * Transfer data from SFTP step name.
     */
    private static final String STEP_TRANSFER_DATA = "transfer-data";

    /**
     * Data import step name.
     */
    private static final String STEP_IMPORT_DATA = "import-data";

    /**
     * Name of the step that deletes the local working directory.
     */
    private static final String STEP_DELETE_WORK_DIR = "delete-work-dir";

    /**
     * Task for creating local working directory.
     */
    @Autowired
    private CreateDirectoryTask createLocalWorkingDir;

    /**
     * Task for transferring data from SFTP to local working directory.
     */
    @Autowired
    private SftpTransferTask sftpTransferTask;

    /**
     * Task for importing meter data.
     */
    @Autowired
    private ImportMeterDataTask importMeterDataTask;

    /**
     * Task for deleting local working directory.
     */
    @Autowired
    private DeleteDirectoryTask deleteLocalWorkingDir;

    /**
     * Builds a step for creating the local working directory.
     *
     * @return the configured step.
     */
    private Step createLocalWorkingDir() {
        return stepBuilderFactory.get(STEP_CREATE_WORK_DIR)
                                 .tasklet(createLocalWorkingDir)
                                 .listener(new ExecutionContextPromotionListener() {

                                     @Override
                                     public ExitStatus afterStep(StepExecution stepExecution) {
                                        ExecutionContext stepContext = stepExecution.getExecutionContext();
                                        ExecutionContext jobContext = stepExecution.getJobExecution().getExecutionContext();

                                        String prevStepKey = STEP_CREATE_WORK_DIR +
                                                             Constants.PARAMETER_NAME_DELIMITER +
                                                             CreateDirectoryTask.EnumOutParameter.NEW_DIRECTORY.getValue();

                                        // Configure transfer data task
                                        String nextStepKey = STEP_TRANSFER_DATA +
                                                             Constants.PARAMETER_NAME_DELIMITER +
                                                             SftpTransferTask.EnumInParameter.WORKING_DIRECTORY.getValue();

                                        if (stepContext.containsKey(prevStepKey)) {
                                            jobContext.put(nextStepKey, stepContext.get(prevStepKey));
                                        }

                                        // Configure import data task
                                        nextStepKey = STEP_IMPORT_DATA +
                                                      Constants.PARAMETER_NAME_DELIMITER +
                                                      ImportMeterDataTask.EnumInParameter.WORKING_DIRECTORY.getValue();

                                        if (stepContext.containsKey(prevStepKey)) {
                                            jobContext.put(nextStepKey, stepContext.get(prevStepKey));
                                        }

                                        // Configure delete local files task
                                        nextStepKey = STEP_DELETE_WORK_DIR +
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
     * Builds a step for fetching data from a SFTP server.
     *
     * @return the configured step.
     */
    private Step transferData() {
        return stepBuilderFactory.get(STEP_TRANSFER_DATA)
                        .tasklet(sftpTransferTask)
                        .build();
    }

    /**
     * Builds a step for parsing data files and importing rows to HBASE.
     *
     * @return the configured step.
     */
	private Step importData() {
        return stepBuilderFactory.get(STEP_IMPORT_DATA)
                        .tasklet(importMeterDataTask)
                        .build();
	}

    /**
     * Builds a step for deleting the local working directory.
     *
     * @return the configured step.
     */
    private Step deleteLocalWorkingDir() {
        return stepBuilderFactory.get(STEP_DELETE_WORK_DIR)
                                 .tasklet(deleteLocalWorkingDir)
                                 .build();
    }

	@Override
	public Job build(String name, JobParametersIncrementer incrementer) throws Exception {
		return jobBuilderFactory.get(name)
		                        .incrementer(incrementer)
		                        .start(createLocalWorkingDir())
		                        .next(transferData())
		                        .next(importData())
		                        .next(deleteLocalWorkingDir())
		                        .build();
	}
}
