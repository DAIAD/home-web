package eu.daiad.web.model.message;

public class MessageResolutionStatus <P extends Message.Parameters>
    implements IMessageResolutionStatus<P>
{
    protected final double score; // in [0, 1]
    
    protected final P parameterizedMessage;
    
    public static final double THRESHOLD = 0.5;
    
    public MessageResolutionStatus(double score, P p)
    {
        this.parameterizedMessage = p;
        
        score = Math.abs(score);
        this.score = score > 1? 1.0 : score;
    }
    
    public MessageResolutionStatus(P p)
    {
        this(1.0, p);
    }
    
    public MessageResolutionStatus(boolean flag, P p)
    {
        this(flag? 1.0 : 0.0, p);
    }
    
    public MessageResolutionStatus(boolean flag)
    {
        this(flag, null);
    }

    public double getScore()
    {
        return score;
    }

    public P getMessage()
    {
        return parameterizedMessage;
    }
    
    public boolean isSignificant()
    {
        return (score > THRESHOLD);
    }
 
}
