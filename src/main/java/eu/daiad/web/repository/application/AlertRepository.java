package eu.daiad.web.repository.application;

import java.util.Locale;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.daiad.web.domain.application.AlertEntity;
import eu.daiad.web.domain.application.AlertTranslationEntity;
import eu.daiad.web.model.message.EnumAlertType;

@Repository 
@Transactional("applicationTransactionManager")
public class AlertRepository implements IAlertRepository
{
    @PersistenceContext(unitName = "default")
    EntityManager entityManager;
    
    @Override
    public AlertTranslationEntity findOne(EnumAlertType alertType, Locale locale)
    {
        TypedQuery<AlertTranslationEntity> query = entityManager.createQuery(
            "SELECT a FROM alert_translation a " +
                "WHERE a.alert.id = :aid AND a.locale = :locale",
            AlertTranslationEntity.class);
        query.setParameter("aid", alertType.getValue());
        query.setParameter("locale", locale.getLanguage());
        
        AlertTranslationEntity e;
        try {
            e = query.getSingleResult();
        } catch (NoResultException x) {
            // Note: or maybe retry with default locale
            e = null;
        }
        return e;
    }
}
