package eu.daiad.web.model;

import java.util.UUID;

public class WaterMeterStatus {

	private UUID deviceKey;

	private long timestamp;
	
	private float volume;

	private float variation;

	public UUID getDeviceKey() {
		return deviceKey;
	}

	public void setDeviceKey(UUID deviceKey) {
		this.deviceKey = deviceKey;
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


}
