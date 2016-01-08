package eu.daiad.web.model;

import java.util.ArrayList;
import java.util.UUID;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;

public class WaterMeterDataSeries {

	private int granularity = TemporalConstants.NONE;

	private UUID deviceKey;

	private WaterMeterDataPoint reference;

	private ArrayList<WaterMeterDataPoint> values = new ArrayList<WaterMeterDataPoint>();

	private long minTimestamp;

	private long maxTimestamp;

	public WaterMeterDataSeries(long minTimestamp, long maxTimestamp, int granularity) {
		this.minTimestamp = minTimestamp;
		this.maxTimestamp = maxTimestamp;
		
		this.granularity = granularity;
	}

	public UUID getDeviceKey() {
		return deviceKey;
	}

	public void setDeviceKey(UUID deviceKey) {
		this.deviceKey = deviceKey;
	}

	public WaterMeterDataPoint getReference() {
		return reference;
	}

	public long getMinTimestamp() {
		return minTimestamp;
	}

	public long getMaxTimestamp() {
		return maxTimestamp;
	}

	public ArrayList<WaterMeterDataPoint> getValues() {
		return values;
	}

	public void add(long timestamp, float volume) {
		DateTime date = new DateTime(timestamp);

		switch (this.granularity) {
		case TemporalConstants.NONE:
			// Retrieve values at the highest granularity, that is at the
			// measurement level
			break;
		case TemporalConstants.HOUR:
			date = new DateTime(date.getYear(), date.getMonthOfYear(),
					date.getDayOfMonth(), date.getHourOfDay(), 0, 0);
			break;
		case TemporalConstants.DAY:
			date = new DateTime(date.getYear(), date.getMonthOfYear(),
					date.getDayOfMonth(), 0, 0, 0);
			break;
		case TemporalConstants.WEEK:
			DateTime sunday = date.withDayOfWeek(DateTimeConstants.SUNDAY);

			date = new DateTime(sunday.getYear(), sunday.getMonthOfYear(),
					sunday.getDayOfMonth(), 0, 0, 0);
			break;
		case TemporalConstants.MONTH:
			date = new DateTime(date.getYear(), date.getMonthOfYear(), date
					.dayOfMonth().getMaximumValue(), 0, 0, 0);
			break;
		case TemporalConstants.YEAR:
			date = new DateTime(date.getYear(), 12, 31, 0, 0, 0);
			break;
		default:
			throw new IllegalArgumentException(
					"Granularity level not supported.");
		}

		timestamp = date.getMillis();

		if (this.reference == null) {
			this.reference = new WaterMeterDataPoint();
			this.reference.timestamp = Long.MAX_VALUE;
		}

		if ((timestamp < this.reference.timestamp)
				&& (timestamp > this.minTimestamp)) {
			this.setReference(timestamp, volume);
		} else if ((timestamp > this.reference.timestamp)
				&& (timestamp < this.minTimestamp)) {
			this.setReference(timestamp, volume);
		}

		if ((timestamp >= this.minTimestamp)
				&& (timestamp <= this.maxTimestamp)) {
			// Set volume value as the highest meter measurement in the interval
			// for the selected time granularity
			if (this.granularity != TemporalConstants.NONE) {
				for (int i = 0, count = values.size(); i < count; i++) {
					if (values.get(i).timestamp == timestamp) {
						if (values.get(i).volume < volume) {
							values.get(i).volume = volume;
						}
						return;
					}
				}
			}

			WaterMeterDataPoint point = new WaterMeterDataPoint();
			point.timestamp = timestamp;
			point.volume = volume;

			values.add(point);
		}
	}

	private void setReference(long timestamp, float volume) {
		this.reference.timestamp = timestamp;
		this.reference.volume = volume;
	}
}
