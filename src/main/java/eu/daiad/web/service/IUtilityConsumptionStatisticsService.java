package eu.daiad.web.service;

import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.joda.time.Period;

import eu.daiad.web.model.ComputedNumber;
import eu.daiad.web.model.EnumStatistic;
import eu.daiad.web.model.query.EnumMeasurementField;

/**
 * This is an interface similar to {@link IConsumptionStatisticsService} but a bit simpler, 
 * since target utility is fixed (not part of query).
 */
public interface IUtilityConsumptionStatisticsService
{
    ComputedNumber getNumber(
        LocalDateTime refDate, Period period, EnumMeasurementField field, EnumStatistic statistic);
    
    ComputedNumber getNumber(
        DateTime refDate, Period period, EnumMeasurementField field, EnumStatistic statistic);
}
