package eu.daiad.web.service.message.aggregates;

import org.joda.time.DateTime;

public class AverageMonthlyAmphiro {
    
    private Double value;
    
    private DateTime lastComputed;
    
    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public DateTime getLastComputed() {
        return lastComputed;
    }

    public void setLastComputed(DateTime lastComputed) {
        this.lastComputed = lastComputed;
    }     
}
