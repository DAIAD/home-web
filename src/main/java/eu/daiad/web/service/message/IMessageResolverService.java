package eu.daiad.web.service.message;

import java.util.UUID;

import eu.daiad.web.model.message.ConsumptionAggregateContainer;
import eu.daiad.web.model.message.MessageCalculationConfiguration;
import eu.daiad.web.model.message.PendingMessageStatus;

public interface IMessageResolverService {

	public abstract PendingMessageStatus resolve(MessageCalculationConfiguration config,
					ConsumptionAggregateContainer aggregates, UUID accountKey);

}
