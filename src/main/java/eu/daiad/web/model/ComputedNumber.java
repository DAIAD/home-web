package eu.daiad.web.model;

import org.joda.time.DateTime;
import org.springframework.util.Assert;

public class ComputedNumber extends Number
{
    private final Double value;
    
    private final DateTime timestamp;
    
    private ComputedNumber(Double value, DateTime timestamp)
    {
        this.value = value;
        this.timestamp = timestamp;
    }
    
    public Double getValue() 
    {
        return value;
    }

    public DateTime getTimestamp() 
    {
        return timestamp;
    }

    public static ComputedNumber valueOf(double value, DateTime t)
    {
        return new ComputedNumber(value, t);
    }
    
    public static ComputedNumber valueOf(double value)
    {
        return new ComputedNumber(value, DateTime.now());
    }
    
    public static final ComputedNumber UNDEFINED = new ComputedNumber(null, null);
    
    @Override
    public String toString()
    {
        if (this == UNDEFINED)
            return "undefined";
        else 
            return "ComputedNumber [value=" + value + ", timestamp=" + timestamp + "]";
    }

    @Override
    public int intValue()
    {
        Assert.state(value != null, "[Assertion failed] - Value is not set");
        return value.intValue();
    }

    @Override
    public long longValue()
    {
        Assert.state(value != null, "[Assertion failed] - Value is not set");
        return value.longValue();
    }

    @Override
    public float floatValue()
    {
        Assert.state(value != null, "[Assertion failed] - Value is not set");
        return value.floatValue();
    }

    @Override
    public double doubleValue()
    {
        Assert.state(value != null, "[Assertion failed] - Value is not set");
        return value.doubleValue();
    }
    
    private static final long serialVersionUID = 1L;
}
