package eu.daiad.web.job.task;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.collections4.FluentIterable;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.LocalDateTime;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.core.step.tasklet.StoppableTasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.daiad.web.job.builder.MessageGeneratorJobBuilder;
import eu.daiad.web.model.EnumTimeUnit;
import eu.daiad.web.model.device.EnumDeviceType;
import eu.daiad.web.model.utility.UtilityInfo;
import eu.daiad.web.repository.application.IUtilityRepository;
import eu.daiad.web.service.message.IMessageGeneratorService;

import static eu.daiad.web.model.device.EnumDeviceType.METER;
import static eu.daiad.web.model.device.EnumDeviceType.AMPHIRO;


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
     * Repository for accessing utility data.
     */
    @Autowired
    private IUtilityRepository utilityRepository;

    /**
     * Service for creating application messages.
     */
    @Autowired
    private IMessageGeneratorService messageGeneratorService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        try {
            Map<EnumParameter, String> parameters = getParameters(chunkContext.getStepContext());
            
            LocalDateTime refDate = parameters.containsKey(EnumParameter.REFERENCE_DATETIME)?
                LocalDateTime.parse(parameters.get(EnumParameter.REFERENCE_DATETIME)) :
                LocalDateTime.now().minusDays(1);
           
            eu.daiad.web.service.message.Configuration config = 
                    new eu.daiad.web.service.message.Configuration();
            config.setOnDemandExecution(true);

            String tipInterval = parameters.get(EnumParameter.STATIC_TIP_INTERVAL);
            if (!StringUtils.isBlank(tipInterval))
                config.setTipPeriod(Integer.parseInt(tipInterval));

            String budget = null;
            
            budget = parameters.get(EnumParameter.METER_DAILY_BUDGET);
            if (!StringUtils.isBlank(budget))
                config.setBudget(METER, EnumTimeUnit.DAY, Integer.parseInt(budget));
            
            budget = parameters.get(EnumParameter.METER_WEEKLY_BUDGET);
            if (!StringUtils.isBlank(budget))
                config.setBudget(METER, EnumTimeUnit.WEEK, Integer.parseInt(budget));
            
            budget = parameters.get(EnumParameter.METER_MONTHLY_BUDGET);
            if (!StringUtils.isBlank(budget))
                config.setBudget(METER, EnumTimeUnit.MONTH, Integer.parseInt(budget));
            
            budget = parameters.get(EnumParameter.AMPHIRO_DAILY_BUDGET);
            if (!StringUtils.isBlank(budget))
                config.setBudget(AMPHIRO, EnumTimeUnit.DAY, Integer.parseInt(budget));
            
            budget = parameters.get(EnumParameter.AMPHIRO_WEEKLY_BUDGET);
            if (!StringUtils.isBlank(budget))
                config.setBudget(AMPHIRO, EnumTimeUnit.WEEK, Integer.parseInt(budget));
            
            budget = parameters.get(EnumParameter.AMPHIRO_MONTHLY_BUDGET);
            if (!StringUtils.isBlank(budget))
                config.setBudget(AMPHIRO, EnumTimeUnit.MONTH, Integer.parseInt(budget));
            
            // Generate messages
            
            String accountKeys = parameters.get(EnumParameter.RUN_FOR_ACCOUNT);
            String utilityKeys = parameters.get(EnumParameter.RUN_FOR_UTILITY);

            if (!StringUtils.isBlank(accountKeys)) {
                List<UUID> keys = new ArrayList<>();
                for(String k: StringUtils.split(accountKeys, ","))
                    keys.add(UUID.fromString(k.trim()));
                messageGeneratorService.executeAccounts(refDate, config, keys);
            }

            if (StringUtils.isBlank(utilityKeys)) {
                utilityKeys = "";
                List<UtilityInfo> utilities = utilityRepository.getUtilities();
                for (int i = 0, count = utilities.size(); i < count; i++) {
                    if(utilities.get(i).isMessageGenerationEnabled()) {
                        utilityKeys += ((utilityKeys.length() == 0 ? "" : ",") + utilities.get(i).getKey().toString());
                    }
                }
            }
            
            if (!StringUtils.isBlank(utilityKeys) && !utilityKeys.equals("-")) {
                for(String key : StringUtils.split(utilityKeys, ",")) {
                    messageGeneratorService.executeUtility(refDate, config, UUID.fromString(key));
                }
            }
        } catch (Exception ex) {
            logger.fatal("Failed to complete message calculation process.", ex);
            throw ex;
        }
        return RepeatStatus.FINISHED;
    }

    private Map<EnumParameter, String> getParameters(StepContext stepContext)
    {
        Map<EnumParameter, String> parameters = new EnumMap<>(EnumParameter.class);
        for (Map.Entry<String, String> e: super.getStepParameters(stepContext).entrySet()) {
            EnumParameter k = EnumParameter.fromString(e.getKey());
            if (k != null)
                parameters.put(k, e.getValue());
        }
        return parameters;
    }
    
    @Override
    public void stop() {
        // TODO: Add business logic for stopping processing
    }

    /**
     * Enumeration of step parameters.
     */
    public static enum EnumParameter {
        /**
         * Reference date time
         */
        REFERENCE_DATETIME("reference.datetime"),
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
        AMPHIRO_MONTHLY_BUDGET("monthly.budget.amphiro"),
        /**
         * A comma-separated list of utility keys. Generates messages for a certain utilities only.
         */
        RUN_FOR_UTILITY("execute.for.utility"),
        /**
         * A comma-separated list of account keys. Generates messages for a certain accounts only.
         */
        RUN_FOR_ACCOUNT("execute.for.account");

        private final String value;

        public String getValue() {
            return value;
        }

        private EnumParameter(String value) {
            this.value = value;
        }

        public static EnumParameter fromString(String value) {
            for (EnumParameter p: EnumParameter.values()) {
                if (p.value.equalsIgnoreCase(value)) {
                    return p;
                }
            }
            return null;
        }
    }

}
