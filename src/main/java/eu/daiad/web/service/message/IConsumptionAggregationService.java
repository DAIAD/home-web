package eu.daiad.web.service.message;

import eu.daiad.web.model.message.ConsumptionAggregateContainer;
import eu.daiad.web.model.message.MessageCalculationConfiguration;

public interface IConsumptionAggregationService {

	/**
	 * Computes a set of aggregate values about the average user water consumption for a utility. This information
	 * is used for generating messages for a single user.
	 * @param config the message generation job configuration options.
	 * @return the aggregate values about the average user water consumption of a utility
	 */
	public abstract ConsumptionAggregateContainer execute(MessageCalculationConfiguration config);

}
