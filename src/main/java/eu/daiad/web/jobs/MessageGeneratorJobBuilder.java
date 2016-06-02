package eu.daiad.web.jobs;

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
import org.springframework.stereotype.Component;

import eu.daiad.web.model.message.MessageCalculationConfiguration;
import eu.daiad.web.service.message.IMessageService;

@Component
public class MessageGeneratorJobBuilder implements IJobBuilder {
	private static final Log logger = LogFactory.getLog(MessageGeneratorJobBuilder.class);

	@Autowired
	private JobBuilderFactory jobBuilderFactory;

	@Autowired
	private StepBuilderFactory stepBuilderFactory;

	@Autowired
	private IMessageService messageService;

	public MessageGeneratorJobBuilder() {

	}

	private Step generateMessages() {
		return stepBuilderFactory.get("generateMessages").tasklet(new StoppableTasklet() {
			@Override
			public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
				try {
					// Initialize configuration
					MessageCalculationConfiguration config = new MessageCalculationConfiguration();

					config.setStaticTipInterval(Integer.parseInt((String) chunkContext.getStepContext()
									.getJobParameters().get("static.tip.interval")));
					config.setAggregateComputationInterval(Integer.parseInt((String) chunkContext.getStepContext()
									.getJobParameters().get("aggregate.computation.interval")));

					config.setEurosPerKwh(Double.parseDouble((String) chunkContext.getStepContext().getJobParameters()
									.get("euros.per.kwh")));
					config.setAverageGbpPerKwh(Double.parseDouble((String) chunkContext.getStepContext()
									.getJobParameters().get("average.gbp.per.kwh")));
					config.setEurosPerLiter(Double.parseDouble((String) chunkContext.getStepContext()
									.getJobParameters().get("euros.per.liter")));

					config.setDailyBudget(Integer.parseInt((String) chunkContext.getStepContext().getJobParameters()
									.get("daily.budget")));
					config.setWeeklyBudget(Integer.parseInt((String) chunkContext.getStepContext().getJobParameters()
									.get("weekly.budget")));
					config.setMonthlyBudget(Integer.parseInt((String) chunkContext.getStepContext().getJobParameters()
									.get("monthly.budget")));

					config.setDailyBudgetAmphiro(Integer.parseInt((String) chunkContext.getStepContext()
									.getJobParameters().get("daily.budget.amphiro")));
					config.setWeeklyBudgetAmphiro(Integer.parseInt((String) chunkContext.getStepContext()
									.getJobParameters().get("weekly.budget.amphiro")));
					config.setMonthlyBudgetAmphiro(Integer.parseInt((String) chunkContext.getStepContext()
									.getJobParameters().get("monthly.budget.amphiro")));

					// Execute message generation
					messageService.executeAll(config);
				} catch (Exception ex) {
					logger.fatal("Failed to complete message calculation process.", ex);

					throw ex;
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
		return jobBuilderFactory.get(name).incrementer(incrementer).start(generateMessages()).build();
	}
}
