package eu.daiad.common.model.amphiro;

import java.util.UUID;

public class AmphiroSessionUpdate {

	private UUID deviceKey;

	private long sessionId;

	private long timestamp;

	public AmphiroSessionUpdate(UUID deviceKey, long sessionId, long timestamp) {
		this.deviceKey = deviceKey;
		this.sessionId = sessionId;
		this.timestamp = timestamp;
	}

	public long getSessionId() {
		return sessionId;
	}

	public void setSessionId(long sessionId) {
		this.sessionId = sessionId;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public UUID getDeviceKey() {
		return deviceKey;
	}

	public void setDeviceKey(UUID deviceKey) {
		this.deviceKey = deviceKey;
	}

}
