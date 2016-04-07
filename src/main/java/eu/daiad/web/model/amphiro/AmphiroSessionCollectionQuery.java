package eu.daiad.web.model.amphiro;

import java.util.UUID;

import eu.daiad.web.model.AuthenticatedRequest;
import eu.daiad.web.model.TemporalConstants;

public class AmphiroSessionCollectionQuery extends AuthenticatedRequest {

	private UUID userKey;

	private UUID[] deviceKey;

	private long startDate;

	private long endDate;

	private int granularity = TemporalConstants.NONE;

	public AmphiroSessionCollectionQuery() {

	}

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

	public void setStartDate(long value) {
		this.startDate = value;
	}

	public long getStartDate() {
		return this.startDate;
	}

	public void setEndDate(long value) {
		this.endDate = value;
	}

	public long getEndDate() {
		return this.endDate;
	}

	public void setGranularity(int value) {
		this.granularity = value;
	}

	public int getGranularity() {
		return this.granularity;
	}

}
