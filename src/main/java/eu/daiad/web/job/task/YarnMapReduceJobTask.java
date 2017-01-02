package eu.daiad.web.job.task;

import java.util.Map;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.StoppableTasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import eu.daiad.web.mapreduce.EnumMapReduceParameter;
import eu.daiad.web.mapreduce.RunJar;
import eu.daiad.web.model.error.SchedulerErrorCode;

/**
 * Task for submitting a MapReduce job to a YARN cluster.
 */
@Component
public class YarnMapReduceJobTask extends BaseTask implements StoppableTasklet {

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        String jobName = "";

        try {
            Map<String, String> parameters = getStepParameters(chunkContext.getStepContext());

            jobName = parameters.get(EnumMapReduceParameter.JOB_NAME.getValue());

            RunJar runJar = new RunJar();

            runJar.run(parameters);
        } catch (Throwable t) {
            throw wrapApplicationException(t, SchedulerErrorCode.MAPREDUCE_JOB_INIT_FAILED).set("job", jobName);
        }
        return RepeatStatus.FINISHED;
    }

    @Override
    public void stop() {
        // TODO: Add business logic for stopping processing
    }
}
