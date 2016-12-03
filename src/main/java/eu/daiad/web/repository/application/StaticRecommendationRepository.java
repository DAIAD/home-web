package eu.daiad.web.repository.application;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.daiad.web.domain.application.StaticRecommendationEntity;

@Repository 
@Transactional("applicationTransactionManager")
public class StaticRecommendationRepository implements IStaticRecommendationRepository
{
    @PersistenceContext(unitName = "default")
    EntityManager entityManager;
    
    @Override
    public StaticRecommendationEntity findOne(int recommendationId, Locale locale)
    {
        TypedQuery<StaticRecommendationEntity> query = entityManager.createQuery(
            "SELECT r FROM static_recommendation r " +
                "WHERE r.id = :rid AND r.locale = :locale",
            StaticRecommendationEntity.class);
        query.setParameter("rid", recommendationId);
        query.setParameter("locale", locale.getLanguage());
        
        StaticRecommendationEntity e;
        try {
            e = query.getSingleResult();
        } catch (NoResultException x) {
            // Note: Maybe we should retry with default locale?
            e = null;
        }
        return e;
    }

    @Override
    public StaticRecommendationEntity randomOne(Locale locale)
    {
        List<StaticRecommendationEntity> entities = random(locale, 1);
        return entities.isEmpty()? null : entities.get(0);
    }

    @Override
    public List<StaticRecommendationEntity> random(Locale locale, int size)
    {
        if (size < 1)
            throw new IllegalArgumentException("size must be a positive integer");
        
        TypedQuery<Integer> query = entityManager.createQuery(
            "SELECT r.id FROM static_recommendation r WHERE r.locale = :locale",
            Integer.class);
        query.setParameter("locale", locale.getLanguage());
        
        List<Integer> rids = query.getResultList();
        Collections.shuffle(rids);
        
        size = Math.min(size, rids.size());
        List<StaticRecommendationEntity> results = new ArrayList<>(size);
        for (Integer rid: rids.subList(0, size)) {
            results.add(entityManager.find(StaticRecommendationEntity.class, rid));
        }
        return results;
    }
}
