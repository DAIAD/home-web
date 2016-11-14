package eu.daiad.web.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.springframework.util.StringUtils;

import com.google.common.collect.ImmutableMap;

import eu.daiad.web.model.device.EnumDeviceType;
import eu.daiad.web.model.query.EnumDataField;
import eu.daiad.web.service.message.aggregates.ComputedNumber;

/**
 * A container for common statistics for a group (or an entire utility) of users
 */
public class ConsumptionStats implements Iterable<ConsumptionStats.Key> {
    
    /**
     * Represents available statistics
     */
    public static enum EnumStatistic {
        
        AVERAGE_MONTHLY,
        AVERAGE_WEEKLY,
        AVERAGE_MONTHLY_PER_SESSION, // Meaningful only for volume/duration
        AVERAGE_WEEKLY_PER_SESSION,  // Meaningful only for volume/duration
        THRESHOLD_BOTTOM_10P_MONTHLY,
        THRESHOLD_BOTTOM_10P_WEEKLY,
        THRESHOLD_BOTTOM_25P_MONTHLY,
        THRESHOLD_BOTTOM_25P_WEEKLY;
        
        private static Map<EnumStatistic, String> titles;      
        static {         
            titles = ImmutableMap.<EnumStatistic, String>builder()
                    .put(AVERAGE_MONTHLY, "Average monthly")
                    .put(AVERAGE_WEEKLY, "Average weekly")
                    .put(AVERAGE_MONTHLY_PER_SESSION, "Average monthly per-session")
                    .put(AVERAGE_WEEKLY_PER_SESSION, "Average weekly per-session")
                    .put(THRESHOLD_BOTTOM_10P_MONTHLY, "Bottom 10% monthly threshold")
                    .put(THRESHOLD_BOTTOM_10P_WEEKLY, "Bottom 10% weekly threshold")
                    .put(THRESHOLD_BOTTOM_25P_MONTHLY, "Bottom 25% monthly threshold")
                    .put(THRESHOLD_BOTTOM_25P_WEEKLY, "Bottom 25% weekly threshold")
                    .build();
        }
        
        public String getTitle() {
            return titles.get(this);
        }
    }
    
    /**
     * Represents a key for a statistic to be computed
     */
    public static class Key {
        
        private final EnumStatistic statistic;
        private final EnumDataField field;
        private final EnumDeviceType device;
        
        public Key(EnumStatistic statistic, EnumDeviceType device, EnumDataField field) {
            this.statistic = statistic;
            this.device = device;
            this.field = field;
        }
        
        public Key(EnumStatistic statistic, EnumDeviceType device) {
            this(statistic, device, EnumDataField.VOLUME);
        }
        
        @Override
        public boolean equals(Object o)
        {
            Key k;
            try {
                k = (Key) o;
            } catch (ClassCastException e) {
                return false;
            }
            return k.statistic == statistic && k.device == device && k.field == field;
        }
        
        @Override
        public int hashCode()
        {
            Integer i = statistic.ordinal() + device.ordinal() * (1<<10) + field.ordinal() * (1<<12);
            return i.hashCode();
        }
        
        @Override
        public String toString()
        {
            return String.format("(%s,%s,%s)", statistic, device, field);
        }

        public EnumStatistic getStatistic()
        {
            return statistic;
        }

        public EnumDataField getField()
        {
            return field;
        }

        public EnumDeviceType getDevice()
        {
            return device;
        }
    }
        
    private HashMap<Key, ComputedNumber> stats; 

	public ConsumptionStats() {
	    this.stats = new HashMap<>();
	}

	public void set(EnumStatistic statistic, EnumDeviceType device, EnumDataField field, ComputedNumber value) {
	    stats.put(new Key(statistic, device, field), value);
	}
	
	public void set(EnumStatistic statistic, EnumDeviceType device, ComputedNumber value) {
	    stats.put(new Key(statistic, device), value);
	}
	
	public ComputedNumber get(Key key)
	{
	    return stats.get(key);
	}
	
	public ComputedNumber get(EnumStatistic statistic, EnumDeviceType device, EnumDataField field) {
	    return stats.get(new Key(statistic, device, field));
	}

	public ComputedNumber get(EnumStatistic statistic, EnumDeviceType device) {
	    return stats.get(new Key(statistic, device));
	}
	
	public void resetValues() {
	    for (ComputedNumber n: stats.values())
	        n.reset();
	}
	
	public boolean isEmpty()
	{
	    return stats.isEmpty();
	}
	
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        for (Key k: stats.keySet()) {
            ComputedNumber n = stats.get(k);
            sb.append(String.format(
                    " * %-10.10s - %-12.12s - %-35.35s = %s\n",
                    StringUtils.capitalize(k.device.name().toLowerCase()),
                    StringUtils.capitalize(k.field.name().toLowerCase()),
                    k.statistic.getTitle(),
                    (n == null || n.getValue() == null)? "NULL" : String.format("%.2f", n.getValue()) 
            ));
        }   
        return sb.toString();
    }

    @Override
    public Iterator<Key> iterator()
    {
        return stats.keySet().iterator();
    }
}
