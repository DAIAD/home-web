package eu.daiad.web.model;

import org.joda.time.DateTime;

public class ComputedNumber
{
    private Double value;
    
    private DateTime timestamp;
    
    public ComputedNumber(Double value, DateTime timestamp)
    {
        this.value = value;
        this.timestamp = timestamp;
    }
    
    public ComputedNumber(Double value)
    {
        this.value = value;
        this.timestamp = DateTime.now();
    }
    
    public ComputedNumber()
    {
        this.value = null;
        this.timestamp = null;
    }
    
    public Double getValue() 
    {
        return value;
    }

    public DateTime getTimestamp() 
    {
        return timestamp;
    }

    public void setValue(Double value)
    {
        this.value = value;
        this.timestamp = DateTime.now();
    }
    
    public void reset()
    {
        this.value = null;
        this.timestamp = null;
    }
    
    @Override
    public String toString()
    {
        if (value != null)
            return String.format("<ComputedNumber value=%.3f at=\"%s\">", 
                    value, timestamp);
        else
            return "<ComputedNumber value=NULL>";
    }
}
