package eu.daiad.web.service;

import eu.daiad.web.model.query.MessageAggregatesContainer;
import eu.daiad.web.model.recommendation.MessageCalculationConfiguration;


/**
 *
 * @author nkarag
 */
public interface IAggregatesService {
    
    public abstract MessageAggregatesContainer execute(MessageCalculationConfiguration config);

    public void cancel();
    public boolean isCancelled();
}
