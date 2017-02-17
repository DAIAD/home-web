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

    public static ComputedNumber of(double value, DateTime t)
    {
        return new ComputedNumber(value, t);
    }
    
    public static ComputedNumber of(double value)
    {
        return new ComputedNumber(value, DateTime.now());
    }
    
    @Override
    public String toString()
    {
        return "ComputedNumber [value=" + value + ", timestamp=" + timestamp + "]";
    }
}
