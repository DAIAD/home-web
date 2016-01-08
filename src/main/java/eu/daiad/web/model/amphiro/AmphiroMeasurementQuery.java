package eu.daiad.web.model.amphiro;

import java.util.UUID;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.daiad.web.model.AuthenticatedRequest;
import eu.daiad.web.model.TemporalConstants;

public class AmphiroMeasurementQuery extends AuthenticatedRequest {

	@JsonIgnore
	private UUID userKey;

	private UUID deviceKey[];

	private DateTime startDate;

	private DateTime endDate;

	private int granularity = TemporalConstants.NONE;

	public UUID getUserKey() {
		return userKey;
	}

	public void setUserKey(UUID userKey) {
		this.userKey = userKey;
	}

	public UUID[] getDeviceKey() {
		return deviceKey;
	}

	public void setDeviceKey(UUID[] deviceKey) {
		this.deviceKey = deviceKey;
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

	public void setGranularity(int value) {
		this.granularity = value;
	}

	public int getGranularity() {
		return this.granularity;
	}
}
