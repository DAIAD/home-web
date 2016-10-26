package eu.daiad.web.service.message.aggregates;

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
    }

    public void setTimestamp(DateTime timestamp)
    {
        this.timestamp = timestamp;
    }
    
    public void reset()
    {
        this.value = null;
        this.timestamp = null;
    }
    
    @Override
    public String toString()
    {
        return String.format("<ComputedNumber val=%.3f computed=\"%s\">",
                this.value, this.timestamp);
    }
}
