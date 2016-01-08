package eu.daiad.web.model;

public class AmphiroDataPoint extends AmphiroAbstractDataPoint {

	private long sessionId;

	private int index;

	private boolean history;

	public long getSessionId() {
		return sessionId;
	}

	public void setSessionId(long sessionId) {
		this.sessionId = sessionId;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public boolean isHistory() {
		return history;
	}

	public void setHistory(boolean history) {
		this.history = history;
	}
	
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

}
