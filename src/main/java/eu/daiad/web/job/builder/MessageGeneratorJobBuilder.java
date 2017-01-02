package eu.daiad.web.job.builder;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersIncrementer;
import org.springframework.batch.core.Step;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.daiad.web.job.task.MessageGenerationTask;

/**
 * Job for generating messages for users based on their water consumption behavior.
 */
@Component
public class MessageGeneratorJobBuilder extends BaseJobBuilder implements IJobBuilder {

    /**
     * Message generation step name.
     */
    private static final String STEP_GENERATE_MESSAGE = "generate-message";

    /**
     * Task for creating messages.
     */
    @Autowired
    private MessageGenerationTask messageGenerationTask;

    private Step generateMessages() {
        return stepBuilderFactory.get(STEP_GENERATE_MESSAGE)
                                 .tasklet(messageGenerationTask)
                                 .build();
    }

    @Override
    public Job build(String name, JobParametersIncrementer incrementer) throws Exception {
        return jobBuilderFactory.get(name)
                                .incrementer(incrementer)
                                .start(generateMessages())
                                .build();
    }
}
