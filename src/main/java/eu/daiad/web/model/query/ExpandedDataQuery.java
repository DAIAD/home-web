package eu.daiad.web.model.query;

import java.util.ArrayList;

import eu.daiad.web.service.DataQueryUserCollection;

public class ExpandedDataQuery {

	private long startDateTime;

	private long endDateTime;

	private EnumTimeUnit granularity = EnumTimeUnit.HOUR;

	private ArrayList<DataQueryUserCollection> groups = new ArrayList<DataQueryUserCollection>();

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

	public EnumTimeUnit getGranularity() {
		return granularity;
	}

	public void setGranularity(EnumTimeUnit granularity) {
		this.granularity = granularity;
	}

	public EnumMetric[] getMetrics() {
		return metrics;
	}

	public void setMetrics(EnumMetric[] metrics) {
		this.metrics = metrics;
	}

	public ArrayList<DataQueryUserCollection> getGroups() {
		return groups;
	}

}
