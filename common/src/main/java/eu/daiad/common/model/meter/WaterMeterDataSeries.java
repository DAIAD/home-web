package eu.daiad.common.model.meter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.daiad.common.model.TemporalConstants;

public class WaterMeterDataSeries {

	@JsonIgnore
	private int granularity = TemporalConstants.NONE;

	private UUID deviceKey;

	private String serial;

	private WaterMeterDataPoint reference;

	private List<WaterMeterDataPoint> values = new ArrayList<WaterMeterDataPoint>();

	private long minTimestamp;

	private long maxTimestamp;

	public WaterMeterDataSeries(UUID deviceKey, String serial, long minTimestamp, long maxTimestamp, int granularity) {
		this.minTimestamp = minTimestamp;
		this.maxTimestamp = maxTimestamp;

		this.granularity = granularity;

		this.deviceKey = deviceKey;
		this.serial = serial;
	}

	public UUID getDeviceKey() {
		return deviceKey;
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

	public List<WaterMeterDataPoint> getValues() {
		return values;
	}

	public void sort() {
		Collections.sort(this.values, new Comparator<WaterMeterDataPoint>() {
			public int compare(WaterMeterDataPoint o1, WaterMeterDataPoint o2) {
				if (o1.getTimestamp() <= o2.getTimestamp()) {
					return -1;
				} else {
					return 1;
				}
			}
		});
	}

	public void add(long timestamp, float volume, float difference, DateTimeZone timezone) {
		DateTime date = new DateTime(timestamp, timezone);

		switch (this.granularity) {
			case TemporalConstants.NONE:
				// Retrieve values at the highest granularity, that is at the
				// measurement level
				break;
			case TemporalConstants.HOUR:
				date = new DateTime(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth(), date.getHourOfDay(),
								0, 0, timezone);
				break;
			case TemporalConstants.DAY:
				date = new DateTime(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth(), 0, 0, 0, timezone);
				break;
			case TemporalConstants.WEEK:
				DateTime sunday = date.withDayOfWeek(DateTimeConstants.SUNDAY);

				date = new DateTime(sunday.getYear(), sunday.getMonthOfYear(), sunday.getDayOfMonth(), 0, 0, 0,
								timezone);
				break;
			case TemporalConstants.MONTH:
				date = new DateTime(date.getYear(), date.getMonthOfYear(), date.dayOfMonth().getMaximumValue(), 0, 0,
								0, timezone);
				break;
			case TemporalConstants.YEAR:
				date = new DateTime(date.getYear(), 12, 31, 0, 0, 0, timezone);
				break;
			default:
				throw new IllegalArgumentException("Granularity level not supported.");
		}

		timestamp = date.getMillis();

		if (this.reference == null) {
			this.reference = new WaterMeterDataPoint();
			this.reference.setTimestamp(timestamp);
			this.reference.setVolume(volume);
		}

		if (timestamp < this.reference.getTimestamp()) {
			this.setReference(timestamp, volume);
		}

		if ((timestamp >= this.minTimestamp) && (timestamp <= this.maxTimestamp)) {
			// Set volume value as the highest meter measurement in the interval
			// for the selected time granularity
			if (this.granularity != TemporalConstants.NONE) {
				for (int i = 0, count = values.size(); i < count; i++) {
					if (values.get(i).getTimestamp() == timestamp) {
						if (values.get(i).getVolume() < volume) {
							values.get(i).setVolume(volume);
						}
						values.get(i).setDifference(values.get(i).getDifference() + difference);
						return;
					}
				}
			}

			WaterMeterDataPoint point = new WaterMeterDataPoint();
			point.setTimestamp(timestamp);
			point.setVolume(volume);
			point.setDifference(difference);

			values.add(point);
		}
	}

	private void setReference(long timestamp, float volume) {
		this.reference.setTimestamp(timestamp);
		this.reference.setVolume(volume);
	}

	public String getSerial() {
		return serial;
	}
}
