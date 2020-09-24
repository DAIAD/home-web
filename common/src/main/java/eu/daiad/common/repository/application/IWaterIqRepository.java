package eu.daiad.common.repository.application;

import java.util.List;
import java.util.UUID;

import eu.daiad.common.domain.application.mappings.SavingsPotentialWaterIqMappingEntity;
import eu.daiad.common.model.profile.ComparisonRanking;
import eu.daiad.common.model.profile.ComparisonRanking.DailyConsumption;

public interface IWaterIqRepository {

    /**
     * Deletes stale water IQ data.
     *
     * @param days number of dates after which water IQ data is considered stale.
     */
    void clean(int days);

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
    void update(UUID userKey,
                String from,
                String to,
                ComparisonRanking.WaterIq user,
                ComparisonRanking.WaterIq similar,
                ComparisonRanking.WaterIq neighbor,
                ComparisonRanking.WaterIq all,
                ComparisonRanking.MonthlyConsumtpion monthlyConsumtpion,
                List<ComparisonRanking.DailyConsumption> dailyConsumption);

    /**
     * Returns water IQ data for the user with the given key. Monthly data for
     * the last six months is returned. Daily data is returned only for the
     * most recent month. The time interval is set by the reference year and
     * month.
     *
     * @param key the user key.
     * @param year reference year.
     * @param month reference month.
     * @return water IQ data.
     */
    ComparisonRanking getWaterIqByUserKey(UUID key, int year, int month);

    /**
     * Returns the daily consumption data for the given key for the selected year and month.
     *
     * @param userKey the user key.
     * @param year the year.
     * @param month the month.
     * @return a list of {@link DailyConsumption}.
     */
    List<ComparisonRanking.DailyConsumption> getComparisonDailyConsumption(UUID userKey, int year, int month);

    /**
     * Returns all the daily consumption data for the given user key.
     *
     * @param userKey the user key.
     * @return a list of {@link DailyConsumption}.
     */
    List<ComparisonRanking.DailyConsumption> getAllComparisonDailyConsumption(UUID userKey);

    /**
     * Returns the Water IQ for similar users as computed by the savings potential algorithm.
     *
     * @param utilityId the utility id.
     * @param month the month.
     * @param serial the meter serial number.
     * @return a list of Water IQ values as computed by the savings potential algorithm
     */
    List<SavingsPotentialWaterIqMappingEntity> getWaterIqForSimilarUsersFromSavingsPotential(int utilityId, int month, String serial);

    /**
     * Update user Water IQ.
     *
     * @param userKey the user key.
     * @param dailyConsumption daily consumption data.
     */
    void storeDailyData(UUID userKey, List<ComparisonRanking.DailyConsumption> dailyConsumption);
}
