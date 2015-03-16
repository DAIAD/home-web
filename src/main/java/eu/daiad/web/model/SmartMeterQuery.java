package eu.daiad.web.model;

import java.util.UUID;

public class SmartMeterQuery {

	private UUID applicationKey;

	private String deviceId;


	public void setApplicationKey(UUID value) {
		this.applicationKey = value;
	}

	public UUID getApplicationKey() {
		return this.applicationKey;
	}

	public void setDeviceId(String value) {
		this.deviceId = value;
	}

	public String getDeviceId() {
		return this.deviceId;
	}
}
