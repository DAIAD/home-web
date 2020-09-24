package eu.daiad.common.model.message;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class AlertStatistics
{
    public static class ByType
    {
        private final EnumAlertType type;
        private final int count;

        public ByType(EnumAlertType type, int count)
        {
            this.type = type;
            this.count = count;
        }

        public EnumAlertType getType()
        {
            return type;
        }

        public long getCount()
        {
            return count;
        }
    }

    private Map<EnumAlertType, Integer> countByType = new EnumMap<>(EnumAlertType.class);

    public AlertStatistics() {}

    public Map<EnumAlertType, Integer> getCountByType()
    {
        return countByType;
    }

    public int getCountByType(EnumAlertType type)
    {
        Integer n = countByType.get(type);
        return (n != null)? n.intValue() : 0;
    }

    public AlertStatistics setCountByType(Map<EnumAlertType, Integer> countByType)
    {
        this.countByType.putAll(countByType);
        return this;
    }

    public List<ByType> asList()
    {
        List<ByType> r = new ArrayList<>();

        for (Map.Entry<EnumAlertType, Integer> e: countByType.entrySet())
            r.add(new ByType(e.getKey(), e.getValue()));

        return r;
    }
}
