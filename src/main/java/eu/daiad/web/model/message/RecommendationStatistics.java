package eu.daiad.web.model.message;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class RecommendationStatistics
{
    public static class ByType
    {
        private final EnumRecommendationType type;
        private final long count;

        public ByType(EnumRecommendationType type, long count)
        {
            this.type = type;
            this.count = count;
        }

        public EnumRecommendationType getType()
        {
            return type;
        }

        public long getCount()
        {
            return count;
        }
    }

    private Map<EnumRecommendationType, Long> countByType = new EnumMap<>(EnumRecommendationType.class);

    public RecommendationStatistics() {}

    public Map<EnumRecommendationType, Long> getCountByType()
    {
        return countByType;
    }

    public Long getCountByType(EnumRecommendationType type)
    {
        return countByType.get(type);
    }

    public RecommendationStatistics setCountByType(Map<EnumRecommendationType, Long> countByType)
    {
        this.countByType.putAll(countByType);
        return this;
    }

    public List<ByType> asList()
    {
        List<ByType> r = new ArrayList<>();

        for (Map.Entry<EnumRecommendationType, Long> e: countByType.entrySet())
            r.add(new ByType(e.getKey(), e.getValue()));

        return r;
    }
}
