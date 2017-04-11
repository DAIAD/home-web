package eu.daiad.web.job.builder;

import java.util.Map;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParametersIncrementer;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.listener.ExecutionContextPromotionListener;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.daiad.web.job.task.CreateDirectoryTask;
import eu.daiad.web.job.task.DeleteDirectoryTask;
import eu.daiad.web.job.task.ExportGroupMemberToFileTask;
import eu.daiad.web.job.task.YarnMapReduceJobTask;
import eu.daiad.web.mapreduce.EnumJobMapReduceParameter;
import eu.daiad.web.service.scheduling.Constants;

/**
 * Initializes and submits a MapReduce job to YARN.
 */
@Component
public class MeterForecastingDataAggregationJobBuilder extends BaseJobBuilder implements IJobBuilder {

    /**
     * Name of the step that creates a local working directory.
     */
    private static final String STEP_CREATE_LOCAL_WORK_DIR = "create-local-work-dir";

    /**
     * Name of the step that exports group members.
     */
    private static final String STEP_EXPORT_GROUP_MEMBERS = "export-group-members";

    /**
     * Job submission step name.
     */
    private static final String STEP_SUBMIT_JOB = "submit-job";

    /**
     * Name of the step that deletes the local working directory.
     */
    private static final String STEP_DELETE_LOCAL_WORK_DIR = "delete-local-work-dir";

    /**
     * Task for creating a local working directory.
     */
    @Autowired
    private CreateDirectoryTask createLocalWorkingDir;

    /**
     * Task for exporting group members.
     */
    @Autowired
    private ExportGroupMemberToFileTask exportGroupMemberToFileTask;

    /**
     * Task for submitting a MapReduce job to YARN.
     */
    @Autowired
    private YarnMapReduceJobTask yarnMapReduceJobTask;

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
        return stepBuilderFactory.get(STEP_CREATE_LOCAL_WORK_DIR)
                                 .tasklet(createLocalWorkingDir)
                                 .listener(new ExecutionContextPromotionListener() {

                                     @Override
                                     public ExitStatus afterStep(StepExecution stepExecution) {
                                        ExecutionContext stepContext = stepExecution.getExecutionContext();
                                        ExecutionContext jobContext = stepExecution.getJobExecution().getExecutionContext();

                                        // Share new directory with other tasks
                                        String stepContextKey = STEP_CREATE_LOCAL_WORK_DIR +
                                                                Constants.PARAMETER_NAME_DELIMITER +
                                                                CreateDirectoryTask.EnumOutParameter.NEW_DIRECTORY.getValue();

                                        // Configure member export task
                                        String jobContextKey = STEP_EXPORT_GROUP_MEMBERS +
                                                               Constants.PARAMETER_NAME_DELIMITER +
                                                               ExportGroupMemberToFileTask.EnumInParameter.WORKING_DIRECTORY.getValue();

                                        if (stepContext.containsKey(stepContextKey)) {
                                            jobContext.put(jobContextKey, stepContext.get(stepContextKey));
                                        }

                                        // Set path for the files that will be cached during the MapReduce job execution.
                                        jobContextKey = STEP_SUBMIT_JOB +
                                                        Constants.PARAMETER_NAME_DELIMITER +
                                                        EnumJobMapReduceParameter.LOCAL_CACHE_PATH.getValue();

                                        if (stepContext.containsKey(stepContextKey)) {
                                            jobContext.put(jobContextKey, stepContext.get(stepContextKey));
                                        }

                                        // Configure delete local files task
                                        jobContextKey = STEP_DELETE_LOCAL_WORK_DIR +
                                                        Constants.PARAMETER_NAME_DELIMITER +
                                                        DeleteDirectoryTask.EnumInParameter.INPUT_DIRECTORY.getValue();

                                        if (stepContext.containsKey(stepContextKey)) {
                                            jobContext.put(jobContextKey, stepContext.get(stepContextKey));
                                        }

                                        return null;
                                     }

                                 })
                                 .build();
    }

    /**
     * Builds a step for exporting group members.
     *
     * @return the configured step.
     */
    private Step exportGroupMembers() {
        return stepBuilderFactory.get(STEP_EXPORT_GROUP_MEMBERS)
                                 .tasklet(exportGroupMemberToFileTask)
                                 .listener(new ExecutionContextPromotionListener() {

                                     @Override
                                     public ExitStatus afterStep(StepExecution stepExecution) {
                                        Map<String, JobParameter> jobParameters = stepExecution.getJobParameters().getParameters();
                                        ExecutionContext jobContext = stepExecution.getJobExecution().getExecutionContext();

                                        // Set groups and users filenames
                                        String jobParameterKey = STEP_EXPORT_GROUP_MEMBERS +
                                                                 Constants.PARAMETER_NAME_DELIMITER +
                                                                 ExportGroupMemberToFileTask.EnumInParameter.OUTPUT_FILENAME_GROUPS.getValue();

                                        String jobContextKey = STEP_SUBMIT_JOB +
                                                               Constants.PARAMETER_NAME_DELIMITER +
                                                               EnumAggregationJobParameter.FILENAME_GROUPS.getValue();

                                        if (jobParameters.containsKey(jobParameterKey)) {
                                            jobContext.put(jobContextKey, jobParameters.get(jobParameterKey));
                                        }

                                        jobParameterKey = STEP_EXPORT_GROUP_MEMBERS +
                                                          Constants.PARAMETER_NAME_DELIMITER +
                                                          ExportGroupMemberToFileTask.EnumInParameter.OUTPUT_FILENAME_USERS.getValue();

                                        jobContextKey = STEP_SUBMIT_JOB +
                                                        Constants.PARAMETER_NAME_DELIMITER +
                                                        EnumAggregationJobParameter.FILENAME_USERS.getValue();

                                        if (jobParameters.containsKey(jobParameterKey)) {
                                            jobContext.put(jobContextKey, jobParameters.get(jobParameterKey));
                                        }

                                        return null;
                                     }

                                 })
                                 .build();
    }

    /**
     * Builds a step for executing a MapReduce job on a YARN cluster.
     *
     * @return the configured step.
     */
    private Step submitJob() {
        return stepBuilderFactory.get(STEP_SUBMIT_JOB)
                                 .tasklet(yarnMapReduceJobTask)
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

    @Override
    public Job build(String name, JobParametersIncrementer incrementer) throws Exception {
        return jobBuilderFactory.get(name)
                                .incrementer(incrementer)
                                .start(createLocalWorkingDir())
                                .next(exportGroupMembers())
                                .next(submitJob())
                                .next(deleteLocalWorkingDir())
                                .build();
    }
}
