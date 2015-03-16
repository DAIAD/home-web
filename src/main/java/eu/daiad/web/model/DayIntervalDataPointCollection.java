package eu.daiad.web.model;

import java.util.ArrayList;

import org.joda.time.DateTime;

public class DayIntervalDataPointCollection {

	private ArrayList<DailyDataPoints> points;

	public DayIntervalDataPointCollection() {
		this.points = new ArrayList<DailyDataPoints>();
	}

	public void add(DataPoint point) {
		DateTime arg = new DateTime(point.timestamp);

		long timestamp = new DateTime(arg.getYear(), arg.getMonthOfYear(),
				arg.getDayOfMonth(), 0, 0, 0).getMillis();

		for (int i = 0, count = this.points.size(); i < count; i++) {
			if (this.points.get(i).getTimestamp() == timestamp) {
				this.points.get(i).add(point);
				return;
			}
		}
		
		DailyDataPoints day = new DailyDataPoints(timestamp);
		day.add(point);
		this.points.add(day);
	}

	public void addAll(ArrayList<DataPoint> arg) {
		for (int i = 0, count = arg.size(); i < count; i++) {
			this.add(arg.get(i));
		}
	}

	public ArrayList<DataPoint> getPoints() {
		ArrayList<DataPoint> result = new ArrayList<DataPoint>();

		for (int i = 0, count = this.points.size(); i < count; i++) {
			result.add(this.points.get(i).aggregate());
		}

		return result;
	}
}
