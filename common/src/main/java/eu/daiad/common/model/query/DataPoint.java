package eu.daiad.common.model.query;

import java.util.Map;

public class DataPoint {

    public enum EnumDataPointType {
        UNDEFINED, METER, AMPHIRO, RANKING;
    }

    protected EnumDataPointType type;

    private Long timestamp = null;

    public DataPoint(EnumDataPointType type) {
        this.type = type;
    }

    public DataPoint(EnumDataPointType type, long timestamp) {
        this.type = type;
        this.timestamp = timestamp;
    }

    public EnumDataPointType getType() {
        return type;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public Map<EnumMetric, Double> field(EnumDataField field) {
        return null;
    }
}
