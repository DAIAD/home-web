package eu.daiad.web.jobs;

import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.recommendation.MessageCalculationConfiguration;
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
import eu.daiad.web.service.IMessageService;

/**
 *
 * @author nkarag
 */
public class MessageGeneratorJob implements IJobBuilder{
	private static final Log logger = LogFactory.getLog(MessageGeneratorJob.class);

	@Autowired
	private JobBuilderFactory jobBuilderFactory;

	@Autowired
	private StepBuilderFactory stepBuilderFactory;

        @Autowired
        private IMessageService iMessageService;
        
	public MessageGeneratorJob() {

	}

    private Step generateMessages() {
        return stepBuilderFactory.get("generateMessages").tasklet(new StoppableTasklet() {
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
                try {
                    
                    MessageCalculationConfiguration config = new MessageCalculationConfiguration();
                    
                    //config is initialized with default values. 
                    iMessageService.execute(config);
                    
                } catch (Exception ex) {
                    logger.fatal("Failed to complete message calculation process.", ex);

                    throw ApplicationException.wrap(ex);
                }
                return RepeatStatus.FINISHED;
            }

            @Override
            public void stop() {               
                iMessageService.cancel();
            }
        }).build();
    }

	@Override
	public Job build(String name, JobParametersIncrementer incrementer) throws Exception {
		return jobBuilderFactory.get(name).incrementer(incrementer).start(generateMessages()).build();
	}    
}
