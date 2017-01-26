package eu.daiad.web.domain.application;

import eu.daiad.web.model.message.EnumRecommendationType;

public class RecommendationByTypeRecord
{
    private final EnumRecommendationType type;

    private final int count;

    public RecommendationByTypeRecord(EnumRecommendationType type, Long count)
    {
        this.type = type;
        this.count = count.intValue();
    }

    public RecommendationByTypeRecord(String type, Long count)
    {
        this(EnumRecommendationType.valueOf(type), count);
    }

    public RecommendationByTypeRecord(int code, Long count)
    {
        this(EnumRecommendationType.valueOf(code), count);
    }

    public EnumRecommendationType getType()
    {
        return type;
    }

    public int getCount()
    {
        return count;
    }
}
