package eu.daiad.web.model.message;

public class MessageResolutionStatus <P extends Message.Parameters>
{
    protected final double score; // in [0, 1]
    
    protected final P parameters;
    
    public static final double THRESHOLD = 0.5;
    
    public MessageResolutionStatus(P parameters, double score)
    {
        this.parameters = parameters;
        
        score = Math.abs(score);
        this.score = score > 1? 1.0 : score;
    }
    
    public MessageResolutionStatus(P parameters)
    {
        this(parameters, 1.0);
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
