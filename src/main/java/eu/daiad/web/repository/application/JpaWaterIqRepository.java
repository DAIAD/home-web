package eu.daiad.web.repository.application;

import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.daiad.web.domain.application.AccountEntity;
import eu.daiad.web.domain.application.WaterIqEntity;
import eu.daiad.web.domain.application.WaterIqHistoryEntity;
import eu.daiad.web.model.profile.ComparisonRanking;

/**
 * Provides methods for updating and querying user Water IQ status.
 */
@Repository
@Transactional("applicationTransactionManager")
public class JpaWaterIqRepository implements IWaterIqRepository {

    /**
     * Password reset token interval in hours.
     */
    @Value("${daiad.password.reset.token.duration}")
    private int passwordResetTokenDuration;

    /**
     *  Java Persistence entity manager.
     */
    @PersistenceContext(unitName = "default")
    EntityManager entityManager;

    /**
     * Deletes stale water IQ data.
     *
     * @param days number of dates after which water IQ data is considered stale.
     */
    @Override
    public void clean(int days) {
        DateTime createdOn = (new DateTime()).minusDays(days);

        String waterIqQueryString = "select iq from water_iq_history iq where iq.createdOn <= :createdOn";

        TypedQuery<WaterIqHistoryEntity> query = entityManager.createQuery(waterIqQueryString, WaterIqHistoryEntity.class);
        query.setParameter("createdOn", createdOn);

        for (WaterIqHistoryEntity record : query.getResultList()) {
            entityManager.remove(record);
        }

        entityManager.flush();
    }

    /**
     * Returns water IQ data for the user with the given id.
     *
     * @param userId the user id.
     * @return water IQ data.
     */
    @Override
    public ComparisonRanking getWaterIqByUserId(int userId) {
        String waterIqQueryString = "select iq from water_iq_history iq where iq.account.id = :id order by iq.id desc";

        TypedQuery<WaterIqHistoryEntity> query = entityManager.createQuery(waterIqQueryString, WaterIqHistoryEntity.class);
        query.setParameter("id", userId);

        List<WaterIqHistoryEntity> entries = query.getResultList();

        if(entries.isEmpty()) {
            return null;
        }

        // First entry is the current entry
        ComparisonRanking result = new ComparisonRanking();

        result.waterIq.nearest.value = entries.get(0).getNearestUserValue();
        result.waterIq.nearest.volume = entries.get(0).getNearestUserVolume();

        result.waterIq.similar.value = entries.get(0).getSimilarUserValue();
        result.waterIq.similar.volume= entries.get(0).getSimilarUserVolume();

        result.waterIq.all.value = entries.get(0).getAllUserValue();
        result.waterIq.all.volume = entries.get(0).getAllUserVolume();

        result.last1MonthConsumption.user = entries.get(0).getUserLast1MonthConsmution();
        result.last1MonthConsumption.similar = entries.get(0).getSimilarLast1MonthConsmution();
        result.last1MonthConsumption.nearest = entries.get(0).getNearestLast1MonthConsmution();
        result.last1MonthConsumption.all = entries.get(0).getAllLast1MonthConsmution();

        result.last6MonthConsumption.user = entries.get(0).getUserLast6MonthConsmution();
        result.last6MonthConsumption.similar = entries.get(0).getSimilarLast6MonthConsmution();
        result.last6MonthConsumption.nearest = entries.get(0).getNearestLast6MonthConsmution();
        result.last6MonthConsumption.all = entries.get(0).getAllLast6MonthConsmution();

        for (int i = 0, count = entries.size(); i < count; i++) {
            ComparisonRanking.WaterIqWithTimestamp entry = new ComparisonRanking.WaterIqWithTimestamp();
            entry.value = entries.get(i).getUserValue();
            entry.volume = entries.get(i).getUserVolume();
            entry.timestamp = entries.get(i).getCreatedOn().getMillis();
            entry.from = entries.get(i).getFrom();
            entry.to = entries.get(i).getTo();

            result.waterIq.user.add(entry);
        }
        return result;
    }

