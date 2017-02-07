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


import eu.daiad.web.domain.application.ConsumptionStatsEntity;
import eu.daiad.web.domain.application.GroupEntity;
import eu.daiad.web.domain.application.UtilityEntity;
import eu.daiad.web.model.ComputedNumber;
import eu.daiad.web.model.ConsumptionStats;
import eu.daiad.web.model.utility.UtilityInfo;

@Repository
@Transactional("applicationTransactionManager")
public class ConsumptionStatsRepository implements IConsumptionStatsRepository
{
    private static final Log logger = LogFactory.getLog(ConsumptionStatsRepository.class);
    
    @PersistenceContext(unitName = "default")
    EntityManager entityManager;
    
    private UtilityEntity getUtility(UUID key)
    {
        TypedQuery<UtilityEntity> query = entityManager.createQuery(
                "SELECT u FROM utility u WHERE u.key = :key",
                eu.daiad.web.domain.application.UtilityEntity.class
        );
        query.setParameter("key", key);
        
        UtilityEntity u;
        try {
            u = query.getSingleResult();
        } catch (NoResultException e) {
            u = null;
        } 
        return u;
    }
    
    private GroupEntity getGroup(UUID key)
    {
        TypedQuery<GroupEntity> query = entityManager.createQuery(
            "SELECT g FROM group g WHERE g.key = :key", GroupEntity.class);
        query.setParameter("key", key);
        
        GroupEntity g;
        try {
            g = query.getSingleResult();
        } catch (NoResultException e) {
            g = null;
        }
        return g;
    }
    
    private List<eu.daiad.web.domain.application.ConsumptionStatsEntity> findByGroupAndDate(
        UtilityEntity utility, GroupEntity group, LocalDateTime refDate)
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
        
        TypedQuery<ConsumptionStatsEntity> query = entityManager.createQuery(
                hqlString, ConsumptionStatsEntity.class);
        
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
        logger.info("Fetching stats for utility " + utilityKey + " at " + refDate);
        
        eu.daiad.web.domain.application.UtilityEntity utility = getUtility(utilityKey);
        if (utility == null) {
            throw new IllegalArgumentException("No such utility: " + utilityKey);
        }
        
        GroupEntity group = groupKey == null? null : getGroup(groupKey);
        
        ConsumptionStats stats = null;
        for (ConsumptionStatsEntity e: findByGroupAndDate(utility, group, refDate)) {
            if (stats == null)
                stats = new ConsumptionStats();
            stats.set(e.getStatistic(), e.getDevice(), e.getField(), e.getValue());
        }

        return stats;
    }

    @Override
    public void save(UUID utilityKey, UUID groupKey, LocalDateTime refDate, ConsumptionStats stats)
    {
        logger.info("Saving stats for utility " + utilityKey + " at " + refDate);
        
        UtilityEntity utility = getUtility(utilityKey);
        if (utility == null) {
            throw new IllegalArgumentException("No such utility: " + utilityKey);
        }
        
        GroupEntity group = groupKey == null? null : getGroup(groupKey);
        
        // Prepare a set of (initially detached) entities
        ArrayList<ConsumptionStatsEntity> entities = new ArrayList<>(16);
        for (ConsumptionStats.Key key: stats) {
            ConsumptionStatsEntity e = new ConsumptionStatsEntity(utility, group, refDate);
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
        for (ConsumptionStatsEntity e: findByGroupAndDate(utility, group, refDate))
            entityManager.remove(e);
        
        // Make previous delete visible in present transaction (otherwise unique constraints are violated)
        entityManager.flush();
        
        // Persist (insert) newly created entities
        for (ConsumptionStatsEntity e: entities)
            entityManager.persist(e);
        
        return;
    }
}
