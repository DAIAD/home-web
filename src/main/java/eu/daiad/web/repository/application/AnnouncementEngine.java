
package eu.daiad.web.repository.application;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author nkarag
 */
@Repository
@Transactional("applicationTransactionManager")
@Scope("prototype")
public class AnnouncementEngine implements IAnnouncementEngine{
    
    @Override
    public void broadcastAnnouncement(int channel){

    }
}
