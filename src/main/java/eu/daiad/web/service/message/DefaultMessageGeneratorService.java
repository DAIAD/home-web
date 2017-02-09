package eu.daiad.web.service.message;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.apache.commons.collections4.FluentIterable;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.LocalDateTime;
import org.joda.time.Period;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import eu.daiad.web.annotate.message.MessageGenerator;
import eu.daiad.web.domain.application.AccountEntity;
import eu.daiad.web.domain.application.AlertResolverExecutionEntity;
import eu.daiad.web.domain.application.UtilityEntity;
import eu.daiad.web.model.ConsumptionStats;
import eu.daiad.web.model.EnumDayOfWeek;
import eu.daiad.web.model.device.Device;
import eu.daiad.web.model.device.EnumDeviceType;
import eu.daiad.web.model.message.Alert;
import eu.daiad.web.model.message.MessageResolutionPerAccountStatus;
import eu.daiad.web.model.message.MessageResolutionStatus;
import eu.daiad.web.model.utility.UtilityInfo;
import eu.daiad.web.repository.application.IAccountAlertRepository;
import eu.daiad.web.repository.application.IAccountRecommendationRepository;
import eu.daiad.web.repository.application.IAlertResolverExecutionRepository;
import eu.daiad.web.repository.application.IDeviceRepository;
import eu.daiad.web.repository.application.IGroupRepository;
import eu.daiad.web.repository.application.IRecommendationResolverExecutionRepository;
import eu.daiad.web.repository.application.IUserRepository;
import eu.daiad.web.repository.application.IUtilityRepository;
import eu.daiad.web.service.IConsumptionStatsService;

