package eu.daiad.common.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.summary.Sum;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.LocalDateTime;
import org.joda.time.Period;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.daiad.common.model.ComputedNumber;
import eu.daiad.common.model.EnumStatistic;
import eu.daiad.common.model.EnumTimeAggregation;
import eu.daiad.common.model.device.EnumDeviceType;
import eu.daiad.common.model.query.DataQuery;
import eu.daiad.common.model.query.DataQueryBuilder;
import eu.daiad.common.model.query.DataQueryResponse;
import eu.daiad.common.model.query.EnumDataField;
import eu.daiad.common.model.query.EnumMeasurementDataSource;
import eu.daiad.common.model.query.EnumMeasurementField;
import eu.daiad.common.model.query.EnumMetric;
import eu.daiad.common.model.query.Point;
import eu.daiad.common.model.query.SeriesFacade;
import eu.daiad.common.model.utility.UtilityInfo;
import eu.daiad.common.repository.application.IGroupRepository;
import eu.daiad.common.repository.application.IUtilityRepository;

@Service
@ConfigurationProperties(prefix = "daiad.services.consumption-aggregation")
public class DefaultConsumptionAggregationService implements IConsumptionAggregationService
{

	@Autowired
	IGroupRepository groupRepository;

	@Autowired
    IUtilityRepository utilityRepository;

	@Autowired
	IDataService dataService;

	private static abstract class Aggregator
	{
	    protected final EnumMeasurementField measurementField;

        protected Aggregator(EnumMeasurementField field)
        {
            this.measurementField = field;
        }

        protected DataQueryBuilder newQueryBuilder(UtilityInfo utility, DateTime refDate, Period period)
        {
            DataQueryBuilder querybuilder = new DataQueryBuilder();

            // Specify the source of measurements

            EnumDeviceType deviceType = measurementField.getDeviceType();

            querybuilder.source(EnumMeasurementDataSource.fromDeviceType(deviceType));

            // Supply a proper query interval based on (refDate, period).
            // The time-unit is inferred from how period is expressed.

            DateTime end = null;
            EnumTimeAggregation u = null;
            if (period.getDays() > 0) {
                u = EnumTimeAggregation.DAY;
                end = refDate.withTimeAtStartOfDay();
            } else if (period.getWeeks() > 0) {
                u = EnumTimeAggregation.WEEK;
                end = refDate.withDayOfWeek(DateTimeConstants.MONDAY)
                    .withTimeAtStartOfDay();
            } else if (period.getMonths() > 0) {
                u = EnumTimeAggregation.MONTH;
                end = refDate.withDayOfMonth(1)
                    .withTimeAtStartOfDay();
            } else if (period.getYears() > 0) {
                u = EnumTimeAggregation.YEAR;
                end = refDate.withDayOfYear(1)
                    .withTimeAtStartOfDay();
            } else {
                // Cannot understand period. A granularity of at least DAY should be used!
                Assert.isTrue(false, "Unable to determine time-unit from period " + period);
            }

            DateTime start = end.minus(period);
            DateTimeZone tz = DateTimeZone.forID(utility.getTimezone());

            querybuilder
                .timezone(tz)
                .absolute(start, end, u);

            return querybuilder;
        }

	    public abstract ComputedNumber compute(UtilityInfo utility, DateTime refDate, Period period);
	}

	private class SummingAggregator extends Aggregator
	{

        public SummingAggregator(EnumMeasurementField measurementField, EnumStatistic statistic)
        {
            super(measurementField);

            Assert.isTrue(statistic == EnumStatistic.SUM, "[Assertion failed] - Statistic type must be SUM");

            // Check that summing is meaningful for this (field, statistic)
            EnumDataField field = measurementField.getField();
            Assert.isTrue(
                field == EnumDataField.VOLUME ||
                field == EnumDataField.DURATION ||
                field == EnumDataField.ENERGY, 
                "[Assertion failed] - Data field type not supported");
        }

        @Override
        public ComputedNumber compute(UtilityInfo utility, DateTime refDate, Period period)
        {
            EnumDeviceType deviceType = measurementField.getDeviceType();
            EnumDataField field = measurementField.getField();

            DataQueryBuilder querybuilder = newQueryBuilder(utility, refDate, period)
                .utility("utility", utility.getKey())
                .sum();

            DataQuery query = querybuilder.build();
            DataQueryResponse queryResponse = dataService.execute(query);
            SeriesFacade series = queryResponse.getFacade(deviceType);
            if (series == null || series.isEmpty())
                return ComputedNumber.UNDEFINED;

            Interval interval = query.getTime().asInterval();
            double value = series.aggregate(
                field, EnumMetric.SUM, Point.betweenTime(interval), new Sum());

            return ComputedNumber.valueOf(value);
        }

	}

