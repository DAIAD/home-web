package eu.daiad.web.model;

import org.joda.time.DateTime;

public class SmartMeterDataPoint {

	public long timestamp = 0;

	public float volume = 0;
	
	public int getDayOfMonth() {
		return new DateTime(this.timestamp).getDayOfMonth();
	}

}
