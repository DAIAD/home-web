package eu.daiad.common.model.amphiro;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class AmphiroAbstractDataPoint {

	protected long timestamp;

	protected float temperature;

	protected float volume;

	protected float energy;

	public long getTimestamp() {
		return timestamp;
	}

	@JsonIgnore
	public DateTime getDate() {
		return new DateTime(this.timestamp);
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

}
