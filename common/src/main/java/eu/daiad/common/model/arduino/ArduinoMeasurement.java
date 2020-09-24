package eu.daiad.common.model.arduino;

public class ArduinoMeasurement {
	
	private long timestamp;
	
	private long volume;

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public long getVolume() {
		return volume;
	}

	public void setVolume(long volume) {
		this.volume = volume;
	}

}
