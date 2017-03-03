package eu.daiad.web.service;

import java.util.UUID;

import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.joda.time.Period;

import eu.daiad.web.model.ComputedNumber;
import eu.daiad.web.model.EnumStatistic;
import eu.daiad.web.model.query.EnumMeasurementField;
import eu.daiad.web.model.utility.UtilityInfo;

public interface IConsumptionAggregationService 
{
	public ComputedNumber compute(
	    UUID utilityKey, LocalDateTime refDate, Period period, EnumMeasurementField field, EnumStatistic statistic);
	
	public ComputedNumber compute(
        UUID utilityKey, DateTime refDate, Period period, EnumMeasurementField field, EnumStatistic statistic);
	
}
