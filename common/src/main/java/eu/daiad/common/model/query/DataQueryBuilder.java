package eu.daiad.common.model.query;

import java.util.ArrayList;
import java.util.UUID;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import eu.daiad.common.model.EnumTimeAggregation;
import eu.daiad.common.model.EnumTimeUnit;

public class DataQueryBuilder {

    private DataQuery query;

    ArrayList<EnumMetric> metrics = new ArrayList<EnumMetric>();

    public static DataQueryBuilder create() {
        return new DataQueryBuilder();
    }

    public static DataQueryBuilder create(DataQuery query) {
        return new DataQueryBuilder(query);
    }

    public DataQueryBuilder() {
        query = new DataQuery();
        query.setTime(new TimeFilter());
    }

    public DataQueryBuilder(DataQuery query) {
        if (query == null) {
            this.query = new DataQuery();
            this.query.setTime(new TimeFilter());
        } else {
            this.query = query;
        }
    }

    public DataQueryBuilder reset() {
        query = new DataQuery();
        query.setTime(new TimeFilter());
        metrics = new ArrayList<EnumMetric>();
        return this;
    }

    public DataQueryBuilder removePopulationFilter() {
        query.getPopulation().clear();
        return this;
    }

    public DataQueryBuilder user(String label, UUID user) {
        query.getPopulation().add(new UserPopulationFilter(label, user));
        return this;
    }

    public DataQueryBuilder users(String label, UUID[] users) {
        query.getPopulation().add(new UserPopulationFilter(label, users));
        return this;
    }

    public DataQueryBuilder usersBottom(String label, UUID[] users, EnumMetric metric, int limit) {
        query.getPopulation().add(new UserPopulationFilter(label, users, EnumRankingType.BOTTOM, metric, limit));
        return this;
    }

    public DataQueryBuilder usersTop(String label, UUID[] users, EnumMetric metric, int limit) {
        query.getPopulation().add(new UserPopulationFilter(label, users, EnumRankingType.TOP, metric, limit));
        return this;
    }

    public DataQueryBuilder group(String label, UUID group) {
        query.getPopulation().add(new GroupPopulationFilter(label, group));
        return this;
    }

    public DataQueryBuilder groupBottom(String label, UUID group, EnumMetric metric, int limit) {
        query.getPopulation().add(new GroupPopulationFilter(label, group, EnumRankingType.BOTTOM, metric, limit));
        return this;
    }

    public DataQueryBuilder groupTop(String label, UUID group, EnumMetric metric, int limit) {
        query.getPopulation().add(new GroupPopulationFilter(label, group, EnumRankingType.TOP, metric, limit));
        return this;
    }

    public DataQueryBuilder utility(String label, UUID utility) {
        query.getPopulation().add(new UtilityPopulationFilter(label, utility));
        return this;
    }

    public DataQueryBuilder utilityBottom(String label, UUID utility, EnumMetric metric, int limit) {
        query.getPopulation().add(
                        new UtilityPopulationFilter(label, utility, EnumRankingType.BOTTOM, metric, limit));
        return this;
    }

    public DataQueryBuilder utilityTop(String label, UUID utility, EnumMetric metric, int limit) {
        query.getPopulation().add(new UtilityPopulationFilter(label, utility, EnumRankingType.TOP, metric, limit));
        return this;
    }

    public DataQueryBuilder absolute(long start, long end) {
        query.setTime(new TimeFilter(start, end));

        return this;
    }

    public DataQueryBuilder absolute(long start, long end, EnumTimeAggregation granularity) {
        query.setTime(new TimeFilter(start, end, granularity));

        return this;
    }

    public DataQueryBuilder absolute(DateTime start, DateTime end) {
        query.setTime(new TimeFilter(start, end));

        return this;
    }

    public DataQueryBuilder absolute(DateTime start, DateTime end, EnumTimeAggregation granularity) {
        query.setTime(new TimeFilter(start, end, granularity));

        return this;
    }

    public DataQueryBuilder sliding(long start, int duration) {
        query.setTime(new TimeFilter(start, duration));

        return this;
    }

