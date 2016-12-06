package eu.daiad.web.model.query;

import java.util.EnumMap;
import java.util.Map;

public class AmphiroDataPoint extends DataPoint {

	private Map<EnumMetric, Double> volume = new EnumMap<EnumMetric, Double>(EnumMetric.class);

	private Map<EnumMetric, Double> duration = new EnumMap<EnumMetric, Double>(EnumMetric.class);

	private Map<EnumMetric, Double> temperature = new EnumMap<EnumMetric, Double>(EnumMetric.class);

	private Map<EnumMetric, Double> energy = new EnumMap<EnumMetric, Double>(EnumMetric.class);

	private Map<EnumMetric, Double> flow = new EnumMap<EnumMetric, Double>(EnumMetric.class);

	public AmphiroDataPoint() {
		this.type = EnumDataPointType.AMPHIRO;
	}

	public AmphiroDataPoint(long timestamp) {
		super(timestamp);
		this.type = EnumDataPointType.AMPHIRO;
	}

	public Map<EnumMetric, Double> getDuration() {
		return duration;
	}

	public Map<EnumMetric, Double> getTemperature() {
		return temperature;
	}

	public Map<EnumMetric, Double> getEnergy() {
		return energy;
	}

	public Map<EnumMetric, Double> getFlow() {
		return flow;
	}

	public Map<EnumMetric, Double> getVolume() {
		return volume;
	}

}
