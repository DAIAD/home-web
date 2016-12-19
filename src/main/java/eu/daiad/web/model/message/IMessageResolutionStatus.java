package eu.daiad.web.model.message;

public interface IMessageResolutionStatus <P extends Message.Parameters>
{
    public boolean isSignificant();
    
    public double getScore();
    
    public P getParameters();
}
