package eu.daiad.web.model.query;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MeterUserDataPoint extends UserDataPoint {

	private Map<EnumMetric, Double> volume = new HashMap<EnumMetric, Double>();

	public MeterUserDataPoint(UUID key, String label) {
		super(key, label);
	}

	public Map<EnumMetric, Double> getVolume() {
		return volume;
	}
}
