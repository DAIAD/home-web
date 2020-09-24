package eu.daiad.common.service;

import java.util.UUID;

import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.joda.time.Period;

import eu.daiad.common.model.ComputedNumber;
import eu.daiad.common.model.EnumStatistic;
import eu.daiad.common.model.query.EnumMeasurementField;

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
