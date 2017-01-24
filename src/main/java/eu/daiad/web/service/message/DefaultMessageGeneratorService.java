package eu.daiad.web.service.message;

import java.util.List;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import eu.daiad.web.domain.application.AccountEntity;
import eu.daiad.web.model.ConsumptionStats;
import eu.daiad.web.model.message.MessageResolutionPerAccountStatus;
import eu.daiad.web.model.utility.UtilityInfo;
import eu.daiad.web.repository.application.IGroupRepository;
import eu.daiad.web.repository.application.IUserRepository;
import eu.daiad.web.repository.application.IUtilityRepository;
import eu.daiad.web.service.IConsumptionStatsService;

@Service
public class DefaultMessageGeneratorService
    implements IMessageGeneratorService
{
    private static final Log logger = LogFactory.getLog(DefaultMessageGeneratorService.class);

	@Autowired
	IUtilityRepository utilityRepository;

	@Autowired
	IUserRepository userRepository;

	@Autowired
	IGroupRepository groupRepository;

	@Autowired
	IMessageManagementService messageManager;

	@Autowired
	@Qualifier("cachingConsumptionStatsService")
	IConsumptionStatsService statsService;

	@Autowired
	IMessageResolverService messageResolver;

	@Override
	public void executeAll(Configuration config)
	{
	    List<UtilityInfo> utilities = utilityRepository.getUtilities();
	    for (UtilityInfo utility: utilities) {
			logger.info("About to generate messages for utility " + utility.getName() + "...");
	        executeUtility(config, utility.getKey());
		}
	}

	@Override
	public void executeUtility(Configuration config, UUID utilityKey)
	{
		UtilityInfo utility = utilityRepository.getUtilityByKey(utilityKey);
		ConsumptionStats stats = statsService.getStats(utility, config.getRefDate());
        executeUtility(config, utility, stats);
	}

	private void executeUtility(Configuration config, UtilityInfo utility, ConsumptionStats stats)
	{
		List<UUID> accountKeys = groupRepository.getUtilityByIdMemberKeys(utility.getId());
        for (UUID accountKey: accountKeys) {
		    logger.info("Generate messages for account " + accountKey + " at utility #" + utility.getId());
		    executeAccount(config, utility, stats, accountKey);
		}
	}

	@Override
	public void executeAccount(Configuration config, UUID accountKey)
	{
	    AccountEntity accountEntity = userRepository.getAccountByKey(accountKey);
	    UUID utilityKey = accountEntity.getUtility().getKey();

	    UtilityInfo utilityInfo = utilityRepository.getUtilityByKey(utilityKey);
		ConsumptionStats stats = statsService.getStats(utilityInfo, config.getRefDate());
		executeAccount(config, utilityInfo, stats, accountKey);
	}

	private void executeAccount(
	    Configuration config, UtilityInfo utility, ConsumptionStats stats, UUID accountKey)
	{
		MessageResolutionPerAccountStatus messageStatus = messageResolver.resolve(config, utility, stats, accountKey);
		messageManager.executeAccount(config, stats, messageStatus, accountKey);
	}

}
