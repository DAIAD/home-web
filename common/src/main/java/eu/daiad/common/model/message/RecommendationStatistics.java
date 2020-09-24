package eu.daiad.common.model.message;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class RecommendationStatistics
{
    public static class ByType
    {
        private final EnumRecommendationType type;
        private final int count;

        public ByType(EnumRecommendationType type, int count)
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

    private Map<EnumRecommendationType, Integer> countByType = new EnumMap<>(EnumRecommendationType.class);

    public RecommendationStatistics() {}

    public Map<EnumRecommendationType, Integer> getCountByType()
    {
        return countByType;
    }

    public int getCountByType(EnumRecommendationType type)
    {
        Integer n = countByType.get(type);
        return (n != null)? n.intValue() : 0;
    }

    public RecommendationStatistics setCountByType(Map<EnumRecommendationType, Integer> countByType)
    {
        this.countByType.putAll(countByType);
        return this;
    }

    public List<ByType> asList()
    {
        List<ByType> r = new ArrayList<>();

        for (Map.Entry<EnumRecommendationType, Integer> e: countByType.entrySet())
            r.add(new ByType(e.getKey(), e.getValue()));

        return r;
    }
}
