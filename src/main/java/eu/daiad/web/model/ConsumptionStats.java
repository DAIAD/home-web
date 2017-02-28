package eu.daiad.web.model;

import java.util.HashMap;
import java.util.Iterator;

import org.springframework.util.StringUtils;

import eu.daiad.web.model.device.EnumDeviceType;
import eu.daiad.web.model.query.EnumDataField;

/**
 * A container for common statistics for a group (or an entire utility) of users
 */
public class ConsumptionStats 
    implements Iterable<ConsumptionStats.Key> 
{
    
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
	
	public Double getValue(EnumStatistic statistic, EnumDeviceType device, EnumDataField field) {
        ComputedNumber c = stats.get(new Key(statistic, device, field));
        return (c == null)? null : c.getValue();
    }

    public Double getValue(EnumStatistic statistic, EnumDeviceType device) {
        return getValue(statistic, device, EnumDataField.VOLUME);
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
                    k.statistic,
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
