package eu.daiad.web.model.query;

import java.util.ArrayList;
import java.util.List;

public class RankingDataPoint extends DataPoint {

	private ArrayList<UserDataPoint> users = new ArrayList<UserDataPoint>();

	public RankingDataPoint() {
		type = EnumDataPointType.RANKING;
	}

	public RankingDataPoint(long timestamp) {
		super(timestamp);
		type = EnumDataPointType.RANKING;
	}

	public ArrayList<UserDataPoint> getUsers() {
		return users;
	}

	public DataPoint aggregate(List<EnumMetric> metrics, DataPoint.EnumDataPointType type) {
		switch (type) {
			case METER:
				MeterDataPoint p = (getTimestamp() == null ? new MeterDataPoint() : new MeterDataPoint(getTimestamp()));

				for (EnumMetric m : metrics) {
					if (m == EnumMetric.MIN) {
						p.getVolume().put(m, Double.MAX_VALUE);
					} else {
						p.getVolume().put(m, 0.0);
					}
				}

				double diff;
				boolean average = false;

				for (UserDataPoint user : users) {
					MeterUserDataPoint meterUser = (MeterUserDataPoint) user;

					for (EnumMetric m : metrics) {
						switch (m) {
							case COUNT:
								p.getVolume().put(m, (double) users.size());
								break;
							case SUM:
								p.getVolume().put(m, p.getVolume().get(m) + meterUser.getVolume().get(m));
								break;
							case MIN:
								diff = meterUser.getVolume().get(EnumMetric.MAX)
												- meterUser.getVolume().get(EnumMetric.MIN);

								if (diff < p.getVolume().get(m)) {
									p.getVolume().put(m, diff);
								}
								break;
							case MAX:
								diff = meterUser.getVolume().get(EnumMetric.MAX)
												- meterUser.getVolume().get(EnumMetric.MIN);

								if (diff > p.getVolume().get(m)) {
									p.getVolume().put(m, diff);
								}
								break;
							case AVERAGE:
								average = true;
							default:
								// Ignore
						}
					}
				}
				if (average) {
					if (users.size() == 0) {
						p.getVolume().put(EnumMetric.AVERAGE, 0.0);
					} else {
						p.getVolume().put(EnumMetric.AVERAGE, p.getVolume().get(EnumMetric.SUM) / users.size());
					}
				}
				return p;
			default:
				return null;
		}
	}
}
