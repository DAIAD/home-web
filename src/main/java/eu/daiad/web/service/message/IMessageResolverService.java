package eu.daiad.web.service.message;

import java.util.UUID;

import eu.daiad.web.model.message.ConsumptionAggregateContainer;
import eu.daiad.web.model.message.MessageCalculationConfiguration;
import eu.daiad.web.model.message.PendingMessageStatus;

public interface IMessageResolverService {

	/**
	 * Decides which messages i.e. alerts, recommendations and tips will be generated for a single user.
	 * 
	 * @param config the message generation job configuration options
	 * @param aggregates a set of aggregates values used for comparing user consumption against the average
	 * user consumption of the users of the utility he belongs to.
	 * @param accountKey the user key.
	 * @return information about which messages should be created.
	 */
	public abstract PendingMessageStatus resolve(MessageCalculationConfiguration config,
					ConsumptionAggregateContainer aggregates, UUID accountKey);

}
