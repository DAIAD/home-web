package eu.daiad.web.repository.application;

import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.daiad.web.domain.application.GroupEntity;
import eu.daiad.web.domain.application.UtilityEntity;
import eu.daiad.web.domain.application.UtilityStatisticsEntity;
import eu.daiad.web.model.ComputedNumber;
import eu.daiad.web.model.EnumStatistic;
import eu.daiad.web.model.group.Group;
import eu.daiad.web.model.query.EnumMeasurementField;

@Repository
@Transactional
public class UtilityStatisticsRepository
    implements IUtilityStatisticsRepository
{
    private static class PopulationGroup
    {
        private Integer utilityId;

        private Integer groupId;

        private PopulationGroup() {}

        public Integer getUtilityId()
        {
            return utilityId;
        }

        public Integer getGroupId()
        {
            return groupId;
        }

        public static PopulationGroup of(int utilityId)
        {
            PopulationGroup p = new PopulationGroup();
            p.utilityId = utilityId;
            p.groupId = null;
            return p;
        }

        public static PopulationGroup of(int utilityId, int groupId)
        {
            PopulationGroup p = new PopulationGroup();
            p.utilityId = utilityId;
            p.groupId = groupId;
            return p;
        }
    }

    @PersistenceContext
    private EntityManager entityManager;

    private PopulationGroup resolveGroup(UUID groupKey)
    {
        Assert.state(groupKey != null);

        TypedQuery<GroupEntity> q = entityManager.createQuery(
            "FROM group g WHERE g.key = :key", GroupEntity.class);
        q.setParameter("key", groupKey);

        GroupEntity g;
        try {
            g = q.getSingleResult();
        } catch (NoResultException x) {
            throw new IllegalArgumentException("No such group: " + groupKey);
        }

        UtilityEntity u = g.getUtility();
        return PopulationGroup.of(u.getId(), g.getId());
    }

    private PopulationGroup resolveUtility(UUID utilityKey)
    {
        Assert.state(utilityKey != null);

        TypedQuery<Integer> q = entityManager.createQuery(
            "SELECT u.id FROM utility u WHERE u.key = :key", Integer.class);
        q.setParameter("key", utilityKey);

        int utilityId;
        try {
            utilityId = q.getSingleResult();
        } catch (NoResultException x) {
           throw new IllegalArgumentException("No such utility: " + utilityKey);
        }
        return PopulationGroup.of(utilityId);
    }

    private TypedQuery<UtilityStatisticsEntity> buildQuery(
        PopulationGroup g, DateTime refDate, Period period, EnumMeasurementField field, EnumStatistic statistic)
    {
        Assert.state(g != null && g.getUtilityId() > 0);
        Assert.state(refDate != null);

        refDate = refDate.withTimeAtStartOfDay();

        Integer utilityId = g.getUtilityId();
        Integer groupId = g.getGroupId();

        String hqlString = "SELECT s FROM utility_statistics s WHERE " +
            "s.utility.id = :utilityId" +
            ((groupId != null)? " AND s.group.id = :groupId" : "") +
            " AND s.refDate = :refDate" +
            ((period != null)? " AND s.period = :period" : "") +
            ((field != null)? " AND s.deviceType = :deviceType AND s.field = :field" : "") +
            ((statistic != null)? " AND s.statistic = :statistic" : "");

        TypedQuery<UtilityStatisticsEntity> query =
            entityManager.createQuery(hqlString, UtilityStatisticsEntity.class);

        query.setParameter("utilityId", utilityId);
        if (groupId != null)
            query.setParameter("groupId", groupId);

        query.setParameter("refDate", refDate);
        if (period != null)
            query.setParameter("period", period);
        if (field != null) {
            query.setParameter("deviceType", field.getDeviceType());
            query.setParameter("field", field.getField());
        }
        if (statistic != null)
            query.setParameter("statistic", statistic);

        return query;
    }

    @Override
    public List<UtilityStatisticsEntity> findBy(UUID utilityKey, DateTime refDate)
    {
        return findBy(utilityKey, refDate, (Period) null, null);
    }

    @Override
    public List<UtilityStatisticsEntity> findBy(Group g, DateTime refDate)
    {
        return findBy(g, refDate, (Period) null, null);
    }

    @Override
    public List<UtilityStatisticsEntity> findBy(UUID utilityKey, DateTime refDate, Period period)
    {
        return findBy(utilityKey, refDate, period, null);
    }

    @Override
    public List<UtilityStatisticsEntity> findBy(Group g, DateTime refDate, Period period)
    {
        return findBy(g, refDate, period, null);
    }

    @Override
    public List<UtilityStatisticsEntity> findBy(
        UUID utilityKey, DateTime refDate, Period period, EnumMeasurementField field)
    {
        Assert.notNull(utilityKey, "Expected a non-null utility identifier");
        return findBy(resolveUtility(utilityKey), refDate, period, field);
    }

    @Override
    public List<UtilityStatisticsEntity> findBy(
        Group g, DateTime refDate, Period period, EnumMeasurementField field)
    {
        Assert.notNull(g.getKey(), "Expected a non-null group identifier)");
        return findBy(resolveUtility(g.getKey()), refDate, period, field);
    }

    private List<UtilityStatisticsEntity> findBy(
        PopulationGroup g, DateTime refDate, Period period, EnumMeasurementField field)
    {
        TypedQuery<UtilityStatisticsEntity> q = buildQuery(g, refDate, period, field, null);
        return q.getResultList();
    }

    @Override
    public UtilityStatisticsEntity findOne(
        UUID utilityKey, DateTime refDate, Period period, EnumMeasurementField field, EnumStatistic statistic)
    {
        Assert.notNull(utilityKey, "Expected a non-null utility identifier");
        return findOne(resolveUtility(utilityKey), refDate, period, field, statistic);
    }

    @Override
    public UtilityStatisticsEntity findOne(
        Group g, DateTime refDate, Period period, EnumMeasurementField field, EnumStatistic statistic)
    {
        Assert.notNull(g.getKey(), "Expected a non-null group identifier)");
        return findOne(resolveGroup(g.getKey()), refDate, period, field, statistic);
    }

    private UtilityStatisticsEntity findOne(
        PopulationGroup g, DateTime refDate, Period period, EnumMeasurementField field, EnumStatistic statistic)
    {
        TypedQuery<UtilityStatisticsEntity> q = buildQuery(g, refDate, period, field, statistic);
        q.setMaxResults(1);

        UtilityStatisticsEntity r;
        try {
            r = q.getSingleResult();
        } catch (NoResultException x) {
            r = null;
        }
        return r;
    }

    @Override
    public UtilityStatisticsEntity save(
        UUID utilityKey, DateTime refDate, Period period, EnumMeasurementField field, EnumStatistic statistic,
        ComputedNumber n)
    {
        Assert.notNull(utilityKey, "Expected a non-null utility identifier");
        return save(resolveUtility(utilityKey), refDate, period, field, statistic, n);
    }

    @Override
    public UtilityStatisticsEntity save(
        Group g, DateTime refDate, Period period, EnumMeasurementField field, EnumStatistic statistic,
        ComputedNumber n)
    {
        Assert.notNull(g.getKey(), "Expected a non-null group identifier)");
        return save(resolveGroup(g.getKey()), refDate, period, field, statistic, n);
    }

    private UtilityStatisticsEntity save(
        PopulationGroup g, DateTime refDate, Period period, EnumMeasurementField field, EnumStatistic statistic,
        ComputedNumber n)
    {
        Assert.state(refDate != null && period != null,
            "A reference-date and period is required");
        Assert.state(field != null && statistic != null,
            "A measurement field and a statistic is required");
        Assert.state(n != null && n.getValue() != null,
            "Expected a non-empty computed number");

        refDate = refDate.withTimeAtStartOfDay();

        Integer utilityId = g.getUtilityId();
        Integer groupId = g.getGroupId();

        UtilityStatisticsEntity r = findOne(g, refDate, period, field, statistic);
        if (r == null) {
            // Create a new entity to be persisted
            UtilityEntity utility =
                entityManager.find(UtilityEntity.class, utilityId);
            GroupEntity group = (groupId != null)?
                entityManager.find(GroupEntity.class, groupId) : null;
            r = new UtilityStatisticsEntity(utility, group, refDate);
            r.setPeriod(period);
            r.setDeviceType(field.getDeviceType());
            r.setField(field.getField());
            r.setStatistic(statistic);
            r.setValue(n.getValue());
            r.setComputedAt(n.getTimestamp());
            entityManager.persist(r);
        } else {
            // Update value and associated timestamp
            r.setValue(n.getValue());
            r.setComputedAt(n.getTimestamp());
        }

        return r;
    }

    @Override
    public void delete(UtilityStatisticsEntity e)
    {
        if (e != null)
            entityManager.remove(e);
    }

}
