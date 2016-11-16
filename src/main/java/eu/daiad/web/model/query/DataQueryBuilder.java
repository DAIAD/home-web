package eu.daiad.web.model.query;

import java.util.ArrayList;
import java.util.UUID;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

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
        this.query = new DataQuery();
        this.query.setTime(new TimeFilter());
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
        this.query = new DataQuery();
        this.query.setTime(new TimeFilter());
        this.metrics = new ArrayList<EnumMetric>();
        return this;
    }

    public DataQueryBuilder removePopulationFilter() {
        this.query.getPopulation().clear();
        return this;
    }

    public DataQueryBuilder user(String label, UUID user) {
        this.query.getPopulation().add(new UserPopulationFilter(label, user));
        return this;
    }

    public DataQueryBuilder users(String label, UUID[] users) {
        this.query.getPopulation().add(new UserPopulationFilter(label, users));
        return this;
    }

    public DataQueryBuilder usersBottom(String label, UUID[] users, EnumMetric metric, int limit) {
        this.query.getPopulation().add(new UserPopulationFilter(label, users, EnumRankingType.BOTTOM, metric, limit));
        return this;
    }

    public DataQueryBuilder usersTop(String label, UUID[] users, EnumMetric metric, int limit) {
        this.query.getPopulation().add(new UserPopulationFilter(label, users, EnumRankingType.TOP, metric, limit));
        return this;
    }

    public DataQueryBuilder group(String label, UUID group) {
        this.query.getPopulation().add(new GroupPopulationFilter(label, group));
        return this;
    }

    public DataQueryBuilder groupBottom(String label, UUID group, EnumMetric metric, int limit) {
        this.query.getPopulation().add(new GroupPopulationFilter(label, group, EnumRankingType.BOTTOM, metric, limit));
        return this;
    }

    public DataQueryBuilder groupTop(String label, UUID group, EnumMetric metric, int limit) {
        this.query.getPopulation().add(new GroupPopulationFilter(label, group, EnumRankingType.TOP, metric, limit));
        return this;
    }

    public DataQueryBuilder utility(String label, UUID utility) {
        this.query.getPopulation().add(new UtilityPopulationFilter(label, utility));
        return this;
    }

    public DataQueryBuilder utilityBottom(String label, UUID utility, EnumMetric metric, int limit) {
        this.query.getPopulation().add(
                        new UtilityPopulationFilter(label, utility, EnumRankingType.BOTTOM, metric, limit));
        return this;
    }

    public DataQueryBuilder utilityTop(String label, UUID utility, EnumMetric metric, int limit) {
        this.query.getPopulation().add(new UtilityPopulationFilter(label, utility, EnumRankingType.TOP, metric, limit));
        return this;
    }

    public DataQueryBuilder absolute(long start, long end) {
        this.query.setTime(new TimeFilter(start, end));

        return this;
    }

    public DataQueryBuilder absolute(long start, long end, EnumTimeAggregation granularity) {
        this.query.setTime(new TimeFilter(start, end, granularity));

        return this;
    }

    public DataQueryBuilder absolute(DateTime start, DateTime end) {
        this.query.setTime(new TimeFilter(start, end));

        return this;
    }

    public DataQueryBuilder absolute(DateTime start, DateTime end, EnumTimeAggregation granularity) {
        this.query.setTime(new TimeFilter(start, end, granularity));

        return this;
    }

    public DataQueryBuilder sliding(long start, int duration) {
        this.query.setTime(new TimeFilter(start, duration));

        return this;
    }

    public DataQueryBuilder sliding(long start, int duration, EnumTimeUnit durationTimeUnit) {
        this.query.setTime(new TimeFilter(start, duration, durationTimeUnit));

        return this;
    }

    public DataQueryBuilder sliding(long start, int duration, EnumTimeUnit durationTimeUnit,
                    EnumTimeAggregation granularity) {
        this.query.setTime(new TimeFilter(start, duration, durationTimeUnit, granularity));
        return this;
    }

    public DataQueryBuilder sliding(DateTime start, int duration) {
        this.query.setTime(new TimeFilter(start, duration));

        return this;
    }

    public DataQueryBuilder sliding(int duration) {
        this.query.setTime(new TimeFilter(new DateTime().getMillis(), duration));

        return this;
    }

    public DataQueryBuilder sliding(DateTime start, int duration, EnumTimeUnit durationTimeUnit) {
        this.query.setTime(new TimeFilter(start, duration, durationTimeUnit));

        return this;
    }

    public DataQueryBuilder sliding(int duration, EnumTimeUnit durationTimeUnit) {
        this.query.setTime(new TimeFilter(new DateTime().getMillis(), duration, durationTimeUnit));

        return this;
    }

    public DataQueryBuilder sliding(DateTime start, int duration, EnumTimeUnit durationTimeUnit,
                    EnumTimeAggregation granularity) {
        this.query.setTime(new TimeFilter(start, duration, durationTimeUnit, granularity));

        return this;
    }

    public DataQueryBuilder sliding(int duration, EnumTimeUnit durationTimeUnit, EnumTimeAggregation granularity) {
        this.query.setTime(new TimeFilter(new DateTime().getMillis(), duration, durationTimeUnit, granularity));

        return this;
    }

    public DataQueryBuilder all() {
        this.query.setSource(EnumMeasurementDataSource.BOTH);
        return this;
    }

    public DataQueryBuilder amphiro() {
        this.query.setSource(EnumMeasurementDataSource.AMPHIRO);
        return this;
    }

    public DataQueryBuilder meter() {
        this.query.setSource(EnumMeasurementDataSource.METER);
        return this;
    }
    
    public DataQueryBuilder source(EnumMeasurementDataSource s) {
        this.query.setSource(s);
        return this;
    }
    
    public DataQueryBuilder sum() {
        if (!this.metrics.contains(EnumMetric.SUM)) {
            this.metrics.add(EnumMetric.SUM);
        }
        return this;
    }

    public DataQueryBuilder min() {
        if (!this.metrics.contains(EnumMetric.MIN)) {
            this.metrics.add(EnumMetric.MIN);
        }
        return this;
    }

    public DataQueryBuilder max() {
        if (!this.metrics.contains(EnumMetric.MAX)) {
            this.metrics.add(EnumMetric.MAX);
        }
        return this;
    }

    public DataQueryBuilder count() {
        if (!this.metrics.contains(EnumMetric.COUNT)) {
            this.metrics.add(EnumMetric.COUNT);
        }
        return this;
    }

    public DataQueryBuilder average() {
        if (!this.metrics.contains(EnumMetric.AVERAGE)) {
            this.metrics.add(EnumMetric.AVERAGE);
        }
        return this;
    }

    public DataQueryBuilder timezone(String timezone) {
        this.query.setTimezone(timezone);

        return this;
    }

    public DataQueryBuilder timezone(DateTimeZone timezone) {
        this.query.setTimezone(timezone.toString());

        return this;
    }

    public DataQuery build() {
        if (this.metrics.contains(EnumMetric.AVERAGE)) {
            this.count().sum();
        }
        if (this.metrics.contains(EnumMetric.MIN)) {
            this.max();
        }
        if (this.metrics.contains(EnumMetric.MAX)) {
            this.min();
        }

        for (PopulationFilter filter : this.query.getPopulation()) {
            if ((filter.getRanking() != null) && (!this.metrics.contains(filter.getRanking().getMetric()))) {
                this.metrics.add(filter.getRanking().getMetric());
            }
        }

        EnumMetric[] metricArray = new EnumMetric[this.metrics.size()];

        this.query.setMetrics(this.metrics.toArray(metricArray));

        return this.query;
    }
}
