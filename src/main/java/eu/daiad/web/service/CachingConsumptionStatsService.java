package eu.daiad.web.service;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.daiad.web.model.ConsumptionStats;
import eu.daiad.web.model.utility.UtilityInfo;
import eu.daiad.web.repository.application.ConsumptionStatsRepository;
import eu.daiad.web.repository.application.IConsumptionStatsRepository;

@Service
public class CachingConsumptionStatsService implements IConsumptionStatsService
{
    private static final Log logger = LogFactory.getLog(CachingConsumptionStatsService.class);
    
    @Autowired
    IConsumptionAggregationService aggregationService;
    
    @Autowired
    IConsumptionStatsRepository statsRepository;
    
    private ConcurrentHashMap<String, ConsumptionStats> results = new ConcurrentHashMap<>();
    
    private ConcurrentHashMap<String, Lock> locks = new ConcurrentHashMap<>();
    
    /**
     * Get a key that represents a particular computation
     * 
     * @param utilityKey
     * @param refDate
     * @return
     */
    private String getComputationKey(UUID utilityKey, LocalDateTime refDate)
    {
        return String.format("%s/%s", utilityKey.toString(), refDate.toString("YYYYMMdd"));
    }
    
    /**
     * Get a lock corresponding to the computation key. 
     * 
     * This is mainly to avoid a dog-pile effect while computing.
     * 
     * Note that the need for explicit locking, could have been eliminated if
     * using the ConcurrentHashMap.computeIfAbsent method (which is present only in Java 8)
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
    
    public ConsumptionStats getStats(UtilityInfo utility, LocalDateTime refDate, boolean refresh)
    {
        logger.info("Getting stats for utility " + utility.getName() + " at " + refDate.toDate());
        
        String key = getComputationKey(utility.getKey(), refDate);
        ConsumptionStats result;
       
        if (!refresh) {
            // 1st try: Search inside service's cache
            result = results.get(key);
            if (result != null)
                return result;

            // The result is not found in service's cache.
            // 2nd try: Search inside stats repository
            result = statsRepository.get(utility.getKey(), null, refDate);
            if (result != null)
                return result;
        }
        
        // The result could be found: 
        // We must compute it here and then persist it to stats repository.
        
        logger.info("Computing stats for utility " + utility.getName() + " at " + refDate.toDate());
        
        Lock l = getLock(key);
        boolean computed = false;
        
        // Acquire the lock, proceed to computation if needed
        l.lock();
        try {
            result = results.get(key);
            // Check if already computed while waiting on lock
            if (result == null) {
                result = aggregationService.compute(utility, refDate);
                results.put(key, result);
                computed = true;
            }
        } finally {
            l.unlock();
        }
        
        // If computed here, persist to stats repository.
        if (computed) {
            statsRepository.save(utility.getKey(), null, refDate, result);
        }
        
        return result;
    }

    @Override
    public ConsumptionStats getStats(UtilityInfo utility, LocalDateTime refDate)
    {
        return getStats(utility, refDate, false);
    }

}
