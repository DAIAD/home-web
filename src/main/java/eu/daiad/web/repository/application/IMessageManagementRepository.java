package eu.daiad.web.repository.application;

import java.util.UUID;

import eu.daiad.web.model.message.ConsumptionAggregateContainer;
import eu.daiad.web.model.message.MessageCalculationConfiguration;
import eu.daiad.web.model.message.PendingMessageStatus;

public interface IMessageManagementRepository {

	public void executeAccount(MessageCalculationConfiguration config, ConsumptionAggregateContainer aggregates,
					PendingMessageStatus status, UUID accountkey);

}
