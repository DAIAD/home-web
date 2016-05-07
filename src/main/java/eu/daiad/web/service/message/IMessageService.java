package eu.daiad.web.service.message;

import java.util.UUID;

import eu.daiad.web.model.message.MessageCalculationConfiguration;

public interface IMessageService {

	public abstract void executeAll(MessageCalculationConfiguration config);

	public abstract void executeUtility(MessageCalculationConfiguration config, UUID utilityKey);

	public abstract void executeAccount(MessageCalculationConfiguration config, UUID utilityKey, UUID accountKey);

}
