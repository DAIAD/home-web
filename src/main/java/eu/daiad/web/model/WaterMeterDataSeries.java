package eu.daiad.web.model;

import java.util.ArrayList;
import java.util.UUID;

import org.joda.time.DateTime;

public class WaterMeterDataSeries {

	private UUID deviceKey;

	private SmartMeterDataPoint reference = null;

	private ArrayList<SmartMeterDataPoint> values = new ArrayList<SmartMeterDataPoint>();

	private long minTimestamp;

	private long maxTimestamp;

	public WaterMeterDataSeries(long minTimestamp, long maxTimestamp) {
		this.minTimestamp = minTimestamp;
		this.maxTimestamp = maxTimestamp;
	}


	public UUID getDeviceKey() {
		return deviceKey;
	}

	public void setDeviceKey(UUID deviceKey) {
		this.deviceKey = deviceKey;
	}

	public SmartMeterDataPoint getReference() {
		return reference;
	}

	public long getMinTimestamp() {
		return minTimestamp;
	}

	public long getMaxTimestamp() {
		return maxTimestamp;
	}

	public ArrayList<SmartMeterDataPoint> getValues() {
		return values;
	}

	public void add(long timestamp, float volume) {
		DateTime date = new DateTime(timestamp);

		timestamp = new DateTime(date.getYear(), date.getMonthOfYear(),
				date.getDayOfMonth(), 0, 0, 0).getMillis();

		if (timestamp < this.minTimestamp) {
			if (this.reference == null) {
				this.reference = new SmartMeterDataPoint();
			}
			if (this.reference.timestamp < timestamp) {
				this.reference.timestamp = timestamp;
				this.reference.volume = volume;
			}
		} else {
			for (int i = 0, count = values.size(); i < count; i++) {
				if (values.get(i).timestamp == timestamp) {
					if (values.get(i).volume < volume) {
						values.get(i).volume = volume;
					}
					return;
				}
			}
			SmartMeterDataPoint point = new SmartMeterDataPoint();
			point.timestamp = timestamp;
			point.volume = volume;

			values.add(point);
		}
	}
}
