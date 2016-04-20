package eu.daiad.web.model.amphiro;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class AmphiroMeasurement {

	private long sessionId;

	private int index;

	private boolean history;

	private long timestamp;

	private float temperature;

	private float volume;

	private float energy;

	@JsonIgnore
	private AmphiroSession session;

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

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public float getTemperature() {
		return temperature;
	}

	public void setTemperature(float temperature) {
		this.temperature = temperature;
	}

	public float getVolume() {
		return volume;
	}

	public void setVolume(float volume) {
		this.volume = volume;
	}

	public float getEnergy() {
		return energy;
	}

	public void setEnergy(float energy) {
		this.energy = energy;
	}

	public AmphiroSession getSession() {
		return session;
	}

	public void setSession(AmphiroSession session) {
		this.session = session;
	}

}
