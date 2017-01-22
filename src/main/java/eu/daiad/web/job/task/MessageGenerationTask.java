package eu.daiad.web.job.task;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.LocalDateTime;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.StoppableTasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.daiad.web.job.builder.MessageGeneratorJobBuilder;
import eu.daiad.web.model.device.EnumDeviceType;
import eu.daiad.web.service.message.IMessageGeneratorService;

/**
 * Task for generating reports
 */
@Component
public class MessageGenerationTask extends BaseTask implements StoppableTasklet {

    /**
     * Logger instance for writing events using the configured logging API.
     */
    private static final Log logger = LogFactory.getLog(MessageGeneratorJobBuilder.class);

    /**
     * Service for creating application messages.
     */
    @Autowired
    private IMessageGeneratorService messageService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        try {
            Map<String, String> parameters = getStepParameters(chunkContext.getStepContext());

            LocalDateTime refDate = parameters.containsKey(EnumParameter.REFERENCE_DATETIME.getValue())?
                    LocalDateTime.parse(parameters.get(EnumParameter.REFERENCE_DATETIME.getValue())) :
                    LocalDateTime.now().minusDays(1);

            IMessageGeneratorService.Configuration config = new IMessageGeneratorService.Configuration(refDate);

            config.setOnDemandExecution(true);

            config.setTipInterval(
                Integer.parseInt(parameters.get(EnumParameter.STATIC_TIP_INTERVAL.getValue())));

            config.setDailyBudget(
                EnumDeviceType.METER,
                Integer.parseInt(parameters.get(EnumParameter.METER_DAILY_BUDGET.getValue())));

            config.setWeeklyBudget(
                EnumDeviceType.METER,
                Integer.parseInt(parameters.get(EnumParameter.METER_WEEKLY_BUDGET.getValue())));

            config.setMonthlyBudget(
                EnumDeviceType.METER,
                Integer.parseInt(parameters.get(EnumParameter.METER_MONTHLY_BUDGET.getValue())));

            config.setDailyBudget(
                EnumDeviceType.AMPHIRO,
                Integer.parseInt(parameters.get(EnumParameter.AMPHIRO_DAILY_BUDGET.getValue())));

            config.setWeeklyBudget(
                EnumDeviceType.AMPHIRO,
                Integer.parseInt(parameters.get(EnumParameter.AMPHIRO_WEEKLY_BUDGET.getValue())));

            config.setMonthlyBudget(
                EnumDeviceType.AMPHIRO,
                Integer.parseInt(parameters.get(EnumParameter.AMPHIRO_MONTHLY_BUDGET.getValue())));

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

    /**
     * Enumeration of job parameters.
     */
    public static enum EnumParameter {
        /**
         * Empty parameter.
         */
        EMPTY(null),
        /**
         * Reference date time
         */
        REFERENCE_DATETIME("ref-date"),
        /**
         * Static tip generation interval in days.
         */
        STATIC_TIP_INTERVAL("static.tip.interval"),
        /**
         * Meter daily budget.
         */
        METER_DAILY_BUDGET("daily.budget"),
        /**
         * Meter weekly budget.
         */
        METER_WEEKLY_BUDGET("weekly.budget"),
        /**
         * Meter monthly budget.
         */
        METER_MONTHLY_BUDGET("monthly.budget"),
        /**
         * Amphiro b1 daily budget.
         */
        AMPHIRO_DAILY_BUDGET("daily.budget.amphiro"),
        /**
         * Amphiro b1 weekly budget.
         */
        AMPHIRO_WEEKLY_BUDGET("weekly.budget.amphiro"),
        /**
         * Amphiro b1 monthly budget.
         */
        AMPHIRO_MONTHLY_BUDGET("monthly.budget.amphiro");

        private final String value;

        public String getValue() {
            return value;
        }

        private EnumParameter(String value) {
            this.value = value;
        }

        public static EnumParameter fromString(String value) {
            for (EnumParameter item : EnumParameter.values()) {
                if (item.name().equalsIgnoreCase(value)) {
                    return item;
                }
            }
            return EMPTY;
        }
    }

}
