package eu.daiad.web.repository.application;

import java.util.UUID;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import eu.daiad.web.model.ConsumptionStats;

/**
 * A repository interface for consumption-related day-based statistics.
 * 
 * All reference dates have a maximum granularity of 1 day
 */
public interface IConsumptionStatsRepository
{    
    public ConsumptionStats get(UUID utilityKey, UUID groupKey, LocalDateTime refDate);
    
    public void save(UUID utilityKey, UUID groupKey, LocalDateTime refDate, ConsumptionStats stats);    
}
