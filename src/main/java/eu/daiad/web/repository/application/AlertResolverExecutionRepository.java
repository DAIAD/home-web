package eu.daiad.web.repository.application;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.daiad.web.domain.application.AlertResolverExecutionEntity;
import eu.daiad.web.domain.application.GroupEntity;
import eu.daiad.web.domain.application.UtilityEntity;
import eu.daiad.web.repository.BaseRepository;

@Repository
@Transactional("applicationTransactionManager")
public class AlertResolverExecutionRepository extends BaseRepository
    implements IAlertResolverExecutionRepository
{
    @PersistenceContext(unitName = "default")
    EntityManager entityManager;
    
    @Override
    public AlertResolverExecutionEntity findOne(int rid)
    {
        return entityManager.find(AlertResolverExecutionEntity.class, rid);
    }

    @Override
    public List<AlertResolverExecutionEntity> findByName(String resolverName)
    {
        return findByName(resolverName, (Interval) null);
    }

    @Override
    public List<AlertResolverExecutionEntity> findByName(String resolverName, Interval interval)
    {
        TypedQuery<AlertResolverExecutionEntity> q = entityManager.createQuery(
            "SELECT a FROM alert_resolver_execution a " +
                "WHERE a.resolver_name = :name AND a.finished IS NOT NULL" +
                    ((interval != null)? " AND a.ref_date >= :start AND a.ref_date <= :end" : "") + " " +
                "ORDER BY a.ref_date DESC",
            AlertResolverExecutionEntity.class);
        
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
            "SELECT a.id FROM alert_resolver_execution a " +
                "WHERE a.resolver_name = :name AND a.finished IS NOT NULL" +
                    ((interval != null)? " AND a.ref_date >= :start AND a.ref_date <= :end" : ""),
            Integer.class);
        
        q.setParameter("name", resolverName);
        if (interval != null) {
            q.setParameter("start", interval.getStart());
            q.setParameter("end", interval.getEnd());
        }
        
        return q.getResultList();
    }
    
    @Override
    public AlertResolverExecutionEntity create(AlertResolverExecutionEntity r)
    {
        entityManager.persist(r);
        return r;
    }

    @Override
    public AlertResolverExecutionEntity createWith(
        DateTime refDate, String resolverName, UtilityEntity target, DateTime started)
    {
        AlertResolverExecutionEntity r = 
            new AlertResolverExecutionEntity(refDate, resolverName, target);
        r.setStarted(started);
        return create(r);
    }

    @Override
    public AlertResolverExecutionEntity createWith(
        DateTime refDate, String resolverName, GroupEntity target, DateTime started)
    {
        AlertResolverExecutionEntity r = 
            new AlertResolverExecutionEntity(refDate, resolverName, target);
        r.setStarted(started);
        return create(r);
    }

    @Override
    public AlertResolverExecutionEntity updateFinished(AlertResolverExecutionEntity r, DateTime finished)
    {
        if (r != null) {
            if (!entityManager.contains(r))
                r = findOne(r.getId());
            r.setFinished(finished);
        }
        return r;
    }

    @Override
    public AlertResolverExecutionEntity updateFinished(int rid, DateTime finished)
    {
        AlertResolverExecutionEntity r = findOne(rid);
        if (r != null) {
            r.setFinished(finished);
        }
        return r;
    }
    
    @Override
    public void delete(int rid)
    {
        AlertResolverExecutionEntity r = findOne(rid);
        if (r != null)
            delete(r);
    }

    @Override
    public void delete(AlertResolverExecutionEntity r)
    {
       entityManager.remove(r);
    }
}
