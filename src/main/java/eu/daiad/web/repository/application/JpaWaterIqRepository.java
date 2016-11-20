package eu.daiad.web.repository.application;

import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.daiad.web.domain.application.AccountEntity;
import eu.daiad.web.domain.application.WaterIqEntity;
import eu.daiad.web.domain.application.WaterIqHistoryEntity;
import eu.daiad.web.model.error.SharedErrorCode;
import eu.daiad.web.model.profile.ComparisonRanking;
import eu.daiad.web.model.profile.ComparisonRanking.DailyConsumption;
import eu.daiad.web.repository.BaseRepository;

// TODO : Move all data to HBase

/**
 * Provides methods for updating and querying user Water IQ status.
 */
@Repository("jpaWaterIqRepository")
@Transactional("applicationTransactionManager")
public class JpaWaterIqRepository extends BaseRepository implements IWaterIqRepository {

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
     * Repository for accessing water IQ data.
     */
    @Autowired
    @Qualifier("hBaseWaterIqRepository")
    private IWaterIqRepository hBaseWaterIqRepository;

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
     * Returns water IQ data for the user with the given key.
     *
     * @param key the user key.
     * @return water IQ data.
     */
    @Override
    public ComparisonRanking getWaterIqByUserKey(UUID key) {
        String waterIqQueryString = "select iq from water_iq_history iq where iq.account.key = :key order by iq.id desc";

        TypedQuery<WaterIqHistoryEntity> query = entityManager.createQuery(waterIqQueryString, WaterIqHistoryEntity.class)
                                                              .setFirstResult(0)
                                                              .setMaxResults(6);
        query.setParameter("key", key);

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

        for (int i = 0, count = entries.size(); i < count; i++) {
            ComparisonRanking.WaterIqWithTimestamp waterIq = new ComparisonRanking.WaterIqWithTimestamp();
            waterIq.value = entries.get(i).getUserValue();
            waterIq.volume = entries.get(i).getUserVolume();
            waterIq.timestamp = entries.get(i).getCreatedOn().getMillis();
            waterIq.from = entries.get(i).getFrom();
            waterIq.to = entries.get(i).getTo();

            result.waterIq.user.add(waterIq);

            int month = Integer.parseInt(entries.get(i).getFrom().substring(4, 6));

            ComparisonRanking.MonthlyConsumtpion monthlyConsumtpion = new ComparisonRanking.MonthlyConsumtpion(month);
            monthlyConsumtpion.user = entries.get(i).getUserLast1MonthConsmution();
            monthlyConsumtpion.similar = entries.get(i).getSimilarLast1MonthConsmution();
            monthlyConsumtpion.nearest = entries.get(i).getNearestLast1MonthConsmution();
            monthlyConsumtpion.all = entries.get(i).getAllLast1MonthConsmution();
            monthlyConsumtpion.from = entries.get(i).getFrom();
            monthlyConsumtpion.to = entries.get(i).getTo();

            result.monthlyConsumtpion.add(monthlyConsumtpion);

        }

        // Get daily consumption from HBase
        int year = Integer.parseInt(entries.get(0).getFrom().substring(0, 4));
        int month = Integer.parseInt(entries.get(0).getFrom().substring(4, 6));

        result.dailyConsumtpion = hBaseWaterIqRepository.getComparisonDailyConsumption(key, year, month);
        return result;
    }

    /**
     * Update user Water IQ.
     *
     * @param userKey the user key.
     * @param from time interval start date formatted using the pattern {@code yyyyMMdd}.
     * @param to time interval end date formatted using the pattern {@code yyyyMMdd}.
     * @param user water IQ data for a single user.
     * @param similar water IQ data for a group of similar users.
     * @param nearest water IQ data for the group of neighbors.
     * @param nearest water IQ data for all users.
     * @param monthlyConsumtpion monthly consumption data.
     * @param dailyConsumption daily consumption data.
     */
    @Override
    public void update(UUID userKey,
                       String from,
                       String to,
                       ComparisonRanking.WaterIq user,
                       ComparisonRanking.WaterIq similar,
                       ComparisonRanking.WaterIq nearest,
                       ComparisonRanking.WaterIq all,
                       ComparisonRanking.MonthlyConsumtpion monthlyConsumtpion,
                       List<ComparisonRanking.DailyConsumption> dailyConsumption) {
        DateTime updatedOn = new DateTime();

        // Get existing record
        WaterIqEntity waterIqEntity = null;
        boolean persist = false;

        String accountQueryString = "select a from account a left join fetch a.waterIq iq left join fetch a.waterIqHistory iq_history where a.key = :key";

        TypedQuery<AccountEntity> query = entityManager.createQuery(accountQueryString, AccountEntity.class);
        query.setParameter("key", userKey);

        AccountEntity account = query.getSingleResult();

        // Create or update water IQ
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
        waterIqEntity.setUserLast1MonthConsmution(monthlyConsumtpion.user);
        waterIqEntity.setSimilarLast1MonthConsmution(monthlyConsumtpion.similar);
        waterIqEntity.setNearestLast1MonthConsmution(monthlyConsumtpion.nearest);
        waterIqEntity.setAllLast1MonthConsmution(monthlyConsumtpion.all);

        if(persist) {
            entityManager.persist(waterIqEntity);
        }

        entityManager.flush();

        // Create or update water IQ history
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

        waterIqHistoryEntity.setUserLast1MonthConsmution(monthlyConsumtpion.user);
        waterIqHistoryEntity.setSimilarLast1MonthConsmution(monthlyConsumtpion.similar);
        waterIqHistoryEntity.setNearestLast1MonthConsmution(monthlyConsumtpion.nearest);
        waterIqHistoryEntity.setAllLast1MonthConsmution(monthlyConsumtpion.all);

        if(persist) {
            entityManager.persist(waterIqHistoryEntity);
        }

        // Store daily consumption
        hBaseWaterIqRepository.update(userKey, from, to, user, similar, nearest, all, monthlyConsumtpion, dailyConsumption);
    }

    /**
     * Returns the daily consumption for the given key for the selected year and month.
     *
     * @param userKey the user key.
     * @param year the year.
     * @param month the month.
     * @return a list of {@link ComparisonRanking.DailyConsumption}.
     */
    @Override
    public List<DailyConsumption> getComparisonDailyConsumption(UUID userKey, int year, int month) {
        throw createApplicationException(SharedErrorCode.NOT_IMPLEMENTED);
    }
}
