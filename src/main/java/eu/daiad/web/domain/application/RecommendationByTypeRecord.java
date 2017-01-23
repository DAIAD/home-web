package eu.daiad.web.domain.application;

import eu.daiad.web.model.message.EnumRecommendationType;

public class RecommendationByTypeRecord
{
    private final EnumRecommendationType type;

    private final long count;

    public RecommendationByTypeRecord(EnumRecommendationType type, long count)
    {
        this.type = type;
        this.count = count;
    }

    public RecommendationByTypeRecord(String type, long count)
    {
        this(EnumRecommendationType.valueOf(type), count);
    }

    public RecommendationByTypeRecord(int code, long count)
    {
        this(EnumRecommendationType.valueOf(code), count);
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
