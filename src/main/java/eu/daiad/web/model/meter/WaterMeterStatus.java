package eu.daiad.web.model.meter;

import java.util.UUID;

public class WaterMeterStatus {

	private UUID deviceKey;

	private String serial;

	private long timestamp;

	private float volume;

	public WaterMeterStatus(String serial) {
		this.serial = serial;
	}

	public UUID getDeviceKey() {
		return deviceKey;
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

	public String getSerial() {
		return serial;
	}

	public void setDeviceKey(UUID deviceKey) {
		this.deviceKey = deviceKey;
	}

}
