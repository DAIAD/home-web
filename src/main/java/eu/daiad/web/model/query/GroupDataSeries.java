package eu.daiad.web.model.query;

import java.util.ArrayList;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;

public class GroupDataSeries {

	private String label;

	ArrayList<DataPoint> points = new ArrayList<DataPoint>();

	public GroupDataSeries(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	public ArrayList<DataPoint> getPoints() {
		return points;
	}

	private DataPoint getDataPoint(EnumTimeAggregation granularity, long timestamp, EnumMetric[] metrics) {
		DateTime date = new DateTime(timestamp, DateTimeZone.UTC);

		switch (granularity) {
			case HOUR:
				date = new DateTime(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth(), date.getHourOfDay(),
								0, 0, DateTimeZone.UTC);
				break;
			case DAY:
				date = new DateTime(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth(), 0, 0, 0,
								DateTimeZone.UTC);
				break;
			case WEEK:
				DateTime sunday = date.withDayOfWeek(DateTimeConstants.SUNDAY);

				date = new DateTime(sunday.getYear(), sunday.getMonthOfYear(), sunday.getDayOfMonth(), 0, 0, 0,
								DateTimeZone.UTC);
				break;
			case MONTH:
				date = new DateTime(date.getYear(), date.getMonthOfYear(), 1, 0, 0, 0, DateTimeZone.UTC);
				break;
			case YEAR:
				date = new DateTime(date.getYear(), 1, 1, 0, 0, 0, DateTimeZone.UTC);
				break;
			case ALL:
				break;
			default:
				throw new IllegalArgumentException("Granularity level not supported.");
		}

		DataPoint p = null;

		if (granularity == EnumTimeAggregation.ALL) {
			if (this.points.size() == 0) {
				p = new DataPoint();

				for (EnumMetric m : metrics) {
					if (m == EnumMetric.MIN) {
						p.getValues().put(m.toString(), Double.MAX_VALUE);
					} else {
						p.getValues().put(m.toString(), 0.0);
					}

				}
				this.points.add(p);
			}
			return this.points.get(0);
		} else {
			timestamp = date.getMillis();

			for (int i = 0, count = this.points.size(); i < count; i++) {
				if (timestamp == this.points.get(i).getTimestamp()) {
					p = this.points.get(i);

					break;
				}
			}

			if (p == null) {
				p = new DataPoint(timestamp);

				for (EnumMetric m : metrics) {
					if (m == EnumMetric.MIN) {
						p.getValues().put(m.toString(), Double.MAX_VALUE);
					} else {
						p.getValues().put(m.toString(), 0.0);
					}

				}

				this.points.add(p);
			}
		}
		return p;
	}

	public void addDataPoint(EnumTimeAggregation granularity, long timestamp, double volume, EnumMetric[] metrics) {
		DataPoint point = this.getDataPoint(granularity, timestamp, metrics);

		boolean avg = false;
		for (EnumMetric m : metrics) {
			switch (m) {
				case COUNT:
					point.getValues().put(m.toString(), point.getValues().get(m.toString()) + 1);
					break;
				case SUM:
					point.getValues().put(m.toString(), point.getValues().get(m.toString()) + volume);
					break;
				case MIN:
					if (point.getValues().get(m.toString()) > volume) {
						point.getValues().put(m.toString(), volume);
					}
					break;
				case MAX:
					if (point.getValues().get(m.toString()) < volume) {
						point.getValues().put(m.toString(), volume);
					}
					break;
				case AVERAGE:
					avg = true;
				default:
					// Ignore
			}
		}
		if (avg) {
			double count = point.getValues().get(EnumMetric.COUNT.toString());
			if (count == 0) {
				point.getValues().put(EnumMetric.AVERAGE.toString(), 0.0);
			} else {
				point.getValues().put(EnumMetric.AVERAGE.toString(),
								point.getValues().get(EnumMetric.SUM.toString()) / count);
			}
		}
	}
}
