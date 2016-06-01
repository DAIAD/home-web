package eu.daiad.web.service.message;

import java.util.UUID;

import eu.daiad.web.model.message.MessageCalculationConfiguration;

public interface IMessageService {

	/**
	 * Generates messages i.e. alerts, recommendations and tips for all users of all utilities based on
	 * a set of configuration options.
	 *  
	 * @param config the configuration options
	 */
	public abstract void executeAll(MessageCalculationConfiguration config);

	/**
	 * Generates messages i.e. alerts, recommendations and tips for all users of a utility based on the utility
	 * key and a set of configuration options.
	 *  
	 * @param config the configuration options
	 */
	public abstract void executeUtility(MessageCalculationConfiguration config, UUID utilityKey);

	/**
	 * Generates messages i.e. alerts, recommendations and tips for a single user based on its key, its utility 
	 * key and a set of configuration options.
	 *  
	 * @param config the configuration options
	 */
	public abstract void executeAccount(MessageCalculationConfiguration config, UUID utilityKey, UUID accountKey);

}