	private class AveragingPerUserAggregator extends Aggregator
	{
        public AveragingPerUserAggregator(EnumMeasurementField measurementField, EnumStatistic statistic)
        {
            super(measurementField);

            Assert.isTrue(statistic == EnumStatistic.AVERAGE_PER_USER, "[Assertion failed] - Statistic type must be AVERAGE_PER_USER");

            // Check that averaging-per-user is meaningful for this (field, statistic)
            EnumDataField field = measurementField.getField();
            Assert.isTrue(
                field == EnumDataField.VOLUME ||
                field == EnumDataField.DURATION ||
                field == EnumDataField.ENERGY, 
                "[Assertion failed] - Data field type not supported");
        }

        @Override
        public ComputedNumber compute(UtilityInfo utility, DateTime refDate, Period period)
        {
            EnumDeviceType deviceType = measurementField.getDeviceType();
            EnumDataField field = measurementField.getField();

            DataQueryBuilder querybuilder = newQueryBuilder(utility, refDate, period)
                .utility("utility", utility.getKey())
                .sum();

            DataQuery query = querybuilder.build();
            DataQueryResponse queryResponse = dataService.execute(query);
            SeriesFacade series = queryResponse.getFacade(deviceType);
            if (series == null || series.isEmpty())
                return ComputedNumber.UNDEFINED;

            Interval interval = query.getTime().asInterval();
            double targetValue = series.aggregate(
                field, EnumMetric.SUM, Point.betweenTime(interval), new Sum());
            double averageValue = targetValue / series.getPopulationCount();

            return ComputedNumber.valueOf(averageValue);
        }
	}

	private class AveragingPerSessionAggregator extends Aggregator
	{
	    public AveragingPerSessionAggregator(EnumMeasurementField measurementField, EnumStatistic statistic)
        {
            super(measurementField);

            Assert.isTrue(statistic == EnumStatistic.AVERAGE_PER_SESSION, "[Assertion failed] - Statistic type must be AVERAGE_PER_SESSION");

            // Check that averaging-per-session is meaningful for this (field, statistic)
            EnumDeviceType deviceType = measurementField.getDeviceType();
            Assert.isTrue(deviceType == EnumDeviceType.AMPHIRO, "[Assertion failed] - Device type must be AMPHIRO");
        }

        @Override
        public ComputedNumber compute(UtilityInfo utility, DateTime refDate, Period period)
        {
            EnumDeviceType deviceType = measurementField.getDeviceType();
            EnumDataField field = measurementField.getField();

            DataQueryBuilder querybuilder = newQueryBuilder(utility, refDate, period)
                .utility("utility", utility.getKey())
                .sum()
                .average();

            DataQuery query = querybuilder.build();
            DataQueryResponse queryResponse = dataService.execute(query);
            SeriesFacade series = queryResponse.getFacade(deviceType);
            if (series == null || series.isEmpty())
                return ComputedNumber.UNDEFINED;

            double averageValue = Double.NaN;
            Interval interval = query.getTime().asInterval();

            if (field.supports(EnumMetric.SUM)) {
                double totalValue = series.aggregate(
                    field, EnumMetric.SUM, Point.betweenTime(interval), new Sum());
                double numberOfSessions = series.aggregate(
                    field, EnumMetric.COUNT, Point.betweenTime(interval), new Sum());
                averageValue = totalValue / numberOfSessions;
            } else {
                averageValue = series.aggregate(
                    field, EnumMetric.AVERAGE, Point.betweenTime(interval), new Mean());
            }

            return ComputedNumber.valueOf(averageValue);
        }
	}

	private class PercentileOfUsersAggregator extends Aggregator
	{
	    private final double ratio;

	    public PercentileOfUsersAggregator(EnumMeasurementField measurementField, EnumStatistic statistic)
        {
            super(measurementField);

            // Check statistic and map it to a ratio

            switch (statistic) {
            case PERCENTILE_10P_OF_USERS:
                ratio = 0.10;
                break;
            case PERCENTILE_25P_OF_USERS:
                ratio = 0.25;
                break;
            case PERCENTILE_50P_OF_USERS:
                ratio = 0.50;
                break;
            case PERCENTILE_75P_OF_USERS:
                ratio = 0.75;
                break;
            case PERCENTILE_90P_OF_USERS:
                ratio = 0.90;
                break;
            default:
                ratio = Double.NaN;
                Assert.isTrue(false,
                    "This aggregator cannot handle statistic " + statistic);
            }

            // Check if meaningful for this (field, statistic)

            EnumDataField field = measurementField.getField();
            Assert.isTrue(
                field == EnumDataField.VOLUME ||
                field == EnumDataField.DURATION ||
                field == EnumDataField.ENERGY, 
                "[Assertion failed] - Data field type is not supported");
        }

	    private List<UUID> getAccountKeys(UtilityInfo utility)
	    {
	        return groupRepository.getUtilityByIdMemberKeys(utility.getId());
	    }

