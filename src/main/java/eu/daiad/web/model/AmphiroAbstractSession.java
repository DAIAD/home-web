package eu.daiad.web.model;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class AmphiroAbstractSession {

	protected long timestamp;

	protected int duration;

	protected float temperature;

	protected float volume;

	protected float energy;

	protected float flow;

	public long getTimestamp() {
		return timestamp;
	}

	@JsonIgnore
	public DateTime getDate() {
		return new DateTime(this.timestamp);
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
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

}
