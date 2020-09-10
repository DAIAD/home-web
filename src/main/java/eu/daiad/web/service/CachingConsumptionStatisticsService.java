package eu.daiad.web.service;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.Period;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.daiad.web.domain.application.UtilityStatisticsEntity;
import eu.daiad.web.model.ComputedNumber;
import eu.daiad.web.model.EnumStatistic;
import eu.daiad.web.model.query.EnumMeasurementField;
import eu.daiad.web.model.utility.UtilityInfo;
import eu.daiad.web.repository.application.IUtilityRepository;
import eu.daiad.web.repository.application.IUtilityStatisticsRepository;

@Service
public class CachingConsumptionStatisticsService
    implements IConsumptionStatisticsService
{
    private static final Log logger = LogFactory.getLog(CachingConsumptionStatisticsService.class);

    private static boolean debug = false;

    @Autowired
    IConsumptionAggregationService aggregationService;

    @Autowired
    IUtilityRepository utilityRepository;

    @Autowired
    IUtilityStatisticsRepository statisticsRepository;

    private ConcurrentHashMap<String, ComputedNumber> results = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String, Lock> locks = new ConcurrentHashMap<>();

    /**
     * Get a key that represents a particular computation
     *
     * @param utilityKey
     * @param refDate
     * @return a string that represents the key of a particular computation.
     */
    private String getComputationKey(
        UUID utilityKey, DateTime refDate,Period period, EnumMeasurementField field, EnumStatistic statistic)
    {
        return String.format(
            "%s/%s/%s/%s/%s", utilityKey, refDate.toString("YYYYMMdd"), period, field, statistic);
    }

    /**
     * Get a lock corresponding to the computation key.
     *
     * This is mainly to avoid a dog-pile effect while computing.
     *
     * Note that the need for explicit locking, could have been eliminated if
     * using the ConcurrentHashMap.computeIfAbsent method (present only in Java 8)
     *
     * @param key
     * @return Lock
     */
    private Lock getLock(String key)
    {
        Lock l = locks.putIfAbsent(key, new ReentrantLock());
        if (l == null)
            l = locks.get(key);
        return l;
    }

    private ComputedNumber computeNumberIfNeeded(
        UUID utilityKey, DateTime refDate, Period period, EnumMeasurementField field, EnumStatistic statistic)
    {
        debug("Looking-up statistic %s/%s for period %s ending on %s",
            field, statistic, period, refDate.toLocalDate());

        String resultkey = getComputationKey(utilityKey, refDate, period, field, statistic);
        ComputedNumber result = null;

        // 1st try: Search inside service's cache
        result = results.get(resultkey);
        if (result != null)
            return result;

        // The result is not found in service's cache.
        // 2nd try: Search inside statistics repository
        UtilityStatisticsEntity e =
            statisticsRepository.findOne(utilityKey, refDate, period, field, statistic);
        if (e != null) {
            // The result is already computed probably in a previous application run
            result = e.getComputedNumber();
            // Also, put in cache here to avoid future database lookups
            results.putIfAbsent(resultkey, result);
            return result;
        }

        // The result could not be found (either in cache or in repository):
        // we must compute it here and then persist it to repository.

        debug("Need to compute statistic %s/%s for period %s ending on %s",
            field, statistic, period, refDate.toLocalDate());

        Lock l = getLock(resultkey);
        boolean computed = false;

        // Acquire the lock, proceed to computation if needed
        l.lock();
        try {
            result = results.get(resultkey);
            // Check if already computed while waiting on lock
            if (result == null) {
                result = aggregationService.compute(utilityKey, refDate, period, field, statistic);
                results.put(resultkey, result);
                computed = true;
            }
        } finally {
            l.unlock();
        }

        // If computed here (by this thread), persist to repository.
        if (computed) {
            statisticsRepository.save(utilityKey, refDate, period, field, statistic, result);
        }

        return result;

    }

    //
    // Interface IConsumptionStatisticsService
    //

    @Override
    public ComputedNumber getNumber(
        UUID utilityKey, LocalDateTime refDate, Period period, EnumMeasurementField field, EnumStatistic statistic)
    {
        UtilityInfo info = utilityRepository.getUtilityByKey(utilityKey);
        Assert.state(info != null);

        DateTimeZone tz = DateTimeZone.forID(info.getTimezone());
        return computeNumberIfNeeded(utilityKey, refDate.toDateTime(tz), period, field, statistic);
    }

    @Override
    public ComputedNumber getNumber(
        UUID utilityKey, DateTime refDate, Period period, EnumMeasurementField field, EnumStatistic statistic)
    {
        return computeNumberIfNeeded(utilityKey, refDate, period, field, statistic);
    }

    //
    // Helpers
    //

    private static void debug(String f, Object ...args)
    {
        if (debug && logger.isDebugEnabled())
            logger.debug(String.format(f, args));
    }
}
