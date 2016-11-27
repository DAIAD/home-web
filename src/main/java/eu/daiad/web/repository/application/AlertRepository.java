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
    public AlertEntity findOne(EnumAlertType alertType)
    {
        return entityManager.find(AlertEntity.class, alertType.getValue());
    }
}
