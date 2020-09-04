package eu.daiad.web.service;

import java.util.UUID;

import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.joda.time.Period;

import eu.daiad.web.model.ComputedNumber;
import eu.daiad.web.model.EnumStatistic;
import eu.daiad.web.model.query.EnumMeasurementField;

/**
 * A simple interface for querying utility-wide consumption-related statistics
 */
public interface IConsumptionStatisticsService
{
    ComputedNumber getNumber(
        UUID utilityKey, LocalDateTime refDate, Period period, EnumMeasurementField field, EnumStatistic statistic);

    ComputedNumber getNumber(
        UUID utilityKey, DateTime refDate, Period period, EnumMeasurementField field, EnumStatistic statistic);
}
