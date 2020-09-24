package eu.daiad.common.repository.application;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.daiad.common.domain.application.AccountEntity;
import eu.daiad.common.domain.application.WaterIqHistoryEntity;
import eu.daiad.common.domain.application.mappings.SavingsPotentialWaterIqMappingEntity;
import eu.daiad.common.model.error.SharedErrorCode;
import eu.daiad.common.model.profile.ComparisonRanking;
import eu.daiad.common.model.profile.ComparisonRanking.DailyConsumption;
import eu.daiad.common.repository.BaseRepository;

//TODO : Move all data to HBase

/**
* Provides methods for updating and querying user Water IQ status.
*/
@Repository("jpaWaterIqRepository")
@Transactional
public class JpaWaterIqRepository extends BaseRepository implements IWaterIqRepository {

    /**
     *  Java Persistence entity manager.
     */
    @PersistenceContext
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
     * @param year reference year.
     * @param month reference month.
     * @return water IQ data.
     */
    @Override
    public ComparisonRanking getWaterIqByUserKey(UUID key, int year, int month) {
        String waterIqQueryString = "select     iq " +
                                    "from       water_iq_history iq " +
                                    "where      (iq.account.key = :key) and " +
                                    "           ((iq.year < :maxYear) or (iq.year = :maxYear and iq.month <= :maxMonth)) and " +
                                    "           ((iq.year > :minYear) or (iq.year = :minYear and iq.month >= :minMonth)) " +
                                    "order by   iq.year desc, iq.month desc";

        TypedQuery<WaterIqHistoryEntity> query = entityManager.createQuery(waterIqQueryString, WaterIqHistoryEntity.class)
                                                              .setFirstResult(0)
                                                              .setMaxResults(6);

        query.setParameter("key", key);
        query.setParameter("maxYear", year);
        query.setParameter("maxMonth", month);
        query.setParameter("minYear", (month < 6 ? (year - 1) : year));
        query.setParameter("minMonth", ((month - 5) > 0 ? (month - 5) : (month + 7)));

        List<WaterIqHistoryEntity> entries = query.getResultList();

        if(entries.isEmpty()) {
            return null;
        }

        // First entry is the current entry
        ComparisonRanking result = new ComparisonRanking();

        for (int i = 0, count = entries.size(); i < count; i++) {
            ComparisonRanking.WaterIqWithTimestamp waterIq = new ComparisonRanking.WaterIqWithTimestamp();

            waterIq.timestamp = entries.get(i).getCreatedOn().getMillis();
            waterIq.from = entries.get(i).getFrom();
            waterIq.to = entries.get(i).getTo();

            waterIq.user.value = entries.get(i).getUserValue();
            waterIq.user.volume = entries.get(i).getUserVolume();


            waterIq.nearest.value = entries.get(i).getNearestUserValue();
            waterIq.nearest.volume = entries.get(i).getNearestUserVolume();

            waterIq.similar.value = entries.get(i).getSimilarUserValue();
            waterIq.similar.volume= entries.get(i).getSimilarUserVolume();

            waterIq.all.value = entries.get(i).getAllUserValue();
            waterIq.all.volume = entries.get(i).getAllUserVolume();

            result.waterIq.add(waterIq);

            ComparisonRanking.MonthlyConsumtpion monthlyConsumtpion = new ComparisonRanking.MonthlyConsumtpion(entries.get(i).getYear(), entries.get(i).getMonth());
            monthlyConsumtpion.user = entries.get(i).getUserLast1MonthConsmution();
            monthlyConsumtpion.similar = entries.get(i).getSimilarLast1MonthConsmution();
            monthlyConsumtpion.nearest = entries.get(i).getNearestLast1MonthConsmution();
            monthlyConsumtpion.all = entries.get(i).getAllLast1MonthConsmution();
            monthlyConsumtpion.from = entries.get(i).getFrom();
            monthlyConsumtpion.to = entries.get(i).getTo();

            result.monthlyConsumtpion.add(monthlyConsumtpion);

        }

        // Get daily consumption from HBase
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
     * @param neighbor water IQ data for the group of neighbors.
     * @param all water IQ data for all users.
     * @param monthlyConsumtpion monthly consumption data.
     * @param dailyConsumption daily consumption data.
     */
    @Override
    public void update(UUID userKey,
                       String from,
                       String to,
                       ComparisonRanking.WaterIq user,
                       ComparisonRanking.WaterIq similar,
                       ComparisonRanking.WaterIq neighbor,
                       ComparisonRanking.WaterIq all,
                       ComparisonRanking.MonthlyConsumtpion monthlyConsumtpion,
                       List<ComparisonRanking.DailyConsumption> dailyConsumption) {
        DateTime updatedOn = new DateTime();

        boolean persist = false;

        // Get existing record
        String accountQueryString = "select a from account a left join fetch a.waterIqHistory iq_history where a.key = :key";

        TypedQuery<AccountEntity> query = entityManager.createQuery(accountQueryString, AccountEntity.class);
        query.setParameter("key", userKey);

        AccountEntity account = query.getSingleResult();

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
        waterIqHistoryEntity.setYear(Integer.parseInt(from.substring(0, 4)));
        waterIqHistoryEntity.setMonth(Integer.parseInt(from.substring(4, 6)));
        waterIqHistoryEntity.setUserValue(user.value);
        waterIqHistoryEntity.setUserVolume(user.volume);
        if (similar != null) {
            waterIqHistoryEntity.setSimilarUserValue(similar.value);
            waterIqHistoryEntity.setSimilarUserVolume(similar.volume);
        }
        if (neighbor != null) {
            waterIqHistoryEntity.setNearestUserValue(neighbor.value);
            waterIqHistoryEntity.setNearestUserVolume(neighbor.volume);
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
        hBaseWaterIqRepository.update(userKey, from, to, user, similar, neighbor, all, monthlyConsumtpion, dailyConsumption);
    }

    /**
     * Returns the daily consumption data for the given key for the selected year and month.
     *
     * @param userKey the user key.
     * @param year the year.
     * @param month the month.
     * @return a list of {@link DailyConsumption}.
     */
    @Override
    public List<DailyConsumption> getComparisonDailyConsumption(UUID userKey, int year, int month) {
        throw createApplicationException(SharedErrorCode.NOT_IMPLEMENTED);
    }

    /**
     * Returns all the daily consumption data for the given user key.
     *
     * @param userKey the user key.
     * @return a list of {@link DailyConsumption}.
     */
    @Override
    public List<ComparisonRanking.DailyConsumption> getAllComparisonDailyConsumption(UUID userKey) {
        throw createApplicationException(SharedErrorCode.NOT_IMPLEMENTED);
    }

    /**
     * Returns the Water IQ for similar users as computed by the savings potential algorithm.
     *
     * @param utilityId the utility id.
     * @param month the month.
     * @param serial the meter serial number.
     * @return a list of Water IQ values as computed by the savings potential algorithm.
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<SavingsPotentialWaterIqMappingEntity> getWaterIqForSimilarUsersFromSavingsPotential(int utilityId, int month, String serial) {
        String clusterQueryString = "select i.cluster from savings_potential_water_iq i " +
                                    "where i.utility.id = :utilityId and i.month = :month and i.serial = :serial";

        TypedQuery<String> clusterQuery = entityManager.createQuery(clusterQueryString, String.class)
                                                       .setParameter("utilityId", utilityId)
                                                       .setParameter("month", month)
                                                       .setParameter("serial", serial);

        List<String> clusters = clusterQuery.getResultList();
        if (clusters.size() != 1) {
            return new ArrayList<SavingsPotentialWaterIqMappingEntity>();
        }

        String waterIqQuery = "select   i.serial, i.iq, a.key as user_key " +
                              "from     savings_potential_water_iq i " +
                              "             inner join device_meter m on i.serial = m.serial " +
                              "             inner join device d on m.id = d.id " +
                              "             inner join account a on d.account_id = a.id " +
                              "where i.utility_id = :utilityId and i.month = :month and i.cluster = :cluster";

        Query query = entityManager.createNativeQuery(waterIqQuery, "SavingsPotentialWaterIqResult")
                                   .setParameter("utilityId", utilityId)
                                   .setParameter("month", month)
                                   .setParameter("cluster", clusters.get(0));

        return (List<SavingsPotentialWaterIqMappingEntity>) query.getResultList();
    }

    /**
     * Update user Water IQ.
     *
     * @param userKey the user key.
     * @param dailyConsumption daily consumption data.
     */
    @Override
    public void storeDailyData(UUID userKey, List<ComparisonRanking.DailyConsumption> dailyConsumption) {
        throw createApplicationException(SharedErrorCode.NOT_IMPLEMENTED);
    }

}
