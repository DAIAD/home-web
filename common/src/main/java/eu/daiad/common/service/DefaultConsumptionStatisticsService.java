package eu.daiad.common.service;

import java.util.UUID;

import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.joda.time.Period;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.daiad.common.model.ComputedNumber;
import eu.daiad.common.model.EnumStatistic;
import eu.daiad.common.model.query.EnumMeasurementField;

@Service
public class DefaultConsumptionStatisticsService
    implements IConsumptionStatisticsService
{
    @Autowired
    IConsumptionAggregationService aggregationService;

    @Override
    public ComputedNumber getNumber(UUID utilityKey, LocalDateTime refDate, Period period,
        EnumMeasurementField field, EnumStatistic statistic)
    {
        return aggregationService.compute(utilityKey, refDate, period, field, statistic);
    }

    @Override
    public ComputedNumber getNumber(UUID utilityKey, DateTime refDate, Period period,
        EnumMeasurementField field, EnumStatistic statistic)
    {
        return aggregationService.compute(utilityKey, refDate, period, field, statistic);
    }

}
