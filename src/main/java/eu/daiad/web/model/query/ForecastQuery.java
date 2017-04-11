package eu.daiad.web.model.query;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ForecastQuery extends AbstractDataQuery {

    @JsonIgnore
    private EnumMetric[] metrics = { EnumMetric.SUM, EnumMetric.COUNT };

    public ForecastQuery() {

    }

    public EnumMetric[] getMetrics() {
        return metrics;
    }

}
