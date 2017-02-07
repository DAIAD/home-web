package eu.daiad.web.service.message;

import java.util.UUID;

import eu.daiad.web.model.ConsumptionStats;
import eu.daiad.web.model.message.MessageResolutionPerAccountStatus;
import eu.daiad.web.model.utility.UtilityInfo;

public interface IMessageResolverService {

	/**
	 * Decides which messages i.e. alerts, recommendations and tips will be generated for a single user.
	 *
	 * @param config The message generation job configuration options
	 * @param utility
	 * @param stats A set of aggregates values used for comparing user consumption against the average
	 * user consumption of the users of the utility he belongs to.
	 * @param accountKey The user key.
	 * @return information about which messages should be created.
	 */
	public MessageResolutionPerAccountStatus resolve(
	    Configuration config, UtilityInfo utilityInfo, ConsumptionStats stats, UUID accountKey);

}
