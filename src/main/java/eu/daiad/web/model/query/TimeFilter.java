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
	private EnumTimeAggregation granularity  = EnumTimeAggregation.ALL;

	private long start;

	private Long end;

	private Integer duration;

	@JsonDeserialize(using = EnumTimeUnit.Deserializer.class)
	private EnumTimeUnit durationTimeUnit = EnumTimeUnit.HOUR;

	public TimeFilter() {
		this.type = EnumTemporalFilterType.ABSOLUTE;
		this.granularity  = EnumTimeAggregation.HOUR;

		this.start = (new DateTime()).getMillis();
		this.end = this.start;
	}

	public TimeFilter(long start, long end) {
		this.type = EnumTemporalFilterType.ABSOLUTE;
		this.granularity  = EnumTimeAggregation.HOUR;

		this.start = start;
		this.end = end;
	}

	public TimeFilter(DateTime start, DateTime end) {
		this.type = EnumTemporalFilterType.ABSOLUTE;
		this.granularity  = EnumTimeAggregation.HOUR;

		this.start = start.getMillis();
		this.end = end.getMillis();
	}

	public TimeFilter(long start, long end, EnumTimeAggregation granularity ) {
		this.type = EnumTemporalFilterType.ABSOLUTE;
		this.granularity  = granularity ;

		this.start = start;
		this.end = end;
	}

	public TimeFilter(DateTime start, DateTime end, EnumTimeAggregation granularity ) {
		this.type = EnumTemporalFilterType.ABSOLUTE;
		this.granularity  = granularity ;

		this.start = start.getMillis();
		this.end = end.getMillis();
	}

	public TimeFilter(long start, int duration) {
		this.type = EnumTemporalFilterType.SLIDING;
		this.granularity  = EnumTimeAggregation.HOUR;

		this.start = start;
		this.duration = duration;
		this.durationTimeUnit = EnumTimeUnit.HOUR;
	}

	public TimeFilter(long start, int duration, EnumTimeUnit durationTimeUnit) {
		this.type = EnumTemporalFilterType.SLIDING;
		this.granularity  = EnumTimeAggregation.HOUR;

		this.start = start;
		this.duration = duration;
		this.durationTimeUnit = durationTimeUnit;
	}

	public TimeFilter(long start, int duration, EnumTimeUnit durationTimeUnit, EnumTimeAggregation granularity) {
		this.type = EnumTemporalFilterType.SLIDING;
		this.granularity  = granularity;

		this.start = start;
		this.duration = duration;
		this.durationTimeUnit = durationTimeUnit;
	}

	public TimeFilter(DateTime start, int duration) {
		this.type = EnumTemporalFilterType.SLIDING;
		this.granularity  = EnumTimeAggregation.HOUR;

		this.start = start.getMillis();
		this.duration = duration;
		this.durationTimeUnit = EnumTimeUnit.HOUR;
	}

	public TimeFilter(DateTime start, int duration, EnumTimeUnit durationTimeUnit) {
		this.type = EnumTemporalFilterType.SLIDING;
		this.granularity  = EnumTimeAggregation.HOUR;

		this.start = start.getMillis();
		this.duration = duration;
		this.durationTimeUnit = durationTimeUnit;
	}

	public TimeFilter(DateTime start, int duration, EnumTimeUnit durationTimeUnit, EnumTimeAggregation granularity) {
		this.type = EnumTemporalFilterType.SLIDING;
		this.granularity  = granularity;

		this.start = start.getMillis();
		this.duration = duration;
		this.durationTimeUnit = durationTimeUnit;
	}

	public EnumTemporalFilterType getType() {
		return type;
	}

	public EnumTimeAggregation getGranularity () {
		return granularity ;
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

	public Interval asInterval()
	{
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
		return
		    "TimeFilter [type=" + type + ", granularity =" + granularity  + ", start=" + start + ", end=" + end
		        + ", duration=" + duration + ", durationTimeUnit=" + durationTimeUnit + "]";
	}

}
