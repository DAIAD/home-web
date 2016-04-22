
package eu.daiad.web.repository.application;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author nkarag
 */
@Repository
@Transactional("transactionManager")
@Scope("prototype")
public class AnnouncementRepository implements IAnnouncementRepository{
    
    @Override
    public void broadcastAnnouncement(int channel){

    }
}
