package eu.daiad.web.model.meter;

import java.util.UUID;

public class WaterMeterStatus {

	private UUID deviceKey;

	private String serial;

	private long timestamp;

	private float volume;

	private float variation;

	public WaterMeterStatus(UUID deviceKey, String serial) {
		this.deviceKey = deviceKey;
		this.serial = serial;
	}

	public UUID getDeviceKey() {
		return deviceKey;
	}

	public float getVariation() {
		return variation;
	}

	public void setVariation(float variation) {
		this.variation = variation;
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

}
