package eu.daiad.web.repository.application;

import java.util.Locale;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.daiad.web.domain.application.AnnouncementEntity;
import eu.daiad.web.domain.application.AnnouncementTranslationEntity;

@Repository 
@Transactional("applicationTransactionManager")
public class AnnouncementRepository implements IAnnouncementRepository
{
    @PersistenceContext(unitName = "default")
    EntityManager entityManager;
    
    @Override
    public AnnouncementTranslationEntity findOne(int announcementId, Locale locale)
    {
        TypedQuery<AnnouncementTranslationEntity> query = entityManager.createQuery(
            "SELECT a FROM announcement_translation a " +
                "WHERE a.announcement.id = :aid AND a.locale = :locale",
            AnnouncementTranslationEntity.class);
        query.setParameter("aid", announcementId);
        query.setParameter("locale", locale.getLanguage());
        
        AnnouncementTranslationEntity e;
        try {
            e = query.getSingleResult();
        } catch (NoResultException x) {
            // Note: or maybe retry with default locale
            e = null;
        }
        return e;
    }
}
