package eu.daiad.web.service;

import eu.daiad.web.model.recommendation.MessageCalculationConfiguration;

/**
 * 
 * @author nkarag
 */

public interface IMessageService {
    
    public abstract void execute(MessageCalculationConfiguration config);
       
    public abstract void cancel();

    public abstract boolean isCancelled();
    
}
