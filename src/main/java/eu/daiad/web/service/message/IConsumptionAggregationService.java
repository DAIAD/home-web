package eu.daiad.web.service.message;

import eu.daiad.web.model.message.ConsumptionAggregateContainer;
import eu.daiad.web.model.message.MessageCalculationConfiguration;

public interface IConsumptionAggregationService {

	public abstract ConsumptionAggregateContainer execute(MessageCalculationConfiguration config);

}
