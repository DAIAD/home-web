package eu.daiad.web.model.message;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import eu.daiad.web.model.query.PopulationFilter;
import eu.daiad.web.model.query.TimeFilter;

public class MessageStatisticsQuery
{
    @JsonIgnore
    private String timezone;

    private TimeFilter time;

    private ArrayList<PopulationFilter> population = new ArrayList<>();

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

    public TimeFilter getTime() {
        return time;
    }

    public void setTime(TimeFilter time) {
        this.time = time;
    }

}
