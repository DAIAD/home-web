package eu.daiad.web.model;

import java.util.UUID;

import org.joda.time.DateTime;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

public class ShowerQuery {

	private UUID userKey;

	private UUID deviceKey;

	public UUID getUserKey() {
		return userKey;
	}

	private long showerId;

	@DateTimeFormat(iso = ISO.DATE)
	private DateTime startDate;

	@DateTimeFormat(iso = ISO.DATE)
	private DateTime endDate;

	public void setUserKey(UUID userKey) {
		this.userKey = userKey;
	}

	public UUID getDeviceKey() {
		return deviceKey;
	}

	public void setDeviceKey(UUID deviceKey) {
		this.deviceKey = deviceKey;
	}

	public void setShowerId(long value) {
		this.showerId = value;
	}

	public long getShowerId() {
		return this.showerId;
	}

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

}
