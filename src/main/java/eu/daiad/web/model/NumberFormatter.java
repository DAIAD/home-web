package eu.daiad.web.model;

import com.ibm.icu.text.DecimalFormat;

public class NumberFormatter extends ValueWrapper<Double>
{
    private final String pattern;
    
    public NumberFormatter(Double p)
    {
        this(p, ".##");
    }
    
    public NumberFormatter(Double p, String pattern)
    {
        super(p);
        this.pattern = pattern;
    }
    
    @Override
    public String toString()
    {
        return (new DecimalFormat(pattern)).format(_value);
    }
}