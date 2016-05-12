package eu.daiad.web.model.amphiro;

import java.util.ArrayList;
import java.util.UUID;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.daiad.web.model.TemporalConstants;

public class AmphiroDataSeries {

	private UUID deviceKey;

	@JsonIgnore
	private int granularity = TemporalConstants.NONE;

	private ArrayList<AmphiroAbstractDataPoint> points;

	public AmphiroDataSeries(UUID deviceKey) {
		this.deviceKey = deviceKey;
		this.granularity = TemporalConstants.NONE;

		this.points = new ArrayList<AmphiroAbstractDataPoint>();
	}

	public AmphiroDataSeries(UUID deviceKey, int granularity) {
		this.deviceKey = deviceKey;
		this.granularity = granularity;

		this.points = new ArrayList<AmphiroAbstractDataPoint>();
	}

	public UUID getDeviceKey() {
		return this.deviceKey;
	}

	public int getGranularity() {
		return this.granularity;
	}

	public void setPoints(ArrayList<AmphiroDataPoint> value, DateTimeZone timezone) {
		this.points.clear();
		if (value != null) {
			for (int i = 0, count = value.size(); i < count; i++) {
				this.add(value.get(i), timezone);
			}
		}
	}

	public ArrayList<AmphiroAbstractDataPoint> getPoints() {
		return this.points;
	}

	public void add(AmphiroDataPoint point, DateTimeZone timezone) {
		if (this.granularity == TemporalConstants.NONE) {
			// Retrieve values at the highest granularity, that is at the
			// measurement level
			this.points.add(point);
		} else {

			DateTime date = new DateTime(point.getTimestamp(), timezone);

			switch (this.granularity) {
				case TemporalConstants.HOUR:
					date = new DateTime(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth(),
									date.getHourOfDay(), 0, 0, timezone);
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
					date = new DateTime(date.getYear(), date.getMonthOfYear(), 1, 0, 0, 0, timezone);
					break;
				case TemporalConstants.YEAR:
					date = new DateTime(date.getYear(), 1, 1, 0, 0, 0, timezone);
					break;
				default:
					throw new IllegalArgumentException("Granularity level not supported.");
			}

			AmphiroAggregatedDataPoint aggregate = null;

			for (int i = 0, count = this.points.size(); i < count; i++) {
				if (date.getMillis() == this.points.get(i).getTimestamp()) {
					aggregate = (AmphiroAggregatedDataPoint) this.points.get(i);

					aggregate.addPoint(point);

					return;
				}
			}

			aggregate = new AmphiroAggregatedDataPoint(date.getMillis());
			aggregate.addPoint(point);

			this.points.add(aggregate);
		}
	}

}