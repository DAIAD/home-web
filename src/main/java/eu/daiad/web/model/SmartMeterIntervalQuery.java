package eu.daiad.web.model;

import org.joda.time.DateTime;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

public class SmartMeterIntervalQuery extends SmartMeterQuery {

	@DateTimeFormat(iso = ISO.DATE)
	private DateTime startDate;

	@DateTimeFormat(iso = ISO.DATE)
	private DateTime endDate;

	private int granularity = TemporalConstants.NONE;

	public void setStartDate(DateTime value) {
		this.startDate = value;
	}

	public DateTime getStartDate() {
		return this.startDate;
	}

	public void setEndDate(DateTime value) {
		this.endDate = value;
	}

	public DateTime getEndDate() {
		return this.endDate;
	}

	public void setGranularity(int value) {
		this.granularity = value;
	}

	public int getGranularity() {
		return this.granularity;
	}
}
