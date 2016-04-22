package eu.daiad.web.model.query;

import java.util.HashMap;
import java.util.Map;

public class MeterDataPoint extends DataPoint {

	private Map<EnumMetric, Double> volume = new HashMap<EnumMetric, Double>();

	public MeterDataPoint() {
		this.type = EnumDataPointType.METER;
	}

	public MeterDataPoint(long timestamp) {
		super(timestamp);
		this.type = EnumDataPointType.METER;
	}

	public Map<EnumMetric, Double> getVolume() {
		return volume;
	}

}
