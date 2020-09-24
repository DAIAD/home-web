package eu.daiad.common.model;

import org.joda.time.DateTime;

public class DateFormatter extends ValueWrapper<DateTime>
{
    private final String pattern;
    
    public DateFormatter(DateTime p)
    {
        this(p, "dd/MM/YYYY");
    }
    
    public DateFormatter(DateTime p, String pattern)
    {
        super(p);
        this.pattern = pattern;
    }
    
    @Override
    public String toString()
    {
        return _value.toString(pattern);
    }
}
