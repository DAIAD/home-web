package eu.daiad.web.service.message;

import java.util.UUID;

import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.daiad.web.model.message.ConsumptionAggregateContainer;
import eu.daiad.web.model.message.MessageCalculationConfiguration;
import eu.daiad.web.model.message.PendingMessageStatus;
import eu.daiad.web.model.utility.UtilityInfo;
import eu.daiad.web.repository.application.IMessageManagementRepository;
import eu.daiad.web.repository.application.IUserRepository;
import eu.daiad.web.repository.application.IUtilityRepository;

@Service
public class DefaultMessageService implements IMessageService {

	@Autowired
	IUtilityRepository utilityRepository;

	@Autowired
	IUserRepository userRepository;

	@Autowired
	IMessageManagementRepository messageManagementRepository;

	@Autowired
	IConsumptionAggregationService aggregationService;

	@Autowired
	IMessageResolverService messageResolverService;

	@Override
	public void executeAll(MessageCalculationConfiguration config) {
		for (UtilityInfo utility : utilityRepository.getUtilities()) {
			executeUtility(config, utility.getKey());
		}
	}

	@Override
	public void executeUtility(MessageCalculationConfiguration config, UUID utilityKey) {
		UtilityInfo utility = this.utilityRepository.getUtilityByKey(utilityKey);

		config.setUtilityId(utility.getId());
		config.setTimezone(DateTimeZone.forID(utility.getTimezone()));

		ConsumptionAggregateContainer aggregates = aggregationService.execute(config);

		executeUtility(config, aggregates);
	}

	private void executeUtility(MessageCalculationConfiguration config, ConsumptionAggregateContainer aggregates) {
		for (UUID accountKey : userRepository.getUserKeysForUtility(config.getUtilityId())) {
			executeAccount(config, aggregates, accountKey);
		}
	}

	@Override
	public void executeAccount(MessageCalculationConfiguration config, UUID utilityKey, UUID accountKey) {
		UtilityInfo utility = this.utilityRepository.getUtilityByKey(utilityKey);

		config.setUtilityId(utility.getId());
		config.setTimezone(DateTimeZone.forID(utility.getTimezone()));
		
		ConsumptionAggregateContainer aggregates = aggregationService.execute(config);

		this.executeAccount(config, aggregates, accountKey);
	}

	private void executeAccount(MessageCalculationConfiguration config, ConsumptionAggregateContainer aggregates,
					UUID accountKey) {
		PendingMessageStatus status = this.messageResolverService.resolve(config, aggregates, accountKey);

		this.messageManagementRepository.executeAccount(config, aggregates, status, accountKey);
	}

}
