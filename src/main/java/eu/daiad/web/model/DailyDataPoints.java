package eu.daiad.web.model;

import org.joda.time.DateTime;

public class DailyDataPoints {

	private int pointCount = 0;

	private DataPoint point;

	public DailyDataPoints(long timestamp) {
		DateTime arg = new DateTime(timestamp);

		this.point = new DataPoint();

		this.point.timestamp = new DateTime(arg.getYear(),
				arg.getMonthOfYear(), arg.getDayOfMonth(), 0, 0, 0).getMillis();
	}

	public DailyDataPoints(DateTime date) {
		this.point = new DataPoint();

		this.point.timestamp = new DateTime(date.getYear(),
				date.getMonthOfYear(), date.getDayOfMonth(), 0, 0, 0)
				.getMillis();
	}

	public void add(DataPoint point) {
		DateTime arg = new DateTime(point.timestamp);

		if (this.point.timestamp == new DateTime(arg.getYear(),
				arg.getMonthOfYear(), arg.getDayOfMonth(), 0, 0, 0).getMillis()) {

			this.point.volume += point.volume;
			this.point.energy += point.energy;
			this.point.temperature = (this.point.temperature * this.pointCount + point.temperature);
			this.pointCount++;
			this.point.temperature = this.point.temperature
					/ (float) this.pointCount;
		}
	}

	public DataPoint aggregate() {
		return this.point;
	}

	public long getTimestamp() {
		return this.point.timestamp;
	}

}
