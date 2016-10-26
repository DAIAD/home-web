package eu.daiad.web.service.message;

import java.util.List;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.daiad.web.model.message.ConsumptionStats;
import eu.daiad.web.model.message.MessageCalculationConfiguration;
import eu.daiad.web.model.message.PendingMessageStatus;
import eu.daiad.web.model.utility.UtilityInfo;
import eu.daiad.web.repository.application.IGroupRepository;
import eu.daiad.web.repository.application.IMessageManagementRepository;
import eu.daiad.web.repository.application.IUserRepository;
import eu.daiad.web.repository.application.IUtilityRepository;

@Service
public class DefaultMessageService implements IMessageService {

    private static final Log logger = LogFactory.getLog(DefaultMessageService.class);
    
	@Autowired
	IUtilityRepository utilityRepository;

	@Autowired
	IUserRepository userRepository;

	@Autowired
	IGroupRepository groupRepository;

	@Autowired
	IMessageManagementRepository messageManagementRepository;

	@Autowired
	IConsumptionAggregationService aggregationService;

	@Autowired
	IMessageResolverService messageResolverService;

	@Override
	public void executeAll(MessageCalculationConfiguration config) 
	{
	    for (UtilityInfo utility : utilityRepository.getUtilities()) {
			logger.info("About to generate messages for utility " + utility.getName() + "...");
	        executeUtility(config, utility.getKey());
		}
	}

	@Override
	public void executeUtility(MessageCalculationConfiguration config, UUID utilityKey) 
	{
		UtilityInfo utility = this.utilityRepository.getUtilityByKey(utilityKey);
		ConsumptionStats aggregates = aggregationService.compute(utility);
		executeUtility(config, utility, aggregates);
	}

	private void executeUtility(
	        MessageCalculationConfiguration config, UtilityInfo utility, ConsumptionStats stats) 
	{
		List<UUID> accountKeys = groupRepository.getUtilityByIdMemberKeys(utility.getId()); 
	    for (UUID accountKey: accountKeys) {
			logger.info("[DRY-RUN] About to generate messages for account " +
			        accountKey + " at utility #" + utility.getId());
	        // Fixme executeAccount(config, utility, stats, accountKey);
		}
	}

	@Override
	public void executeAccount(MessageCalculationConfiguration config, UUID utilityKey, UUID accountKey) 
	{
		UtilityInfo utility = utilityRepository.getUtilityByKey(utilityKey);
		ConsumptionStats stats = aggregationService.compute(utility);
		executeAccount(config, utility, stats, accountKey);
	}

	private void executeAccount(
	        MessageCalculationConfiguration config, UtilityInfo utility, ConsumptionStats stats, UUID accountKey) 
	{
		PendingMessageStatus status = messageResolverService.resolve(config, utility, stats, accountKey);
		messageManagementRepository.executeAccount(config, stats, status, accountKey);
	}

}
