package eu.daiad.web.repository.application;

import eu.daiad.web.domain.application.Account;
import eu.daiad.web.model.recommendation.MessageCalculationConfiguration;

/**
 *
 * @author nkarag
 */
public interface IMessageManagementRepository {
    
    public void execute(int groupID);
    public void execute(Account account);  
    public void execute(MessageCalculationConfiguration config);
    public void testMethodCreateMessagesForDummyUser(MessageCalculationConfiguration config);
    public void cancel();
    public boolean isCancelled();
    
}
