package eu.daiad.web.job.builder;

import java.util.HashMap;
import java.util.Map;

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

import eu.daiad.web.mapreduce.RunJar;
import eu.daiad.web.model.error.SchedulerErrorCode;

@Component
public class MapReduceJobBuilder extends BaseJobBuilder implements IJobBuilder {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    private Step submitMapReduceJob() {
        return stepBuilderFactory.get("submitJob").tasklet(new StoppableTasklet() {
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
                String jobName = "";

                try {
                    Map<String, String> properties = new HashMap<String, String>();

                    for (String key : chunkContext.getStepContext().getJobParameters().keySet()) {
                        if (chunkContext.getStepContext().getJobParameters().get(key) instanceof String) {
                            properties.put(key, (String) chunkContext.getStepContext().getJobParameters().get(key));
                        }
                    }

                    jobName = (String) chunkContext.getStepContext().getJobParameters().get("mapreduce.job.name");

                    RunJar runJar = new RunJar();

                    runJar.run(properties);
                } catch (Throwable t) {
                    throw wrapApplicationException(t, SchedulerErrorCode.MAPREDUCE_JOB_INIT_FAIL).set("job", jobName);
                }
                return RepeatStatus.FINISHED;
            }

            @Override
            public void stop() {
                // TODO: Add business logic for stopping processing
            }

        }).build();
    }

    @Override
    public Job build(String name, JobParametersIncrementer incrementer) throws Exception {
        return jobBuilderFactory.get(name).incrementer(incrementer).start(submitMapReduceJob()).build();
    }
}
