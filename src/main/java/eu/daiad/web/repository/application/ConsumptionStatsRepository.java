package eu.daiad.web.repository.application;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.daiad.web.model.ConsumptionStats;
import eu.daiad.web.model.utility.UtilityInfo;
import eu.daiad.web.service.message.aggregates.ComputedNumber;

@Repository
@Transactional("applicationTransactionManager")
public class ConsumptionStatsRepository implements IConsumptionStatsRepository
{
    private static final Log logger = LogFactory.getLog(ConsumptionStatsRepository.class);
    
    @PersistenceContext(unitName = "default")
    EntityManager entityManager;
    
    private eu.daiad.web.domain.application.Utility getUtility(UUID key)
    {
        TypedQuery<eu.daiad.web.domain.application.Utility> query = entityManager.createQuery(
                "SELECT u FROM utility u WHERE u.key = :key",
                eu.daiad.web.domain.application.Utility.class
        );
        query.setParameter("key", key);
        
        eu.daiad.web.domain.application.Utility u;
        try {
            u = query.getSingleResult();
        } catch (NoResultException e) {
            u = null;
        } 
        return u;
    }
    
    private eu.daiad.web.domain.application.Group getGroup(UUID key)
    {
        TypedQuery<eu.daiad.web.domain.application.Group> query = entityManager.createQuery(
                "SELECT g FROM group g WHERE g.key = :key",
                eu.daiad.web.domain.application.Group.class
        );
        query.setParameter("key", key);
        
        eu.daiad.web.domain.application.Group g;
        try {
            g = query.getSingleResult();
        } catch (NoResultException e) {
            g = null;
        }
        return g;
    }
    
    private List<eu.daiad.web.domain.application.ConsumptionStats> findByGroupAndDate(
            eu.daiad.web.domain.application.Utility utility, 
            eu.daiad.web.domain.application.Group group,
            LocalDateTime refDate)
    { 
        DateTimeZone tz = DateTimeZone.forID(utility.getTimezone());    
        DateTime refdate0 = refDate.toDateTime(tz).minusHours(12);
        DateTime refdate1 = refDate.toDateTime(tz).plusHours(12);
        
        String hqlString = 
            "SELECT s FROM consumption_stats s " +
            "WHERE " +
                "s.utility.key = :utilityKey AND " +
                "s.refDate > :refdate0 AND " +
                "s.refDate < :refdate1 AND " +
                ((group == null)? "s.group is NULL" : "s.group.key = :groupKey")
        ;
        
        TypedQuery<eu.daiad.web.domain.application.ConsumptionStats> query = entityManager.createQuery(
                hqlString, eu.daiad.web.domain.application.ConsumptionStats.class);
        
        query.setParameter("utilityKey", utility.getKey());
        query.setParameter("refdate0", refdate0);
        query.setParameter("refdate1", refdate1);
        if (group != null)
            query.setParameter("groupKey", group.getKey());
        
        return query.getResultList();
    }
    
    @Override
    public ConsumptionStats get(UUID utilityKey, UUID groupKey, LocalDateTime refDate)
    {
        logger.info("Fetching stats for utility " + utilityKey + " at " + refDate + " ...");
        
        eu.daiad.web.domain.application.Utility utility = getUtility(utilityKey);
        if (utility == null) {
            throw new IllegalArgumentException("No such utility: " + utilityKey);
        }
        
        eu.daiad.web.domain.application.Group group = groupKey == null? null : getGroup(groupKey);
        
        ConsumptionStats stats = null;
        for (eu.daiad.web.domain.application.ConsumptionStats e: findByGroupAndDate(utility, group, refDate)) {
            if (stats == null)
                stats = new ConsumptionStats();
            stats.set(e.getStatistic(), e.getDevice(), e.getField(), e.getValue());
        }

        return stats;
    }

    @Override
    public void save(UUID utilityKey, UUID groupKey, LocalDateTime refDate, ConsumptionStats stats)
    {
        logger.info("Saving stats for utility " + utilityKey + " at " + refDate + " ...");
        
        eu.daiad.web.domain.application.Utility utility = getUtility(utilityKey);
        if (utility == null) {
            throw new IllegalArgumentException("No such utility: " + utilityKey);
        }
        
        eu.daiad.web.domain.application.Group group = groupKey == null? null : getGroup(groupKey);
        
        // Prepare a set of (initially detached) entities
        ArrayList<eu.daiad.web.domain.application.ConsumptionStats> entities = new ArrayList<>(16);
        for (ConsumptionStats.Key key: stats) {
            eu.daiad.web.domain.application.ConsumptionStats e = 
                    new eu.daiad.web.domain.application.ConsumptionStats(utility, group, refDate);
            ComputedNumber val = stats.get(key);
            if (val == null)
                continue;
            e.setDevice(key.getDevice());
            e.setField(key.getField());
            e.setStatistic(key.getStatistic());
            e.setValue(val);
            entities.add(e);
        }
        
        // Delete any existing entities on the same target (utility, group, ref-date)
        for (eu.daiad.web.domain.application.ConsumptionStats e: findByGroupAndDate(utility, group, refDate))
            entityManager.remove(e);
        
        // Make previous delete visible in present transaction (otherwise unique constraints are violated)
        entityManager.flush();
        
        // Persist (insert) newly created entities
        for (eu.daiad.web.domain.application.ConsumptionStats e: entities)
            entityManager.persist(e);
        
        return;
    }
}
