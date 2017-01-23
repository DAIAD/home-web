package eu.daiad.web.service.message;

import java.util.UUID;

import eu.daiad.web.model.ConsumptionStats;
import eu.daiad.web.model.message.MessageResolutionPerAccountStatus;
import eu.daiad.web.service.message.IMessageGeneratorService.Configuration;

public interface IMessageManagementService
{
    public void executeAccount(
        Configuration config, ConsumptionStats stats, MessageResolutionPerAccountStatus messageStatus, UUID accountkey);

}
