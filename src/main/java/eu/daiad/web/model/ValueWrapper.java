package eu.daiad.web.model;

public abstract class ValueWrapper <P>
{
    protected final P _value;
    
    protected ValueWrapper(P p)
    {
        this._value = p;
    }
    
    public P value()
    {
        return _value;
    }
}