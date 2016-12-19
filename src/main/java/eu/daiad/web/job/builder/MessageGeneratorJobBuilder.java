package eu.daiad.web.job.builder;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.LocalDateTime;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersIncrementer;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.StoppableTasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.daiad.web.model.device.EnumDeviceType;
import eu.daiad.web.model.message.MessageCalculationConfiguration;
import eu.daiad.web.service.message.IMessageService;

/**
 * Job for generating messages for users based on their water consumption behavior.
 */
@Component
public class MessageGeneratorJobBuilder extends BaseJobBuilder implements IJobBuilder {

    /**
     * Logger instance for writing events using the configured logging API.
     */
    private static final Log logger = LogFactory.getLog(MessageGeneratorJobBuilder.class);

    /**
     * Service for creating application messages.
     */
    @Autowired
    private IMessageService messageService;

    private Step generateMessages() {
        return stepBuilderFactory.get("generateMessages").tasklet(new StoppableTasklet() {
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
                try {
                    Map<String, Object> params = chunkContext.getStepContext().getJobParameters();

                    LocalDateTime refDate = params.containsKey("ref-date")?
                            LocalDateTime.parse((String) params.get("ref-date")) :
                            LocalDateTime.now().minusDays(1);
                    
                    MessageCalculationConfiguration config = new MessageCalculationConfiguration(refDate);

                    config.setOnDemandExecution(true);
                    
                    config.setStaticTipInterval(
                        Integer.parseInt((String) params.get("static.tip.interval")));

                    config.setDailyBudget(
                        EnumDeviceType.METER,    
                        Integer.parseInt((String) params.get("daily.budget")));
                    
                    config.setWeeklyBudget(
                        EnumDeviceType.METER,    
                        Integer.parseInt((String) params.get("weekly.budget")));
                    
                    config.setMonthlyBudget(
                        EnumDeviceType.METER,
                        Integer.parseInt((String) params.get("monthly.budget")));

                    config.setDailyBudget(
                        EnumDeviceType.AMPHIRO,
                        Integer.parseInt((String) params.get("daily.budget.amphiro")));
                    
                    config.setWeeklyBudget(
                        EnumDeviceType.AMPHIRO,
                        Integer.parseInt((String) params.get("weekly.budget.amphiro")));
                    
                    config.setMonthlyBudget(
                        EnumDeviceType.AMPHIRO,
                        Integer.parseInt((String) params.get("monthly.budget.amphiro")));

                    // Generate messages for everything!
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
        return jobBuilderFactory.get(name)
                .incrementer(incrementer)
                .start(generateMessages())
                .build();
    }
}
