package eu.daiad.common.model.query;

import java.util.EnumMap;
import java.util.Map;

public class MeterDataPoint extends DataPoint {

    private Map<EnumMetric, Double> volume = new EnumMap<EnumMetric, Double>(EnumMetric.class);

    public void merge(MeterDataPoint p) {
        for(EnumMetric m : volume.keySet()) {
            switch(m) {
                case SUM:
                    volume.put(m, volume.get(m) + p.getVolume().get(m));
                    break;
                case MIN:
                    volume.put(m, volume.get(m) + p.getVolume().get(m));
                    break;
                case MAX:
                    break;
                case COUNT:
                    break;
                case AVERAGE:
                    break;
                default:
                    throw new IllegalArgumentException(String.format("EnumMetric member [%s] is not supported.", m));
            }
        }
    }

    public MeterDataPoint() {
        super(EnumDataPointType.METER);
    }

    public MeterDataPoint(long timestamp) {
        super(EnumDataPointType.METER, timestamp);
    }

    public Map<EnumMetric, Double> getVolume() {
        return volume;
    }

    @Override
    public Map<EnumMetric, Double> field(EnumDataField field) {
        return volume;
    }
}
