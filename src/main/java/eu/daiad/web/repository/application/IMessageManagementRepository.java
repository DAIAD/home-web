package eu.daiad.web.repository.application;

import java.util.UUID;

import eu.daiad.web.model.message.MessageCalculationConfiguration;
import eu.daiad.web.model.ConsumptionStats;
import eu.daiad.web.model.message.MessageResolutionPerAccountStatus;

public interface IMessageManagementRepository {

    public void executeAccount(
        MessageCalculationConfiguration config, 
        ConsumptionStats stats, MessageResolutionPerAccountStatus messageStatus, UUID accountkey);

}
