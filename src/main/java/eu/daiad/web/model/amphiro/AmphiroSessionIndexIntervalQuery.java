package eu.daiad.web.model.amphiro;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.daiad.web.model.AuthenticatedRequest;

public class AmphiroSessionIndexIntervalQuery extends AuthenticatedRequest {

	@JsonIgnore
	private UUID userKey;

	private UUID deviceKey;

	private long sessionId;

	private boolean excludeMeasurements = false;
	
	public UUID getUserKey() {
		return userKey;
	}

	public void setUserKey(UUID userKey) {
		this.userKey = userKey;
	}

	public UUID getDeviceKey() {
		return deviceKey;
	}

	public void setDeviceKey(UUID deviceKey) {
		this.deviceKey = deviceKey;
	}

	public long getSessionId() {
		return sessionId;
	}

	public void setSessionId(long sessionId) {
		this.sessionId = sessionId;
	}

    public boolean isExcludeMeasurements() {
        return excludeMeasurements;
    }

    public void setExcludeMeasurements(boolean excludeMeasurements) {
        this.excludeMeasurements = excludeMeasurements;
    }

}