        @Override
        public ComputedNumber compute(UtilityInfo utility, DateTime refDate, Period period)
        {
            EnumDeviceType deviceType = measurementField.getDeviceType();
            EnumDataField field = measurementField.getField();

            DataQuery query;
            DataQueryResponse queryResponse;
            SeriesFacade series;

            DataQueryBuilder querybuilder = newQueryBuilder(utility, refDate, period)
                .sum()
                .average();

            // Collect values (e.g. consumption) for each user on given interval

            List<UUID> accountKeys = getAccountKeys(utility);
            List<Double> values = new ArrayList<>(accountKeys.size());
            for (UUID accountKey: accountKeys) {
                querybuilder.removePopulationFilter();
                querybuilder.user("user", accountKey);

                query = querybuilder.build();
                queryResponse = dataService.execute(query);
                series = queryResponse.getFacade(deviceType);
                if (series == null || series.isEmpty())
                    continue; // no consumption; skip

                Interval interval = query.getTime().asInterval();
                double value = series.aggregate(
                    field, EnumMetric.SUM, Point.betweenTime(interval), new Sum());
                values.add(value);
            }

            if (values.isEmpty())
                return ComputedNumber.UNDEFINED;

            // Find percentile on values (select k-th based on ratio)

            Collections.sort(values);
            int k = Double.valueOf(ratio * values.size()).intValue();
            double percentileValue = values.get(k);

            return ComputedNumber.valueOf(percentileValue);
        }
	}

	private UtilityInfo resolveUtility(UUID utilityKey)
	{
	    UtilityInfo utility = utilityRepository.getUtilityByKey(utilityKey);
	    Assert.state(utility != null, "No such utility: " + utilityKey);
	    return utility;
	}

	private Aggregator newAggregator(EnumMeasurementField measurementField, EnumStatistic statistic)
	{
	    // Decide which aggregator is to be used for (measurementField, statistic)

	    EnumDeviceType deviceType = measurementField.getDeviceType();
	    EnumDataField field = measurementField.getField();

	    Aggregator aggregator = null;

	    switch (statistic) {
	    case AVERAGE_PER_USER:
	        {
	            switch (field) {
	            case VOLUME:
	            case DURATION:
	                aggregator = new AveragingPerUserAggregator(measurementField, statistic);
	                break;
	            case TEMPERATURE:
	                aggregator = null; // not supported (only AVERAGE_PER_SESSION)
	                break;
	            case FLOW:
	                aggregator = null; // not supported (only AVERAGE_PER_SESSION)
	                break;
	            case ENERGY:
	                aggregator = null; // not supported (yet)
	                break;
	            default:
	                break;
	            }
	        }
	        break;
	    case AVERAGE_PER_SESSION:
	        {
	            if (deviceType == EnumDeviceType.AMPHIRO)
	                aggregator = new AveragingPerSessionAggregator(measurementField, statistic);
	            else
	                aggregator = null; // not supported for any other type of device
	        }
	        break;
	    case SUM:
	        {
	            switch (field) {
                case VOLUME:
                case DURATION:
                    aggregator = new SummingAggregator(measurementField, statistic);
                    break;
                case TEMPERATURE:
                    aggregator = null; // not supported
                    break;
                case FLOW:
                    aggregator = null; // not supported
                    break;
                case ENERGY:
                    aggregator = null; // not supported (yet)
                    break;
                default:
                    break;
                }
	        }
	        break;
	    case PERCENTILE_10P_OF_USERS:
	    case PERCENTILE_25P_OF_USERS:
	    case PERCENTILE_50P_OF_USERS:
	    case PERCENTILE_75P_OF_USERS:
	    case PERCENTILE_90P_OF_USERS:
	        {
	            switch (field) {
                case VOLUME:
                case DURATION:
                    aggregator = new PercentileOfUsersAggregator(measurementField, statistic);
                    break;
                case TEMPERATURE:
                    aggregator = null; // not supported
                    break;
                case FLOW:
                    aggregator = null; // not supported
                    break;
                case ENERGY:
                    aggregator = null; // not supported (yet)
                    break;
                default:
                    break;
                }
	        }
	        break;
	    default:
	        break;
	    }

	    return aggregator;
	}

	private ComputedNumber compute(
	    UtilityInfo utility, DateTime refDate, Period period, EnumMeasurementField field, EnumStatistic statistic)
	{
	    Aggregator aggregator = newAggregator(field, statistic);
	    if (aggregator == null)
	        throw new UnsupportedOperationException(
	            "Cannot find a suitable aggregator for (" + field + "," + statistic + ")");
	    return aggregator.compute(utility, refDate, period);
	}

    @Override
    public ComputedNumber compute(
        UUID utilityKey, LocalDateTime refDate, Period period, EnumMeasurementField field, EnumStatistic statistic)
    {
        UtilityInfo utility = resolveUtility(utilityKey);
        DateTimeZone tz = DateTimeZone.forID(utility.getTimezone());
        return compute(utility, refDate.toDateTime(tz), period, field, statistic);
    }

    @Override
    public ComputedNumber compute(
        UUID utilityKey, DateTime refDate, Period period, EnumMeasurementField field, EnumStatistic statistic)
    {
        UtilityInfo utility = resolveUtility(utilityKey);
        return compute(utility, refDate, period, field, statistic);
    }
}
