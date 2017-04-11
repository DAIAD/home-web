package eu.daiad.web.model.query;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class DataQuery extends AbstractDataQuery {

    @JsonDeserialize(using = EnumMeasurementDataSource.Deserializer.class)
    private EnumMeasurementDataSource source = EnumMeasurementDataSource.NONE;

    private EnumMetric[] metrics = {};

    public DataQuery() {

    }

    public EnumMeasurementDataSource getSource() {
        return source;
    }

    public void setSource(EnumMeasurementDataSource source) {
        this.source = source;
    }

    public EnumMetric[] getMetrics() {
        return metrics;
    }

    public void setMetrics(EnumMetric[] metrics) {
        this.metrics = metrics;
    }

}
