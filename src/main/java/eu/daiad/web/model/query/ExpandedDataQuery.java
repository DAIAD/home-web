package eu.daiad.web.model.query;

import java.util.ArrayList;

public class ExpandedDataQuery {

	private long startDateTime;

	private long endDateTime;

	private EnumTimeAggregation granularity = EnumTimeAggregation.ALL;

	private ArrayList<ExpandedPopulationFilter> groups = new ArrayList<ExpandedPopulationFilter>();

	private EnumMetric metrics[];

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

}
