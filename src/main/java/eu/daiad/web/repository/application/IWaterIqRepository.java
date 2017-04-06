package eu.daiad.web.repository.application;

import java.util.List;
import java.util.UUID;

import eu.daiad.web.model.profile.ComparisonRanking;
import eu.daiad.web.model.profile.ComparisonRanking.DailyConsumption;

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
     * @param nearest water IQ data for the group of neighbors.
     * @param all water IQ data for all users.
     * @param monthlyConsumtpion monthly consumption data.
     * @param dailyConsumption daily consumption data.
     */
    void update(UUID userKey,
                String from,
                String to,
                ComparisonRanking.WaterIq user,
                ComparisonRanking.WaterIq similar,
                ComparisonRanking.WaterIq nearest,
                ComparisonRanking.WaterIq all,
                ComparisonRanking.MonthlyConsumtpion monthlyConsumtpion,
                List<ComparisonRanking.DailyConsumption> dailyConsumption);

    /**
     * Returns water IQ data for the user with the given key.
     *
     * @param key the user key.
     * @param year reference year.
     * @param month reference month.
     * @return water IQ data.
     */
    ComparisonRanking getWaterIqByUserKey(UUID key, int year, int month);

    /**
     * Returns the daily consumption for the given key for the selected year and month.
     *
     * @param userKey the user key.
     * @param year the year.
     * @param month the month.
     * @return a list of {@link DailyConsumption}.
     */
    List<ComparisonRanking.DailyConsumption> getComparisonDailyConsumption(UUID userKey, int year, int month);

    /**
     * Returns the Water IQ as computed by the savings potential algorithm.
     *
     * @param month the month.
     * @param serial the meter serial number.
     * @return a value from A to F or null if not savings potential data exist.
     */
    String getWaterIqFromSavingsPotential(int month, String serial);

}
