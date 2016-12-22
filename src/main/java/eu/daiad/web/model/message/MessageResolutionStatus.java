package eu.daiad.web.model.message;

public class MessageResolutionStatus <P extends Message.Parameters>
    implements IMessageResolutionStatus<P>
{
    protected final double score; // in [0, 1]
    
    protected final P parameters;
    
    public static final double THRESHOLD = 0.5;
    
    public MessageResolutionStatus(double score, P parameters)
    {
        this.parameters = parameters;
        
        score = Math.abs(score);
        this.score = score > 1? 1.0 : score;
    }
    
    public MessageResolutionStatus(P parameters)
    {
        this(1.0, parameters);
    }
    
    public MessageResolutionStatus(boolean flag, P parameters)
    {
        this(flag? 1.0 : 0.0, parameters);
    }
    
    public MessageResolutionStatus(boolean flag)
    {
        this(flag, null);
    }

    public double getScore()
    {
        return score;
    }

    public P getParameters()
    {
        return parameters;
    }
    
    public boolean isSignificant()
    {
        return (score > THRESHOLD);
    }
 
}