    public DataQueryBuilder sliding(long start, int duration, EnumTimeUnit durationTimeUnit) {
        query.setTime(new TimeFilter(start, duration, durationTimeUnit));

        return this;
    }

    public DataQueryBuilder sliding(long start, int duration, EnumTimeUnit durationTimeUnit,
                    EnumTimeAggregation granularity) {
        query.setTime(new TimeFilter(start, duration, durationTimeUnit, granularity));
        return this;
    }

    public DataQueryBuilder sliding(DateTime start, int duration) {
        query.setTime(new TimeFilter(start, duration));

        return this;
    }

    public DataQueryBuilder sliding(int duration) {
        query.setTime(new TimeFilter(new DateTime().getMillis(), duration));

        return this;
    }

    public DataQueryBuilder sliding(DateTime start, int duration, EnumTimeUnit durationTimeUnit) {
        query.setTime(new TimeFilter(start, duration, durationTimeUnit));

        return this;
    }

    public DataQueryBuilder sliding(int duration, EnumTimeUnit durationTimeUnit) {
        query.setTime(new TimeFilter(new DateTime().getMillis(), duration, durationTimeUnit));

        return this;
    }

    public DataQueryBuilder sliding(DateTime start, int duration, EnumTimeUnit durationTimeUnit,
                    EnumTimeAggregation granularity) {
        query.setTime(new TimeFilter(start, duration, durationTimeUnit, granularity));

        return this;
    }

    public DataQueryBuilder sliding(int duration, EnumTimeUnit durationTimeUnit, EnumTimeAggregation granularity) {
        query.setTime(new TimeFilter(new DateTime().getMillis(), duration, durationTimeUnit, granularity));

        return this;
    }

    public DataQueryBuilder all() {
        query.setSource(EnumMeasurementDataSource.BOTH);
        return this;
    }

    public DataQueryBuilder amphiro() {
        query.setSource(EnumMeasurementDataSource.AMPHIRO);
        return this;
    }

    public DataQueryBuilder meter() {
        query.setSource(EnumMeasurementDataSource.METER);
        return this;
    }

    public DataQueryBuilder source(EnumMeasurementDataSource s) {
        query.setSource(s);
        return this;
    }

    public DataQueryBuilder sum() {
        if (!metrics.contains(EnumMetric.SUM)) {
            metrics.add(EnumMetric.SUM);
        }
        return this;
    }

    public DataQueryBuilder min() {
        if (!metrics.contains(EnumMetric.MIN)) {
            metrics.add(EnumMetric.MIN);
        }
        return this;
    }

    public DataQueryBuilder max() {
        if (!metrics.contains(EnumMetric.MAX)) {
            metrics.add(EnumMetric.MAX);
        }
        return this;
    }

    public DataQueryBuilder count() {
        if (!metrics.contains(EnumMetric.COUNT)) {
            metrics.add(EnumMetric.COUNT);
        }
        return this;
    }

    public DataQueryBuilder average() {
        if (!metrics.contains(EnumMetric.AVERAGE)) {
            metrics.add(EnumMetric.AVERAGE);
        }
        return this;
    }

    public DataQueryBuilder timezone(String timezone) {
        query.setTimezone(timezone);

        return this;
    }

    public DataQueryBuilder timezone(DateTimeZone timezone) {
        query.setTimezone(timezone.toString());

        return this;
    }

    public DataQueryBuilder userAggregates() {
        query.setUsingPreAggregation(true);

        return this;
    }

    public DataQuery build() {
        if (metrics.contains(EnumMetric.AVERAGE)) {
            count().sum();
        }
        if (metrics.contains(EnumMetric.MIN)) {
            max();
        }
        if (metrics.contains(EnumMetric.MAX)) {
            min();
        }

        for (PopulationFilter filter : query.getPopulation()) {
            if ((filter.getRanking() != null) && (!metrics.contains(filter.getRanking().getMetric()))) {
                metrics.add(filter.getRanking().getMetric());
            }
        }

        EnumMetric[] metricArray = new EnumMetric[metrics.size()];

        query.setMetrics(metrics.toArray(metricArray));

        return query;
    }
}
