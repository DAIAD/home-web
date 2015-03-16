package eu.daiad.web.model;

import java.util.Date;

import org.joda.time.DateTime;

public class Shower {

	private long id;

	private long timestamp = Long.MAX_VALUE;

	private float volume;

	private float energy;

	private float temperature;

	private long minTimeSlice = Long.MAX_VALUE;

	private long maxTimeSlice = Long.MIN_VALUE;

	private int minShowerTime = Integer.MAX_VALUE;

	private int maxShowerTime = Integer.MIN_VALUE;
	
	private int count = 0;
	
	public Shower(long id) {
		this.id = id;
	}

	public long getId() {
		return this.id;
	}

	public Date getDate() {
		if (this.count == 0) {
			return null;
		}
		return new Date(this.timestamp);
	}
	
	public long getTimestamp() {
		if (this.count == 0) {
			return Long.MIN_VALUE;
		}
		return this.timestamp;
	}

	public float getVolume() {
		return this.volume;
	}

	public float getEnergy() {
		return this.energy;
	}

	public float getTemperature() {
		return this.temperature;
	}

	public long getDuration() {
		return (this.count == 0 ? 0 : (this.maxShowerTime
				- this.minShowerTime + 1));
	}
	
	public int getDayOfMonth() {
		return new DateTime(this.timestamp).getDayOfMonth();
	}

	public void add(DataPoint point) {
		if (point.timestamp < this.minTimeSlice) {
			this.minTimeSlice = point.timestamp;
		}
		if (point.timestamp > this.maxTimeSlice) {
			this.maxTimeSlice = point.timestamp;
		}
		if (point.showerTime < this.minShowerTime) {
			this.minShowerTime = point.showerTime;
		}
		if (point.showerTime > this.maxShowerTime) {
			this.maxShowerTime = point.showerTime;
		}
		this.volume += point.volume;
		this.energy += point.energy;
		this.temperature = ((this.temperature * this.count) + point.temperature)
				/ (this.count + 1);

		this.count++;
		
		if (this.timestamp > point.timestamp) {
			this.timestamp = point.timestamp;
		}
	}
}
