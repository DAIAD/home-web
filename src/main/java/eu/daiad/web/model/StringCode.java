package eu.daiad.web.model;

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
