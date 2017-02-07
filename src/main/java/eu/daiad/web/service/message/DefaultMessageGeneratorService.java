package eu.daiad.web.service.message;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import eu.daiad.web.repository.application.IDeviceRepository;
import eu.daiad.web.repository.application.IGroupRepository;
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

	@Override
	public void executeUtility(Configuration config, UUID utilityKey)
	{
	    LocalDateTime refDate = config.getRefDate();
	    
	    UtilityInfo info = utilityRepository.getUtilityByKey(utilityKey);
		ConsumptionStats stats = statsService.getStats(info, refDate);
		List<UUID> accountKeys = utilityRepository.getUtilityMembers(utilityKey);
		executeAccounts(config, info, stats, accountKeys);
	}

	@Override
	public void executeAccounts(Configuration config, List<UUID> accountKeys)
	{
	    LocalDateTime refDate = config.getRefDate();    
	    for (UtilityInfo utilityInfo: utilityRepository.getUtilities()) {
	        UUID utilityKey = utilityInfo.getKey();
	        List<UUID> keys = ListUtils.intersection(
	            accountKeys,
	            utilityRepository.getUtilityMembers(utilityKey));
	        if (keys.isEmpty())
	            continue;
	        ConsumptionStats stats = statsService.getStats(utilityInfo, refDate);
	        executeAccounts(config, utilityInfo, stats, keys);    
	    }
	}

	private void executeAccounts(
	    Configuration config, UtilityInfo utilityInfo, ConsumptionStats utilityStats, List<UUID> accountKeys)
	{
		info("About to generate messages for %d accounts in utility %s",
		    accountKeys.size(), utilityInfo.getKey());
	    
		
		
	    //
		// Alerts
		// 
		
		Map<String, IAlertResolver> resolvers = context.getBeansOfType(IAlertResolver.class);
		
		List<MessageResolutionStatus<Alert.ParameterizedTemplate>> results = new ArrayList<>();
		for (String resolverName: resolvers.keySet()) {
		    IAlertResolver resolver = resolvers.get(resolverName);
		    info("About to resolve alerts with %s (%s)", resolverName, resolver);
		    
		    GenerateMessages annotation = resolver.getClass().getAnnotation(GenerateMessages.class);
		    if (annotation == null)
		        continue; 
		    
		    resolver.setup(config, utilityInfo, utilityStats);
		    
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
		}
	}
	
	private boolean isDevicePresent(EnumDeviceType deviceType, UUID accountKey)
	{
	    List<Device> d = deviceRepository.getUserDevices(accountKey, deviceType);
	    return !d.isEmpty();
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
		
	// Fixme: Remove obsolete code
	private void executeAccounts1(
        Configuration config, UtilityInfo utilityInfo, ConsumptionStats utilityStats, List<UUID> accountKeys)
    {
        logger.info(String.format("About to generate messages for %d accounts in utility %s",
            accountKeys.size(), utilityInfo.getKey()));
        for (UUID key: accountKeys) {
            MessageResolutionPerAccountStatus messageStatus =
                resolverService.resolve(config, utilityInfo, utilityStats, key);
            managerService.executeAccount(config, utilityStats, messageStatus, key);
        }
           
    }

}
