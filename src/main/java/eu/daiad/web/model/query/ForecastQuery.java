package eu.daiad.web.model.query;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ForecastQuery {

    @JsonIgnore
    private String timezone;

    private TimeFilter time;

    private ArrayList<PopulationFilter> population = new ArrayList<PopulationFilter>();

    private ArrayList<SpatialFilter> spatial = new ArrayList<SpatialFilter>();

    private EnumMetric[] metrics = { EnumMetric.SUM, EnumMetric.COUNT };

    public TimeFilter getTime() {
        return time;
    }

    public void setTime(TimeFilter time) {
        this.time = time;
    }

    public EnumMeasurementDataSource getSource() {
        return EnumMeasurementDataSource.METER;
    }

    public EnumMetric[] getMetrics() {
        return metrics;
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
