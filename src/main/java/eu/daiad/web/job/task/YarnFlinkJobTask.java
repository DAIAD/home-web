package eu.daiad.web.job.task;

import java.util.Map;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.SimpleSystemProcessExitCodeMapper;
import org.springframework.batch.core.step.tasklet.StoppableTasklet;
import org.springframework.batch.core.step.tasklet.SystemProcessExitCodeMapper;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import eu.daiad.web.flink.EnumFlinkParameter;
import eu.daiad.web.flink.RunJar;
import eu.daiad.web.model.error.SchedulerErrorCode;

/**
 * Task for submitting an Apache Flink job to a YARN cluster.
 */
@Component
public class YarnFlinkJobTask extends BaseTask implements StoppableTasklet {

    /**
     * Maps the exit code of a system process to {@link ExitStatus} value.
     */
    private SystemProcessExitCodeMapper systemProcessExitCodeMapper = new SimpleSystemProcessExitCodeMapper();

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        String jobName = "";

        try {
            Map<String, String> parameters = getStepParameters(chunkContext.getStepContext());

            jobName = parameters.get(EnumFlinkParameter.JOB_NAME.getValue());

            RunJar runJar = new RunJar();

            int exitCode = runJar.run(parameters);

            ExitStatus exitStatus = systemProcessExitCodeMapper.getExitStatus(exitCode);

            if(!exitStatus.equals(ExitStatus.COMPLETED)) {
                throw createApplicationException(SchedulerErrorCode.FLINK_JOB_INIT_FAILED).set("job", jobName);
            }
        } catch (Throwable t) {
            throw wrapApplicationException(t, SchedulerErrorCode.FLINK_JOB_INIT_FAILED).set("job", jobName);
        }
        return RepeatStatus.FINISHED;
    }

    @Override
    public void stop() {
        // TODO: Add business logic for stopping processing
    }

}
