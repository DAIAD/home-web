package eu.daiad.web.model.message;

public class SimpleMessageResolutionStatus <P extends Message.Parameters>
    implements MessageResolutionStatus<P>
{    
    protected final P parameterizedMessage;
    
    protected final EnumMessageLevel level;
    
    public SimpleMessageResolutionStatus(EnumMessageLevel level, P p)
    {
        this.parameterizedMessage = p;
        this.level = level;
    }
    
    public SimpleMessageResolutionStatus(P p)
    {
        this(true, p);
    }
    
    public SimpleMessageResolutionStatus(boolean flag, P p)
    {
        this(flag? EnumMessageLevel.NOTIFY : EnumMessageLevel.LOG, p);
    }
    
    public SimpleMessageResolutionStatus(boolean flag)
    {
        this(flag, null);
    }

    public Double getScore()
    {
        return (EnumMessageLevel.compare(level, EnumMessageLevel.NOTIFY) < 0)?
            SCORE_LOW_VALUE : SCORE_HIGH_VALUE;
    }

    public P getMessage()
    {
        return parameterizedMessage;
    }
    
    public boolean isSignificant()
    {
        return EnumMessageLevel.compare(EnumMessageLevel.NOTIFY, level) <= 0;
    }
 
}
