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
	    super(EnumDataPointType.AMPHIRO);
	}

	public AmphiroDataPoint(long timestamp) {
		super(EnumDataPointType.AMPHIRO, timestamp);
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

	@Override
	public Map<EnumMetric, Double> field(EnumDataField field)
	{
	    Map<EnumMetric, Double> m = null;
	    switch (field) {
	    case VOLUME:
	        m = volume;
	        break;
	    case DURATION:
            m = duration;
            break;
        case ENERGY:
            m = energy;
            break;
        case FLOW:
            m = flow;
            break;
        case TEMPERATURE:
            m = temperature;
            break;
        default:
            m = volume;
            break;
        }
	    return m;
	}
}
