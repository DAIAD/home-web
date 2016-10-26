package eu.daiad.web.repository.application;

import java.util.UUID;

import eu.daiad.web.model.message.ConsumptionStats;
import eu.daiad.web.model.message.MessageCalculationConfiguration;
import eu.daiad.web.model.message.CandidateMessageStatus;
import eu.daiad.web.model.security.AuthenticatedUser;
import org.joda.time.DateTime;

public interface IMessageManagementRepository {

	public void executeAccount(
	        MessageCalculationConfiguration config, 
	        ConsumptionStats aggregates,
			CandidateMessageStatus messageStatus, 
			UUID accountkey);

	public DateTime getLastDateOfAccountStaticRecommendation(AuthenticatedUser user);
}
