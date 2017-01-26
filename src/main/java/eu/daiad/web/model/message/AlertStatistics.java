package eu.daiad.web.model.message;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class AlertStatistics
{
    public static class ByType
    {
        private final EnumAlertType type;
        private final long count;

        public ByType(EnumAlertType type, long count)
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

    private Map<EnumAlertType, Long> countByType = new EnumMap<>(EnumAlertType.class);

    public AlertStatistics() {}

    public Map<EnumAlertType, Long> getCountByType()
    {
        return countByType;
    }

    public Long getCountByType(EnumAlertType type)
    {
        return countByType.get(type);
    }

    public AlertStatistics setCountByType(Map<EnumAlertType, Long> countByType)
    {
        this.countByType.putAll(countByType);
        return this;
    }

    public List<ByType> asList()
    {
        List<ByType> r = new ArrayList<>();

        for (Map.Entry<EnumAlertType, Long> e: countByType.entrySet())
            r.add(new ByType(e.getKey(), e.getValue()));

        return r;
    }
}
