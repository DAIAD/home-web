package eu.daiad.web.model.query;

import java.util.ArrayList;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;

public class GroupDataSeries {

	private String label;

	private int population;

	ArrayList<DataPoint> points = new ArrayList<DataPoint>();

	public GroupDataSeries(String label, int population) {
		this.label = label;
		this.population = population;
	}

	public String getLabel() {
		return label;
	}

	public ArrayList<DataPoint> getPoints() {
		return points;
	}

	private DataPoint getDataPoint(EnumTimeAggregation granularity, long timestamp, EnumMetric[] metrics,
					DataPoint.EnumDataPointType type, DateTimeZone timezone) {
		DateTime date = new DateTime(timestamp, timezone);

		switch (granularity) {
			case HOUR:
				date = new DateTime(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth(), date.getHourOfDay(),
								0, 0, timezone);
				break;
			case DAY:
				date = new DateTime(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth(), 0, 0, 0, timezone);
				break;
			case WEEK:
				DateTime sunday = date.withDayOfWeek(DateTimeConstants.SUNDAY);

				date = new DateTime(sunday.getYear(), sunday.getMonthOfYear(), sunday.getDayOfMonth(), 0, 0, 0,
								timezone);
				break;
			case MONTH:
				date = new DateTime(date.getYear(), date.getMonthOfYear(), 1, 0, 0, 0, timezone);
				break;
			case YEAR:
				date = new DateTime(date.getYear(), 1, 1, 0, 0, 0, timezone);
				break;
			case ALL:
				break;
			default:
				throw new IllegalArgumentException("Granularity level not supported.");
		}

		DataPoint p = null;

		if (granularity == EnumTimeAggregation.ALL) {
			if (this.points.size() == 0) {
				switch (type) {
					case METER:
						p = new DataPoint();

						for (EnumMetric m : metrics) {
							if (m == EnumMetric.MIN) {
								p.getVolume().put(m, Double.MAX_VALUE);
							} else {
								p.getVolume().put(m, 0.0);
							}

						}
						break;
					case AMPHIRO:
						AmphiroDataPoint ap = new AmphiroDataPoint();

						for (EnumMetric m : metrics) {
							if (m == EnumMetric.MIN) {
								ap.getVolume().put(m, Double.MAX_VALUE);
							} else {
								ap.getVolume().put(m, 0.0);
							}
							if (m == EnumMetric.MIN) {
								ap.getEnergy().put(m, Double.MAX_VALUE);
							} else {
								ap.getEnergy().put(m, 0.0);
							}
							if (m == EnumMetric.MIN) {
								ap.getTemperature().put(m, Double.MAX_VALUE);
							} else {
								ap.getTemperature().put(m, 0.0);
							}
							if (m == EnumMetric.MIN) {
								ap.getDuration().put(m, Double.MAX_VALUE);
							} else {
								ap.getDuration().put(m, 0.0);
							}
							if (m == EnumMetric.MIN) {
								ap.getFlow().put(m, Double.MAX_VALUE);
							} else {
								ap.getFlow().put(m, 0.0);
							}

							p = ap;
						}
						break;
					default:
						throw new IllegalArgumentException("Data point type is not supported.");

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
				switch (type) {
					case METER:
						p = new DataPoint(timestamp);

						for (EnumMetric m : metrics) {
							if (m == EnumMetric.MIN) {
								p.getVolume().put(m, Double.MAX_VALUE);
							} else {
								p.getVolume().put(m, 0.0);
							}

						}
						break;
					case AMPHIRO:
						AmphiroDataPoint ap = new AmphiroDataPoint(timestamp);

						for (EnumMetric m : metrics) {
							if (m == EnumMetric.MIN) {
								ap.getVolume().put(m, Double.MAX_VALUE);
							} else {
								ap.getVolume().put(m, 0.0);
							}

							// Compute COUNT only for volume
							if (m == EnumMetric.COUNT) {
								continue;
							}

							if (m == EnumMetric.MIN) {
								ap.getEnergy().put(m, Double.MAX_VALUE);
							} else {
								ap.getEnergy().put(m, 0.0);
							}
							if (m == EnumMetric.MIN) {
								ap.getTemperature().put(m, Double.MAX_VALUE);
							} else {
								ap.getTemperature().put(m, 0.0);
							}
							if (m == EnumMetric.MIN) {
								ap.getDuration().put(m, Double.MAX_VALUE);
							} else {
								ap.getDuration().put(m, 0.0);
							}
							if (m == EnumMetric.MIN) {
								ap.getFlow().put(m, Double.MAX_VALUE);
							} else {
								ap.getFlow().put(m, 0.0);
							}

							p = ap;
						}
						break;
					default:
						throw new IllegalArgumentException("Data point type is not supported.");

				}
				this.points.add(p);
			}
		}
		return p;
	}

	public void addDataPoint(EnumTimeAggregation granularity, long timestamp, double volume, EnumMetric[] metrics,
					DateTimeZone timezone) {
		DataPoint point = this.getDataPoint(granularity, timestamp, metrics, DataPoint.EnumDataPointType.METER,
						timezone);

		boolean avg = false;
		for (EnumMetric m : metrics) {
			switch (m) {
				case COUNT:
					point.getVolume().put(m, point.getVolume().get(m) + 1);
					break;
				case SUM:
					point.getVolume().put(m, point.getVolume().get(m) + volume);
					break;
				case MIN:
					if (point.getVolume().get(m) > volume) {
						point.getVolume().put(m, volume);
					}
					break;
				case MAX:
					if (point.getVolume().get(m) < volume) {
						point.getVolume().put(m, volume);
					}
					break;
				case AVERAGE:
					avg = true;
				default:
					// Ignore
			}
		}
		if (avg) {
			double count = point.getVolume().get(EnumMetric.COUNT);
			if (count == 0) {
				point.getVolume().put(EnumMetric.AVERAGE, 0.0);
			} else {
				point.getVolume().put(EnumMetric.AVERAGE, point.getVolume().get(EnumMetric.SUM) / count);
			}
		}
	}

	public void addDataPoint(EnumTimeAggregation granularity, long timestamp, double volume, double energy,
					double duration, double temperature, double flow, EnumMetric[] metrics, DateTimeZone timezone) {
		AmphiroDataPoint point = (AmphiroDataPoint) this.getDataPoint(granularity, timestamp, metrics,
						DataPoint.EnumDataPointType.AMPHIRO, timezone);

		boolean avg = false;
		for (EnumMetric m : metrics) {
			switch (m) {
				case COUNT:
					point.getVolume().put(m, point.getVolume().get(m) + 1);
					break;
				case SUM:
					point.getVolume().put(m, point.getVolume().get(m) + volume);
					point.getEnergy().put(m, point.getEnergy().get(m) + energy);
					point.getDuration().put(m, point.getDuration().get(m) + duration);
					point.getTemperature().put(m, point.getTemperature().get(m) + temperature);
					point.getFlow().put(m, point.getFlow().get(m) + flow);
					break;
				case MIN:
					if (point.getVolume().get(m) > volume) {
						point.getVolume().put(m, volume);
					}
					if (point.getEnergy().get(m) > energy) {
						point.getEnergy().put(m, energy);
					}
					if (point.getDuration().get(m) > duration) {
						point.getDuration().put(m, duration);
					}
					if (point.getTemperature().get(m) > temperature) {
						point.getTemperature().put(m, temperature);
					}
					if (point.getFlow().get(m) > flow) {
						point.getFlow().put(m, flow);
					}
					break;
				case MAX:
					if (point.getVolume().get(m) < volume) {
						point.getVolume().put(m, volume);
					}
					if (point.getEnergy().get(m) < energy) {
						point.getEnergy().put(m, energy);
					}
					if (point.getDuration().get(m) < duration) {
						point.getDuration().put(m, duration);
					}
					if (point.getTemperature().get(m) < temperature) {
						point.getTemperature().put(m, temperature);
					}
					if (point.getFlow().get(m) < flow) {
						point.getFlow().put(m, flow);
					}
					break;
				case AVERAGE:
					avg = true;
				default:
					// Ignore
			}
		}
		if (avg) {
			double count = point.getVolume().get(EnumMetric.COUNT);
			if (count == 0) {
				point.getVolume().put(EnumMetric.AVERAGE, 0.0);
				point.getEnergy().put(EnumMetric.AVERAGE, 0.0);
				point.getDuration().put(EnumMetric.AVERAGE, 0.0);
				point.getTemperature().put(EnumMetric.AVERAGE, 0.0);
				point.getFlow().put(EnumMetric.AVERAGE, 0.0);
			} else {
				point.getVolume().put(EnumMetric.AVERAGE, point.getVolume().get(EnumMetric.SUM) / count);
				point.getEnergy().put(EnumMetric.AVERAGE, point.getEnergy().get(EnumMetric.SUM) / count);
				point.getDuration().put(EnumMetric.AVERAGE, point.getDuration().get(EnumMetric.SUM) / count);
				point.getTemperature().put(EnumMetric.AVERAGE, point.getTemperature().get(EnumMetric.SUM) / count);
				point.getFlow().put(EnumMetric.AVERAGE, point.getFlow().get(EnumMetric.SUM) / count);
			}
		}
	}

	public int getPopulation() {
		return population;
	}

}