    /**
     * Update user Water IQ.
     *
     * @param userKey
     */
    @Override
    public void update(UUID userKey,
                       String from,
                       String to,
                       ComparisonRanking.WaterIq user,
                       ComparisonRanking.WaterIq similar,
                       ComparisonRanking.WaterIq nearest,
                       ComparisonRanking.WaterIq all,
                       ComparisonRanking.MonthlyConsumtpion last1Month,
                       ComparisonRanking.MonthlyConsumtpion last6Month) {
        DateTime updatedOn = new DateTime();

        // Get existing record
        WaterIqEntity waterIqEntity = null;
        boolean persist = false;

        String accountQueryString = "select a from account a left join fetch a.waterIq iq left join fetch a.waterIqHistory iq_history where a.key = :key";

        TypedQuery<AccountEntity> query = entityManager.createQuery(accountQueryString, AccountEntity.class);
        query.setParameter("key", userKey);

        AccountEntity account = query.getSingleResult();

        // Create record if not already exists
        if(account.getWaterIq() == null) {
            waterIqEntity = new WaterIqEntity();
            waterIqEntity.setAccount(account);

            persist = true;
        } else {
            waterIqEntity = account.getWaterIq();
        }

        waterIqEntity.setUpdatedOn(updatedOn);
        waterIqEntity.setFrom(from);
        waterIqEntity.setTo(to);
        waterIqEntity.setUserValue(user.value);
        waterIqEntity.setUserVolume(user.volume);
        if (similar != null) {
            waterIqEntity.setSimilarUserValue(similar.value);
            waterIqEntity.setSimilarUserVolume(similar.volume);
        }
        if (nearest != null) {
            waterIqEntity.setNearestUserValue(nearest.value);
            waterIqEntity.setNearestUserVolume(nearest.volume);
        }
        if (all != null) {
            waterIqEntity.setAllUserValue(all.value);
            waterIqEntity.setAllUserVolume(all.volume);
        }
        waterIqEntity.setUserLast1MonthConsmution(last1Month.user);
        waterIqEntity.setUserLast6MonthConsmution(last6Month.user);
        waterIqEntity.setSimilarLast1MonthConsmution(last1Month.similar);
        waterIqEntity.setSimilarLast6MonthConsmution(last6Month.similar);
        waterIqEntity.setNearestLast1MonthConsmution(last1Month.nearest);
        waterIqEntity.setNearestLast6MonthConsmution(last6Month.nearest);
        waterIqEntity.setAllLast1MonthConsmution(last1Month.all);
        waterIqEntity.setAllLast6MonthConsmution(last6Month.all);

        if(persist) {
            entityManager.persist(waterIqEntity);
        }

        entityManager.flush();

        // Update history
        WaterIqHistoryEntity waterIqHistoryEntity = null;
        persist = false;

        for (WaterIqHistoryEntity iq : account.getWaterIqHistory()) {
            if ((iq.getFrom().equals(from)) && (iq.getTo().equals(to))) {
                waterIqHistoryEntity = iq;
                break;
            }
        }

        if(waterIqHistoryEntity == null) {
            waterIqHistoryEntity = new WaterIqHistoryEntity();
            waterIqHistoryEntity.setAccount(account);

            persist = true;
        }

        waterIqHistoryEntity.setCreatedOn(updatedOn);
        waterIqHistoryEntity.setFrom(from);
        waterIqHistoryEntity.setTo(to);
        waterIqHistoryEntity.setUserValue(user.value);
        waterIqHistoryEntity.setUserVolume(user.volume);
        if (similar != null) {
            waterIqHistoryEntity.setSimilarUserValue(similar.value);
            waterIqHistoryEntity.setSimilarUserVolume(similar.volume);
        }
        if (nearest != null) {
            waterIqHistoryEntity.setNearestUserValue(nearest.value);
            waterIqHistoryEntity.setNearestUserVolume(nearest.volume);
        }
        if (all != null) {
            waterIqHistoryEntity.setAllUserValue(all.value);
            waterIqHistoryEntity.setAllUserVolume(all.volume);
        }

        waterIqHistoryEntity.setUserLast1MonthConsmution(last1Month.user);
        waterIqHistoryEntity.setUserLast6MonthConsmution(last6Month.user);
        waterIqHistoryEntity.setSimilarLast1MonthConsmution(last1Month.similar);
        waterIqHistoryEntity.setSimilarLast6MonthConsmution(last6Month.similar);
        waterIqHistoryEntity.setNearestLast1MonthConsmution(last1Month.nearest);
        waterIqHistoryEntity.setNearestLast6MonthConsmution(last6Month.nearest);
        waterIqHistoryEntity.setAllLast1MonthConsmution(last1Month.all);
        waterIqHistoryEntity.setAllLast6MonthConsmution(last6Month.all);

        if(persist) {
            entityManager.persist(waterIqHistoryEntity);
        }
    }
}
