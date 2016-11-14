package eu.daiad.web.service;

import org.joda.time.LocalDateTime;

import eu.daiad.web.model.ConsumptionStats;
import eu.daiad.web.model.utility.UtilityInfo;

public interface IConsumptionStatsService
{
    ConsumptionStats getStats(UtilityInfo utility, LocalDateTime refDate);
}
