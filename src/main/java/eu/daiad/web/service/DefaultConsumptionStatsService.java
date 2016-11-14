package eu.daiad.web.service;

import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.daiad.web.model.ConsumptionStats;
import eu.daiad.web.model.utility.UtilityInfo;
import eu.daiad.web.repository.application.IConsumptionStatsRepository;

@Service
public class DefaultConsumptionStatsService implements IConsumptionStatsService
{
    @Autowired
    IConsumptionAggregationService aggregationService;
    
    @Override
    public ConsumptionStats getStats(UtilityInfo utility, LocalDateTime refDate)
    {
        return aggregationService.compute(utility, refDate);
    }
}
