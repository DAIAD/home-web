
package eu.daiad.web.repository.application;

import eu.daiad.web.model.recommendation.Recommendation;
import java.util.List;
import org.joda.time.DateTime;

/**
 *
 * @author nkarag
 */
public interface IMessageQueryRepository {
       
    public List<Recommendation> getMessages(String username);   
    public List<Recommendation> testGetMessages();    
    public void messageAcknowledged(String username, String type, int alertId, DateTime acknowledgedOn);

}
