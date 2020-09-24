package eu.daiad.common.model.meter;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class WaterMeterDataPoint {

	private long timestamp = 0;

	private float volume = 0;

	private float difference = 0;

	public int getWeek() {
		return (new DateTime(timestamp)).getWeekOfWeekyear();
	}

	@JsonIgnore
	public DateTime getUtcDate() {
		return new DateTime(this.timestamp, DateTimeZone.UTC);
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public float getVolume() {
		return volume;
	}

	public void setVolume(float volume) {
		this.volume = volume;
	}

	public float getDifference() {
		return difference;
	}

	public void setDifference(float difference) {
		this.difference = difference;
	}

}
