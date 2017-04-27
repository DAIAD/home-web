package eu.daiad.web.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

@JsonSerialize(using = ToStringSerializer.class)
public abstract class StringCode
{
    protected String code;
    
    protected StringCode(String code) 
    {
        this.code = code;
    }
    
    @Override
    public String toString()
    {
        return code;
    }
}
