package eu.daiad.web.model.amphiro;

public class AmphiroSessionUpdate {

	private long sessionId;

	private long timestamp;

	public AmphiroSessionUpdate(long sessionId, long timestamp) {
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

}
