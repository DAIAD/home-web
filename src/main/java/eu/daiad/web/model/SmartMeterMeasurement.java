package eu.daiad.web.model;

public class SmartMeterMeasurement {

	private long timestamp;
	
	private float volume;

	public float getVolume() {
		return volume;
	}

	public void setVolume(float volume) {
		this.volume = volume;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
}
