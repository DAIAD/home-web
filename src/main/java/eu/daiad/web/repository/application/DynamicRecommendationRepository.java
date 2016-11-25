package eu.daiad.web.repository.application;

import java.util.Locale;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.daiad.web.domain.application.DynamicRecommendationTranslationEntity;
import eu.daiad.web.model.message.EnumDynamicRecommendationType;

@Repository 
@Transactional("applicationTransactionManager")
public class DynamicRecommendationRepository implements IDynamicRecommendationRepository
{
    @PersistenceContext(unitName = "default")
    EntityManager entityManager;
    
    @Override
    public DynamicRecommendationTranslationEntity findOne(
        EnumDynamicRecommendationType recommendationType, Locale locale)
    {
        TypedQuery<DynamicRecommendationTranslationEntity> query = entityManager.createQuery(
            "SELECT r FROM dynamic_recommendation r " +
                "WHERE r.recommendation.id = :rid AND r.locale = :locale",
            DynamicRecommendationTranslationEntity.class);
        query.setParameter("rid", recommendationType.getValue());
        query.setParameter("locale", locale.getLanguage());
        
        DynamicRecommendationTranslationEntity e;
        try {
            e = query.getSingleResult();
        } catch (NoResultException x) {
            // Note: or maybe retry with default locale
            e = null;
        }
        return e;
    }
}
