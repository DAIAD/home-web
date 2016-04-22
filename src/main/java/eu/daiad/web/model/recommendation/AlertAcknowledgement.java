package eu.daiad.web.model.recommendation;

import eu.daiad.web.model.AuthenticatedRequest;

/**
 *
 * @author nkarag
 */
//@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
public class AlertAcknowledgement extends AuthenticatedRequest{
    
    private int alertId;
    
    public int getAlertId() {
        return alertId;
    }

    public void setAlertId(int alertId) {
        this.alertId = alertId;
    }
            
}
