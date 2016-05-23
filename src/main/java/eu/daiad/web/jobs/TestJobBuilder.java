package eu.daiad.web.jobs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersIncrementer;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.daiad.web.model.error.ApplicationException;

@Component
public class TestJobBuilder implements IJobBuilder {

	private static final Log logger = LogFactory.getLog(TestJobBuilder.class);

	@Autowired
	private JobBuilderFactory jobBuilderFactory;

	@Autowired
	private StepBuilderFactory stepBuilderFactory;

	public TestJobBuilder() {

	}

	private static class StatusTasklet implements Tasklet {

		@Override
		public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
			try {
				for (int i = 0; i < 30; i++) {
					Thread.sleep(30000);

					logger.warn(DateTime.now().toString());
				}
			} catch (Exception ex) {
				throw ApplicationException.wrap(ex);
			}
			return RepeatStatus.FINISHED;
		}

	}

	private Step doSomething() {
		return stepBuilderFactory.get("doSomething").tasklet(new StatusTasklet()).build();
	}

	@Override
	public Job build(String name, JobParametersIncrementer incrementer) throws Exception {
		return jobBuilderFactory.get(name).incrementer(incrementer).start(doSomething()).build();
	}
}
