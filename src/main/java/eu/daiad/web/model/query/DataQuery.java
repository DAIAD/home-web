package eu.daiad.web.model.query;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class DataQuery {

    @JsonIgnore
    private String timezone;

    private TimeFilter time;

    private ArrayList<PopulationFilter> population = new ArrayList<PopulationFilter>();

    private ArrayList<SpatialFilter> spatial = new ArrayList<SpatialFilter>();

    @JsonDeserialize(using = EnumMeasurementDataSource.Deserializer.class)
    private EnumMeasurementDataSource source = EnumMeasurementDataSource.NONE;

    private EnumMetric[] metrics = {};

    public TimeFilter getTime() {
        return time;
    }

    public void setTime(TimeFilter time) {
        this.time = time;
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

    public ArrayList<PopulationFilter> getPopulation() {
        return population;
    }

    @JsonIgnore
    public String getTimezone() {
        return timezone;
    }

    @JsonProperty
    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public ArrayList<SpatialFilter> getSpatial() {
        return spatial;
    }

}
