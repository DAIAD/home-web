package eu.daiad.web.model;

import org.joda.time.DateTime;

public class WaterMeterMeasurementQuery extends WaterMeterStatusQuery {

	private int granularity = TemporalConstants.NONE;

	private DateTime startDate;

	private DateTime endDate;

	public int getGranularity() {
		return granularity;
	}

	public void setGranularity(int granularity) {
		this.granularity = granularity;
	}

	public DateTime getStartDate() {
		return startDate;
	}

	public void setStartDate(DateTime startDate) {
		this.startDate = startDate;
	}

	public DateTime getEndDate() {
		return endDate;
	}

	public void setEndDate(DateTime endDate) {
		this.endDate = endDate;
	}

}