@Service
public class DefaultMessageGeneratorService
    implements IMessageGeneratorService
{
    private static final Log logger = LogFactory.getLog(DefaultMessageGeneratorService.class);

	@Autowired
	ApplicationContext context;
    
	@Autowired 
    @Qualifier("defaultBeanValidator") 
    private Validator validator;
	
    @Autowired
	IUtilityRepository utilityRepository;

    @Autowired
    IDeviceRepository deviceRepository;
    
	@Autowired
	IUserRepository userRepository;

	@Autowired
	IGroupRepository groupRepository;

	@Autowired
	@Qualifier("cachingConsumptionStatsService")
	IConsumptionStatsService statsService;

	@Autowired
    IAccountAlertRepository accountAlertRepository;
	
	@Autowired
	IAlertResolverExecutionRepository alertResolverExecutionRepository;
	
	@Autowired
    IAccountRecommendationRepository accountRecommendationRepository;
	
	@Autowired
    IRecommendationResolverExecutionRepository recommendationResolverExecutionRepository;
	
	/**
	 * Represent a specific target for message generation inside a utility.
	 */
	private class Target
	{
	    private final UtilityInfo utility;
	    
	    private final List<UUID> accountKeys;
	    
	    private final boolean targetUtility;
	    
	    /**
	     * Create a target for the entire utility
	     */
	    private Target(UtilityInfo utility)
	    {
	        this.utility = utility;
	        this.targetUtility = true;
	        this.accountKeys = utilityRepository.getMembers(utility.getId());
	    }
	    
	    /**
         * Create a target for specific accounts into a given utility
         */
	    public Target(UtilityInfo utility, List<UUID> accountKeys)
	    {
	        this.utility = utility;
	        this.targetUtility = false; // targets a subset of this utility
	        this.accountKeys = ListUtils.intersection(
	            accountKeys,
	            utilityRepository.getMembers(utility.getId())
	        );
	    }
	    
	    public List<UUID> getAccounts()
        {
            return accountKeys; 
        }
	    
	    /**
	     * The number of accounts referenced by this target
	     */
	    public int size()
        {
            return accountKeys.size(); 
        }
	    
	    /**
	     * Return a corresponding target entity, if it exists.
	     * 
	     * In the case of a target to a custom set of accounts, {@code null} is returned. 
	     */
	    public UtilityEntity asEntity()
	    {
	        return targetUtility? utilityRepository.findOne(utility.getId()) : null;
	    }
	}
		
	/**
	 * A utility-wide message generator
	 */
	private abstract class Generator 
	    implements IGeneratorContext
	{
	    protected final UtilityInfo utility;
	    
	    /** A reference date for resolvers */
	    protected final DateTime refDate;
	    
	    /** A sliding interval of 1 week ending to refDate */
	    protected final Interval refPastWeek;
	    
	    /** A sliding interval of ~ 1 month ending to refDate */
	    protected final Interval refPastMonth;
	    
	    protected Configuration config = new Configuration();
	    
	    protected Generator(LocalDateTime refDate, UtilityInfo utility)
        {
	        this.utility = utility;
	        
	        // Determine reference date as a (zoned) DateTime
	        
	        DateTimeZone tz = DateTimeZone.forID(utility.getTimezone()); 
	        this.refDate = refDate.toDateTime(tz);
	        
	        // Compute sliding intervals ending to (not including) reference date
	        
	        this.refPastWeek = new Interval(
	            this.refDate.minusWeeks(1).plusMillis(1), this.refDate.minusMillis(1));
	        
	        this.refPastMonth = new Interval(
	            this.refDate.minusMonths(1).plusMillis(1), this.refDate.minusMillis(1));
        }

	    public Generator configure(Configuration config)
	    {
	        this.config = config;
	        return this;
	    }
	    
        @Override
        public DateTime getRefDate()
        {
            return refDate;
        }

        @Override
        public UtilityInfo getUtilityInfo()
        {
            return utility;
        }

        @Override
        public ConsumptionStats getStats()
        {
            return statsService.getStats(utility, refDate.toLocalDateTime());
        }  
        
        protected DeviceExistsPredicate isDevicePresent(EnumDeviceType deviceType)
        {
            return new DeviceExistsPredicate(deviceType, deviceRepository);
        }
        
        public abstract void generate(Target target);
	}
	
	private class AlertGenerator extends Generator
	{
        private IAlertResolverExecutionRepository resolverExecutionRepository;
	    
	    public AlertGenerator(LocalDateTime refDate, UtilityInfo utility)
        {
            super(refDate, utility);
            resolverExecutionRepository = alertResolverExecutionRepository;
        }
        
        public void generate(Target target)
        {
            info("About to generate alerts for %d accounts in utility %s", 
                target.size(), utility.getKey());
            
            Map<String, IAlertResolver> resolvers = context.getBeansOfType(IAlertResolver.class);        
            for (String resolverName: resolvers.keySet()) {
                IAlertResolver resolver = resolvers.get(resolverName);
                resolve(resolverName, resolver, target);
            }
        }
        
        private void resolve(String resolverName, IAlertResolver resolver, Target target)
        {
            MessageGenerator annotation = resolver.getClass().getAnnotation(MessageGenerator.class);
            if (annotation == null || !shouldExecute(resolverName, annotation))
                return; // not enabled as a resolver, or should not execute yet
            
            FluentIterable<UUID> accountKeys = FluentIterable.of(target.getAccounts());
            
            resolver.setup(config, this);
            info("About to resolve alerts with %s (%s)", resolverName, resolver);
            
            DateTime started = DateTime.now();
            AlertResolverExecutionEntity resolverExecutionEntity = 
                resolverExecutionRepository.createWith(refDate, resolverName, target.asEntity(), started);
            
            for (EnumDeviceType deviceType: resolver.getSupportedDevices()) {
                for (UUID accountKey: accountKeys.filter(isDevicePresent(deviceType))) {
                    // Filter by checking per-account limits (throttle)
                    if (hasExceededPerAccountLimits(resolverName, annotation, accountKey)) {
                        info("Skipping resolver %s for account %s as it exceeded limits",
                            resolverName, accountKey);
                        continue;
                    }
                    
                    // Proceed and resolve alerts
                    List<MessageResolutionStatus<Alert.ParameterizedTemplate>> results = null;
                    try {
                        results = resolver.resolve(accountKey, deviceType);
                    } catch (RuntimeException x) {
                        error("Failed to resolve alerts with %s for account %s: %s", 
                            resolverName, accountKey, x);
                        results = null;
                    }
                    if (results == null || results.isEmpty())
                        continue;
                    
                    // Validate and push resolved messages to repository
                    for (MessageResolutionStatus<Alert.ParameterizedTemplate> r: results) {
                        // Check if significant
                        if (!r.isSignificant())
                            continue; 
                        // Validate
                        Alert.ParameterizedTemplate parameterizedTemplate = r.getMessage(); 
                        Set<ConstraintViolation<Alert.ParameterizedTemplate>> violations = 
                            validator.validate(parameterizedTemplate);
                        if (!violations.isEmpty()) {
                            for (ConstraintViolation<Alert.ParameterizedTemplate> c: violations) {
                                info("Failed validation for parameterized template %s: at %s: %s",
                                    parameterizedTemplate,
                                    c.getPropertyPath(), c.getMessage());
                            }
                            continue;
                        }
                        // Passed validation: push to repository
                        accountAlertRepository.createWith(
                            accountKey, parameterizedTemplate, resolverExecutionEntity, deviceType);
                    }
                    
                }
            }
            
            resolver.teardown();
            info("Finished with alerts examined by %s", resolverName);
            
            DateTime finished = DateTime.now();
            resolverExecutionRepository.updateFinished(resolverExecutionEntity, finished);
            
            return;
        }
        
        /**
         * Decide if a resolver has exceeded per-account limits (maxPerWeek, maxPerMonth).
         * 
         * @param resolverName
         * @param resolverAnnotation
         * @param accountKey
         * @return
         */
        private boolean hasExceededPerAccountLimits(
            String resolverName, MessageGenerator resolverAnnotation, UUID accountKey)
        {
            List<Integer> xids;
            
            // Check for a sliding window of 1 week
            
            xids = resolverExecutionRepository.findIdByName(resolverName, refPastWeek);
            if (!xids.isEmpty()) {
                int count = accountAlertRepository.countByAccountAndExecution(accountKey, xids);
                if (count > resolverAnnotation.maxPerWeek())
                    return true;
            }
            
            // Check for a sliding window of 1 month
            
            xids = resolverExecutionRepository.findIdByName(resolverName, refPastMonth);
            if (!xids.isEmpty()) {
                int count = accountAlertRepository.countByAccountAndExecution(accountKey, xids);
                if (count > resolverAnnotation.maxPerMonth())
                    return true;
            }
            
            return false;
        }
        
        /**
         * Decide if a resolver should execute.
         * 
         * @param resolverName
         * @param resolverAnnotation
         * @return
         */
        private boolean shouldExecute(String resolverName, MessageGenerator resolverAnnotation)
        {
            // If a particular day-of-week is specified, check refDate is on that day.
            
            List<EnumDayOfWeek> daysOfWeek = Arrays.asList(resolverAnnotation.dayOfWeek());
            if (!daysOfWeek.isEmpty()) {
                EnumDayOfWeek day = EnumDayOfWeek.valueOf(refDate.getDayOfWeek());
                if (!daysOfWeek.contains(day)) 
                    return false;
            }
            
            // If a particular day-of-month is specified, check refDate is on that day.
               
            List<Integer> daysOfMonth = 
                Arrays.asList(ArrayUtils.toObject(resolverAnnotation.dayOfMonth()));
            if (!daysOfMonth.isEmpty()) {
                Integer day = refDate.getDayOfMonth();
                if (!daysOfMonth.contains(day))
                    return false;
            }
            
            // Otherwise (no day specified or refDate is on a trigger day), check if a successful 
            // execution exists for the interval (refDate - P, refDate]
            
            Period period = Period.parse(resolverAnnotation.period());
            DateTime t0 = refDate.minus(period).plusMillis(1); // just after refDate - P
            
            List<AlertResolverExecutionEntity> executions = 
                resolverExecutionRepository.findByName(resolverName, new Interval(t0, refDate));
            
            return executions.isEmpty();
        }
	    
	}
	
	private static class DeviceExistsPredicate implements Predicate<UUID>
	{
	    private final EnumDeviceType deviceType;
	    
	    private final IDeviceRepository deviceRepository;
	    
	    public DeviceExistsPredicate(EnumDeviceType deviceType, IDeviceRepository deviceRepository)
        {
            this.deviceType = deviceType;
            this.deviceRepository = deviceRepository;
        }
	    
        @Override
	    public boolean evaluate(UUID accountKey)
	    {
            List<Device> devices = deviceRepository.getUserDevices(accountKey, deviceType);
	        return !devices.isEmpty();
	    }
	}
	
	@Override
	public void executeUtility(LocalDateTime refDate, Configuration config, UUID utilityKey)
	{	    
	    UtilityInfo utility = utilityRepository.getUtilityByKey(utilityKey);    
	    generate(refDate, config, utility, new Target(utility));
	}

	@Override
	public void executeAccounts(LocalDateTime refDate, Configuration config, List<UUID> accountKeys)
	{
	    for (UtilityInfo utility: utilityRepository.getUtilities()) {
	        generate(refDate, config, utility, new Target(utility, accountKeys));
	    }
	}

	private void generate(LocalDateTime refDate, Configuration config, UtilityInfo utility, Target target)
	{
	    Generator generator = null;
	    
	    // Todo Generate tips
	   
	    // Generate alerts
	    
	    generator = new AlertGenerator(refDate, utility)
	        .configure(config);
	    generator.generate(target);
	   
	    // Todo Generate recommendations
	}
	
	private static void error(String f, Object ...args)
	{
	    logger.error(String.format(f, args));
	}
	
	private static void warn(String f, Object ...args)
    {
        logger.warn(String.format(f, args));
    }
	
	private static void info(String f, Object ...args)
    {
        logger.info(String.format(f, args));
    }
}
