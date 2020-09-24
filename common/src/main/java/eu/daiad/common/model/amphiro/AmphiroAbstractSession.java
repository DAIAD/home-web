package eu.daiad.common.model.amphiro;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class AmphiroAbstractSession {

	protected Long timestamp;

	protected int duration;

	protected float temperature;

	protected float volume;

	protected float energy;

	protected float flow;

	public Long getTimestamp() {
		return timestamp;
	}

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

	@JsonIgnore
	public DateTime getUtcDate() {
		return new DateTime(timestamp, DateTimeZone.UTC);
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
