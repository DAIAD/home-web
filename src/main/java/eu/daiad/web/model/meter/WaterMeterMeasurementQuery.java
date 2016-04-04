package eu.daiad.web.model.meter;

import eu.daiad.web.model.TemporalConstants;

public class WaterMeterMeasurementQuery extends WaterMeterStatusQuery {

	private int granularity = TemporalConstants.NONE;

	private long startDate;

	private long endDate;

	public int getGranularity() {
		return granularity;
	}

	public void setGranularity(int granularity) {
		this.granularity = granularity;
	}

	public long getStartDate() {
		return startDate;
	}

	public void setStartDate(long startDate) {
		this.startDate = startDate;
	}

	public long getEndDate() {
		return endDate;
	}

	public void setEndDate(long endDate) {
		this.endDate = endDate;
	}

}
