package eu.daiad.web.model.arduino;

import com.fasterxml.jackson.annotation.JsonSetter;

public class ArduinoIntervalQuery {

	String deviceKey;
	
	long timestampStart;
	
	long timestampEnd;

	public String getDeviceKey() {
		return deviceKey;
	}

	public void setDeviceKey(String deviceKey) {
		this.deviceKey = deviceKey;
	}

	public long getTimestampStart() {
		return timestampStart;
	}

	@JsonSetter("start")
	public void setTimestampStart(long timestampStart) {
		this.timestampStart = timestampStart;
	}

	public long getTimestampEnd() {
		return timestampEnd;
	}

	@JsonSetter("end")
	public void setTimestampEnd(long timestampEnd) {
		this.timestampEnd = timestampEnd;
	}
	
}
