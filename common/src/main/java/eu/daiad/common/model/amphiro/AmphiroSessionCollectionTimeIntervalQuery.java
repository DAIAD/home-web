package eu.daiad.common.model.amphiro;

import java.util.UUID;

import eu.daiad.common.model.AuthenticatedRequest;
import eu.daiad.common.model.TemporalConstants;

public class AmphiroSessionCollectionTimeIntervalQuery extends AuthenticatedRequest {

	private UUID userKey;

	private UUID[] deviceKey;

	private Long startDate;

	private Long endDate;

	private int granularity = TemporalConstants.NONE;

	public AmphiroSessionCollectionTimeIntervalQuery() {

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

	public void setStartDate(Long value) {
		this.startDate = value;
	}

	public Long getStartDate() {
		return this.startDate;
	}

	public void setEndDate(Long value) {
		this.endDate = value;
	}

	public Long getEndDate() {
		return this.endDate;
	}

	public void setGranularity(int value) {
		this.granularity = value;
	}

	public int getGranularity() {
		return this.granularity;
	}

}
