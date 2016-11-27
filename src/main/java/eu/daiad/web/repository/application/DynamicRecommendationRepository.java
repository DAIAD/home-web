package eu.daiad.web.repository.application;

import java.util.Locale;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.daiad.web.domain.application.DynamicRecommendationEntity;
import eu.daiad.web.domain.application.DynamicRecommendationTranslationEntity;
import eu.daiad.web.model.message.EnumDynamicRecommendationType;

@Repository 
@Transactional("applicationTransactionManager")
public class DynamicRecommendationRepository implements IDynamicRecommendationRepository
{
    @PersistenceContext(unitName = "default")
    EntityManager entityManager;
    
    @Override
    public DynamicRecommendationEntity findOne(EnumDynamicRecommendationType recommendationType)
    {
        return entityManager.find(DynamicRecommendationEntity.class, recommendationType.getValue());
    }
}
