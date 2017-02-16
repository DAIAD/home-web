package eu.daiad.web.repository.application;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.daiad.web.domain.application.GroupEntity;
import eu.daiad.web.domain.application.RecommendationResolverExecutionEntity;
import eu.daiad.web.domain.application.UtilityEntity;
import eu.daiad.web.repository.BaseRepository;

@Repository
@Transactional("applicationTransactionManager")
public class RecommendationResolverExecutionRepository extends BaseRepository
    implements IRecommendationResolverExecutionRepository
{
    @PersistenceContext(unitName = "default")
    EntityManager entityManager;
    
    @Override
    public RecommendationResolverExecutionEntity findOne(int rid)
    {
        return entityManager.find(RecommendationResolverExecutionEntity.class, rid);
    }

    @Override
    public List<RecommendationResolverExecutionEntity> findByName(String resolverName)
    {
        return findByName(resolverName, (Interval) null);
    }

    @Override
    public List<RecommendationResolverExecutionEntity> findByName(String resolverName, Interval interval)
    {
        TypedQuery<RecommendationResolverExecutionEntity> q = entityManager.createQuery(
            "SELECT a FROM recommendation_resolver_execution a " +
                "WHERE a.resolverName = :name AND a.finished IS NOT NULL" +
                    ((interval != null)? " AND a.refDate >= :start AND a.refDate < :end " : "") +
                "ORDER BY a.refDate DESC",
             RecommendationResolverExecutionEntity.class);
        
        q.setParameter("name", resolverName);
        if (interval != null) {
            q.setParameter("start", interval.getStart());
            q.setParameter("end", interval.getEnd());
        }
        
        return q.getResultList();
    }

    @Override
    public List<Integer> findIdByName(String resolverName)
    {
        return findIdByName(resolverName, (Interval) null);
    }

    @Override
    public List<Integer> findIdByName(String resolverName, Interval interval)
    {
        TypedQuery<Integer> q = entityManager.createQuery(
            "SELECT a.id FROM recommendation_resolver_execution a " +
                "WHERE a.resolverName = :name AND a.finished IS NOT NULL" +
                ((interval != null)? " AND a.refDate >= :start AND a.refDate < :end" : ""),
             Integer.class);
        
        q.setParameter("name", resolverName);
        if (interval != null) {
            q.setParameter("start", interval.getStart());
            q.setParameter("end", interval.getEnd());
        }
        
        return q.getResultList();
    }
    
    @Override
    public RecommendationResolverExecutionEntity create(RecommendationResolverExecutionEntity r)
    {
        entityManager.persist(r);
        return r;
    }

    @Override
    public RecommendationResolverExecutionEntity createWith(
        DateTime refDate, String resolverName, UtilityEntity target, DateTime started)
    {
        RecommendationResolverExecutionEntity r = 
            new RecommendationResolverExecutionEntity(refDate, resolverName, target);
        r.setStarted(started);
        return create(r);
    }

    @Override
    public RecommendationResolverExecutionEntity createWith(
        DateTime refDate, String resolverName, GroupEntity target, DateTime started)
    {
        RecommendationResolverExecutionEntity r = 
            new RecommendationResolverExecutionEntity(refDate, resolverName, target);
        r.setStarted(started);
        return create(r);
    }

    @Override
    public RecommendationResolverExecutionEntity updateFinished(RecommendationResolverExecutionEntity r, DateTime finished)
    {
        if (r != null) {
            if (!entityManager.contains(r))
                r = findOne(r.getId());
            r.setFinished(finished);
        }
        return r;
    }

    @Override
    public RecommendationResolverExecutionEntity updateFinished(int rid, DateTime finished)
    {
        RecommendationResolverExecutionEntity r = findOne(rid);
        if (r != null) {
            r.setFinished(finished);
        }
        return r;
    }

    @Override
    public void delete(int rid)
    {
        RecommendationResolverExecutionEntity r = findOne(rid);
        if (r != null)
            delete(r);
    }

    @Override
    public void delete(RecommendationResolverExecutionEntity r)
    {
        entityManager.remove(r);
    }
}
