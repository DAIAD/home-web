package eu.daiad.web.model;

import org.joda.time.DateTime;

public class DataPoint {

	public long showerId;

	public int showerTime;

	public float temperature = 0;

	public float volume = 0;

	public float energy = 0;

	public float flow = 0;

	public long timestamp;

	public long getShowerId() {
		return showerId;
	}

	public void setShowerId(long showerId) {
		this.showerId = showerId;
	}

	public int getShowerTime() {
		return showerTime;
	}

	public void setShowerTime(int showerTime) {
		this.showerTime = showerTime;
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

	public float getFlow() {
		return flow;
	}

	public void setFlow(float flow) {
		this.flow = flow;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public int getDayOfMonth() {
		return new DateTime(this.timestamp).getDayOfMonth();
	}
}
