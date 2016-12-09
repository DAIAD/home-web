package eu.daiad.web.model.query;

import java.util.EnumMap;
import java.util.Map;

public class MeterDataPoint extends DataPoint {

	private Map<EnumMetric, Double> volume = new EnumMap<EnumMetric, Double>(EnumMetric.class);

	public MeterDataPoint() {
		super();
		this.type = EnumDataPointType.METER;
	}

	public MeterDataPoint(long timestamp) {
		super(timestamp);
		this.type = EnumDataPointType.METER;
	}

	public Map<EnumMetric, Double> getVolume() {
		return volume;
	}
	
	@Override
	public Map<EnumMetric, Double> field(EnumDataField field)
	{
	    return volume;
	}
}
