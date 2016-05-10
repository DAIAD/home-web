package eu.daiad.web.model.message;

import eu.daiad.web.model.AuthenticatedRequest;

public class MessageAcknowledgementRequest extends AuthenticatedRequest{
    
    private int alertId;
    
    public int getAlertId() {
        return alertId;
    }

    public void setAlertId(int alertId) {
        this.alertId = alertId;
    }
            
}
