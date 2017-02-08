package eu.daiad.web.service.message;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import eu.daiad.web.annotate.message.GenerateMessages;
import eu.daiad.web.domain.application.AccountEntity;
import eu.daiad.web.model.ConsumptionStats;
import eu.daiad.web.model.device.Device;
import eu.daiad.web.model.device.EnumDeviceType;
import eu.daiad.web.model.message.Alert;
import eu.daiad.web.model.message.MessageResolutionPerAccountStatus;
import eu.daiad.web.model.message.MessageResolutionStatus;
import eu.daiad.web.model.utility.UtilityInfo;
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

    private static final Set<EnumDeviceType> deviceTypes = 
        EnumSet.of(EnumDeviceType.METER, EnumDeviceType.AMPHIRO);
    
	@Autowired
	ApplicationContext context;
    
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
    IMessageManagementService managerService;
	
	@Autowired
	IMessageResolverService resolverService;

	@Autowired
	IAlertResolverExecutionRepository alertResolverExecutionRepository;
	
	@Autowired
    IRecommendationResolverExecutionRepository recommendationResolverExecutionRepository;
    	
	/**
	 * Represent a specific target for message generation inside a utility.
	 */
	private static class Target
	{
	    private final List<UUID> accountKeys;
	    
	    private Target()
	    {
	        this.accountKeys = null;
	    }
	    
	    private Target(List<UUID> accountKeys)
	    {
	        this.accountKeys = new ArrayList<>(accountKeys);
	    }
	    
	    public List<UUID> getAccountKeys()
        {
            return accountKeys;
        }

        public static Target fromAccounts(List<UUID> keys)
	    {
	        return new Target(keys);
	    }
	    
	    public static final Target ALL = new Target();
	}
		
	/**
	 * A utility-wide message generator
	 */
	private class Generator 
	    implements IGeneratorContext
	{
	    private final UtilityInfo utility;
	    
	    private final DateTime refDate;
	    
	    private Configuration config = new Configuration();
	    
	    public Generator(LocalDateTime refDate, UtilityInfo utility)
        {
	        this.utility = utility;
	        
	        DateTimeZone tz = DateTimeZone.forID(utility.getTimezone()); 
	        this.refDate = refDate.toDateTime(tz);
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
        
        public void generate(Target target)
        {
            List<UUID> accountKeys = target.getAccountKeys();
            if (accountKeys == null) {
                // Consider the entire utility as target
                accountKeys = utilityRepository.getUtilityMembers(utility.getKey());
            }
        
            info("About to generate messages for %d accounts in utility %s",
                accountKeys.size(), utility.getKey());
            
            //
            // Alerts
            // 
            
            Map<String, IAlertResolver> resolvers = context.getBeansOfType(IAlertResolver.class);
            
            List<MessageResolutionStatus<Alert.ParameterizedTemplate>> results = new ArrayList<>();
            for (String resolverName: resolvers.keySet()) {
                IAlertResolver resolver = resolvers.get(resolverName);
                info("About to resolve alerts with %s (%s)", resolverName, resolver);
                results.addAll(resolveAlerts(resolver, resolverName, accountKeys));
            }
            
            
        }
        
        private boolean isDevicePresent(EnumDeviceType deviceType, UUID accountKey)
        {
            List<Device> d = deviceRepository.getUserDevices(accountKey, deviceType);
            return !d.isEmpty();
        }
        
        private List<MessageResolutionStatus<Alert.ParameterizedTemplate>> resolveAlerts(
            IAlertResolver resolver, String resolverName, List<UUID> accountKeys)
        {
            GenerateMessages annotation = resolver.getClass().getAnnotation(GenerateMessages.class);
            if (annotation == null)
                return Collections.emptyList();
            
            resolver.setup(config, this);
            
            List<MessageResolutionStatus<Alert.ParameterizedTemplate>> results = new ArrayList<>();
            
            for (EnumDeviceType deviceType: deviceTypes) {
                if (!resolver.supports(deviceType))
                    continue;
                for (UUID accountKey: accountKeys) {
                    if (!isDevicePresent(deviceType, accountKey))
                        continue;
                    List<MessageResolutionStatus<Alert.ParameterizedTemplate>> r = null;
                    try {
                        r = resolver.resolve(accountKey, deviceType);
                    } catch (RuntimeException x) {
                        error("Failed to resolve alerts with '%s' for account %s: %s", 
                            resolverName, accountKey, x.getMessage());
                        r = null;
                    }
                    if (r != null && !r.isEmpty())
                        results.addAll(r);
                }
            }
            
            resolver.teardown();
            
            return null;
        }
        
	}
	
	@Override
	public void executeUtility(LocalDateTime refDate, Configuration config, UUID utilityKey)
	{	    
	    UtilityInfo utility = utilityRepository.getUtilityByKey(utilityKey);
	    
	    Generator generator = new Generator(refDate, utility)
	        .configure(config);
	    
	    generator.generate(Target.ALL);
	}

	@Override
	public void executeAccounts(LocalDateTime refDate, Configuration config, List<UUID> accountKeys)
	{
	    for (UtilityInfo utility: utilityRepository.getUtilities()) {
	        UUID utilityKey = utility.getKey();
	        
	        List<UUID> keys = ListUtils.intersection(
	            accountKeys,
	            utilityRepository.getUtilityMembers(utilityKey));
	        if (keys.isEmpty())
	            continue;
	        
	        Generator generator = new Generator(refDate, utility)
	            .configure(config);
	        
	        generator.generate(Target.fromAccounts(keys));
	    }
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
