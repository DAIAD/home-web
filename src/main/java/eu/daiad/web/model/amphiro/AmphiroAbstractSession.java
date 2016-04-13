package eu.daiad.web.model.amphiro;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class AmphiroAbstractSession {

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
	public DateTime getUtcDate() {
		return new DateTime(this.timestamp, DateTimeZone.UTC);
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
