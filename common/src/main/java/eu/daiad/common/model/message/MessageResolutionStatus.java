package eu.daiad.common.model.message;

public interface MessageResolutionStatus <P extends Message.Parameters>
{
    public static final double SCORE_LOW_VALUE = 0.0;
    
    public static final double SCORE_HIGH_VALUE = 1.0;
    
    public boolean isSignificant();
    
    public Double getScore();
    
    public P getMessage();
}
