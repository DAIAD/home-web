package eu.daiad.web.model;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

public class SmartMeterIntervalQuery extends SmartMeterQuery {

    private int granularity = TemporalConstants.NONE;
    
	@DateTimeFormat(pattern = "yyyy-MM-dd hh:mm:ss")
	private Date startDate;

	@DateTimeFormat(pattern = "yyyy-MM-dd hh:mm:ss")
	private Date endDate;

	public void setStartDate(Date value) {
		this.startDate = value;
	}

	public Date getStartDate() {
		return this.startDate;
	}

	public void setEndDate(Date value) {
		this.endDate = value;
	}

	public Date getEndDate() {
		return this.endDate;
	}
	
	public void setGranularity(int value) {
		this.granularity = value;
	}

	public int getGranularity() {
		return this.granularity;
	}
}
