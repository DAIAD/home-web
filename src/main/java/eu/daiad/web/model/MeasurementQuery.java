package eu.daiad.web.model;

import java.util.Date;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;

public class MeasurementQuery {

	private UUID applicationKey;

	private UUID deviceId;

    private int granularity = TemporalConstants.NONE;
    
	@DateTimeFormat(pattern = "yyyy-MM-dd hh:mm:ss")
	private Date startDate;

	@DateTimeFormat(pattern = "yyyy-MM-dd hh:mm:ss")
	private Date endDate;

	public void setApplicationKey(UUID value) {
		this.applicationKey = value;
	}

	public UUID getApplicationKey() {
		return this.applicationKey;
	}

	public void setDeviceId(UUID value) {
		this.deviceId = value;
	}

	public UUID getDeviceId() {
		return this.deviceId;
	}

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
