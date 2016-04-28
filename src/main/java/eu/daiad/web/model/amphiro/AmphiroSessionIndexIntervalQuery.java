package eu.daiad.web.model.amphiro;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.daiad.web.model.AuthenticatedRequest;

public class AmphiroSessionIndexIntervalQuery extends AuthenticatedRequest {

	@JsonIgnore
	private UUID userKey;

	private UUID deviceKey;

	private long sessionId;

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

}
