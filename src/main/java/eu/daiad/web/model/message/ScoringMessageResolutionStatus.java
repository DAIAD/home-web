package eu.daiad.web.model.message;

public class ScoringMessageResolutionStatus <P extends Message.Parameters> 
    implements MessageResolutionStatus<P>
{
    protected final double score;
    
    protected final P parameterizedMessage;
    
    public static final double SCORE_THRESHOLD = 0.5;
    
    public ScoringMessageResolutionStatus(double score, P p)
    {
        this.parameterizedMessage = p;
        
        score = Math.abs(score);
        this.score = score > SCORE_HIGH_VALUE? 
            SCORE_HIGH_VALUE : (score < SCORE_LOW_VALUE? SCORE_LOW_VALUE : score);
    }
    
    public ScoringMessageResolutionStatus(P p)
    {
        this(SCORE_HIGH_VALUE, p);
    }

    public Double getScore()
    {
        return score;
    }

    public P getMessage()
    {
        return parameterizedMessage;
    }
    
    public boolean isSignificant()
    {
        return (score > SCORE_THRESHOLD);
    }
}
