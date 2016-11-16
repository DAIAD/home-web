package eu.daiad.web.repository.application;

import java.util.UUID;

import eu.daiad.web.model.message.MessageCalculationConfiguration;
import eu.daiad.web.model.ConsumptionStats;
import eu.daiad.web.model.message.MessageResolutionStatus;
import eu.daiad.web.model.security.AuthenticatedUser;
import org.joda.time.DateTime;

public interface IMessageManagementRepository {

	public void executeAccount(
	        MessageCalculationConfiguration config, 
	        ConsumptionStats aggregates,
			MessageResolutionStatus messageStatus, 
			UUID accountkey);

	public DateTime getLastDateOfAccountStaticRecommendation(AuthenticatedUser user);
}
