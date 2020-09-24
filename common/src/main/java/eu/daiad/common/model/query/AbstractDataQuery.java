package eu.daiad.common.model.query;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import eu.daiad.common.model.security.AuthenticatedUser;

public abstract class AbstractDataQuery {

    @JsonIgnore
    private String timezone;

    private boolean usingPreAggregation;

    @JsonIgnore
    private AuthenticatedUser executor;

    private TimeFilter time;

    private ArrayList<PopulationFilter> population = new ArrayList<PopulationFilter>();

    private ArrayList<SpatialFilter> spatial = new ArrayList<SpatialFilter>();

    public TimeFilter getTime() {
        return time;
    }

    public void setTime(TimeFilter time) {
        this.time = time;
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

    public boolean isUsingPreAggregation() {
        return usingPreAggregation;
    }

    public void setUsingPreAggregation(boolean usingPreAggregation) {
        this.usingPreAggregation = usingPreAggregation;
    }

    public AuthenticatedUser getExecutor() {
        return executor;
    }

    public void setExecutor(AuthenticatedUser executor) {
        this.executor = executor;
    }

}
