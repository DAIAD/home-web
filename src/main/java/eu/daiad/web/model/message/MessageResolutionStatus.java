package eu.daiad.web.model.message;

public interface MessageResolutionStatus <P extends Message.Parameters>
{
    public boolean isSignificant();
    
    public double getScore();
    
    public P getMessage();
}
