package eu.daiad.web.model;

import java.util.ArrayList;
import org.joda.time.DateTime;

public class HourlyDataPoints {

	public HourlyDataPoints() {
		this.points = new ArrayList<DataPoint>();
	}

	private ArrayList<DataPoint> points;

	public ArrayList<DataPoint> getPoints() {
		return this.points;
	}

	public void add(DataPoint point) {
		this.points.add(point);
	}

	public void clear() {
		this.points.clear();
	}

	public int size() {
		return this.points.size();
	}

	public DataPoint average() {
		if (this.points.size() == 0) {
			return null;
		}

		DataPoint value = new DataPoint();

		for (int i = 0, count = this.points.size(); i < count; i++) {
			value.volume += this.points.get(i).volume;
			value.energy += this.points.get(i).energy;
			value.temperature += this.points.get(i).temperature;
		}
		value.temperature /= (float) this.points.size();
	
		DateTime timestamp = new DateTime(this.points.get(0).timestamp);

		value.timestamp = new DateTime(timestamp.getYear(),
				timestamp.getMonthOfYear(), timestamp.getDayOfMonth(),
				timestamp.getHourOfDay(), 0, 0).getMillis();

		return value;
	}
}
