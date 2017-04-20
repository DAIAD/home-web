package eu.daiad.web.model.query;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import eu.daiad.web.model.EnumTimeAggregation;
import eu.daiad.web.model.EnumTimeUnit;

public class TimeFilter {

    @JsonDeserialize(using = EnumTemporalFilterType.Deserializer.class)
    private EnumTemporalFilterType type = EnumTemporalFilterType.ABSOLUTE;

    @JsonDeserialize(using = EnumTimeAggregation.Deserializer.class)
    private EnumTimeAggregation granularity = EnumTimeAggregation.DAY;

    private long start;

    private Long end;

    private Integer duration;

    @JsonDeserialize(using = EnumTimeUnit.Deserializer.class)
    private EnumTimeUnit durationTimeUnit = EnumTimeUnit.HOUR;

    public TimeFilter() {
        type = EnumTemporalFilterType.ABSOLUTE;
        granularity = EnumTimeAggregation.HOUR;

        start = (new DateTime()).getMillis();
        end = start;
    }

    public TimeFilter(long start, long end) {
        type = EnumTemporalFilterType.ABSOLUTE;
        granularity = EnumTimeAggregation.HOUR;

        this.start = start;
        this.end = end;
    }

    public TimeFilter(DateTime start, DateTime end) {
        type = EnumTemporalFilterType.ABSOLUTE;
        granularity = EnumTimeAggregation.HOUR;

        this.start = start.getMillis();
        this.end = end.getMillis();
    }

    public TimeFilter(long start, long end, EnumTimeAggregation granularity) {
        type = EnumTemporalFilterType.ABSOLUTE;
        this.granularity = granularity;

        this.start = start;
        this.end = end;
    }

    public TimeFilter(DateTime start, DateTime end, EnumTimeAggregation granularity) {
        type = EnumTemporalFilterType.ABSOLUTE;
        this.granularity = granularity;

        this.start = start.getMillis();
        this.end = end.getMillis();
    }

    public TimeFilter(long start, int duration) {
        type = EnumTemporalFilterType.SLIDING;
        granularity = EnumTimeAggregation.HOUR;

        this.start = start;
        this.duration = duration;
        durationTimeUnit = EnumTimeUnit.HOUR;
    }

    public TimeFilter(long start, int duration, EnumTimeUnit durationTimeUnit) {
        type = EnumTemporalFilterType.SLIDING;
        granularity = EnumTimeAggregation.HOUR;

        this.start = start;
        this.duration = duration;
        this.durationTimeUnit = durationTimeUnit;
    }

    public TimeFilter(long start, int duration, EnumTimeUnit durationTimeUnit, EnumTimeAggregation granularity) {
        type = EnumTemporalFilterType.SLIDING;
        this.granularity = granularity;

        this.start = start;
        this.duration = duration;
        this.durationTimeUnit = durationTimeUnit;
    }

    public TimeFilter(DateTime start, int duration) {
        type = EnumTemporalFilterType.SLIDING;
        granularity = EnumTimeAggregation.HOUR;

        this.start = start.getMillis();
        this.duration = duration;
        durationTimeUnit = EnumTimeUnit.HOUR;
    }

    public TimeFilter(DateTime start, int duration, EnumTimeUnit durationTimeUnit) {
        type = EnumTemporalFilterType.SLIDING;
        granularity = EnumTimeAggregation.HOUR;

        this.start = start.getMillis();
        this.duration = duration;
        this.durationTimeUnit = durationTimeUnit;
    }

    public TimeFilter(DateTime start, int duration, EnumTimeUnit durationTimeUnit, EnumTimeAggregation granularity) {
        type = EnumTemporalFilterType.SLIDING;
        this.granularity = granularity;

        this.start = start.getMillis();
        this.duration = duration;
        this.durationTimeUnit = durationTimeUnit;
    }

    public EnumTemporalFilterType getType() {
        return type;
    }

    public EnumTimeAggregation getGranularity() {
        return granularity;
    }

    public long getStart() {
        return start;
    }

    public Long getEnd() {
        return end;
    }

    public Integer getDuration() {
        return duration;
    }

    public EnumTimeUnit getDurationTimeUnit() {
        return durationTimeUnit;
    }

    public Interval asInterval() {
        if (end == null) {
            DateTime t1 = new DateTime(start);
            DateTime t2 = t1;
            switch (durationTimeUnit) {
                case HOUR:
                    t2 = t2.plusHours(duration);
                    break;
                case DAY:
                    t2 = t2.plusDays(duration);
                    break;
                case WEEK:
                    t2 = t2.plusWeeks(duration);
                    break;
                case MONTH:
                    t2 = t2.plusMonths(duration);
                    break;
                case YEAR:
                    t2 = t2.plusYears(duration);
                    break;
                default:
                    break;
            }
            return new Interval(t1, t2);
        } else
            return new Interval(start, end);
    }

    @Override
    public String toString() {
        return "TimeFilter [type=" + type + ", granularity =" + granularity + ", start=" + start + ", end=" + end
                        + ", duration=" + duration + ", durationTimeUnit=" + durationTimeUnit + "]";
    }

}
