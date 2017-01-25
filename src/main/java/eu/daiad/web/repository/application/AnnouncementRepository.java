package eu.daiad.web.repository.application;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.daiad.web.domain.application.AnnouncementEntity;

@Repository
@Transactional("applicationTransactionManager")
public class AnnouncementRepository
    implements IAnnouncementRepository
{
    @PersistenceContext(unitName = "default")
    EntityManager entityManager;

    @Override
    public AnnouncementEntity findOne(int announcementId)
    {
        return entityManager.find(AnnouncementEntity.class, announcementId);
    }
}
