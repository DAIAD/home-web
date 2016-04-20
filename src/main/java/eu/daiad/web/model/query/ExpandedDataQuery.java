package eu.daiad.web.model.query;

import java.util.ArrayList;

import org.joda.time.DateTimeZone;

public class ExpandedDataQuery {

	private DateTimeZone timezone;

	private long startDateTime;

	private long endDateTime;

	private EnumTimeAggregation granularity = EnumTimeAggregation.ALL;

	private ArrayList<ExpandedPopulationFilter> groups = new ArrayList<ExpandedPopulationFilter>();

	private EnumMetric metrics[];

	public ExpandedDataQuery(DateTimeZone timezone) {
		this.timezone = timezone;
	}

	public long getStartDateTime() {
		return startDateTime;
	}

	public void setStartDateTime(long startDateTime) {
		this.startDateTime = startDateTime;
	}

	public long getEndDateTime() {
		return endDateTime;
	}

	public void setEndDateTime(long endDateTime) {
		this.endDateTime = endDateTime;
	}

	public EnumTimeAggregation getGranularity() {
		return granularity;
	}

	public void setGranularity(EnumTimeAggregation granularity) {
		this.granularity = granularity;
	}

	public EnumMetric[] getMetrics() {
		return metrics;
	}

	public void setMetrics(EnumMetric[] metrics) {
		this.metrics = metrics;
	}

	public ArrayList<ExpandedPopulationFilter> getGroups() {
		return groups;
	}

	public DateTimeZone getTimezone() {
		return timezone;
	}

}
