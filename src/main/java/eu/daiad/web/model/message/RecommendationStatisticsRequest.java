package eu.daiad.web.model.message;

import eu.daiad.web.model.AuthenticatedRequest;

public class RecommendationStatisticsRequest extends AuthenticatedRequest
{
    private EnumRecommendationType type;

    private MessageStatisticsQuery query;

    public EnumRecommendationType getType()
    {
        return type;
    }

    public void setType(EnumRecommendationType type)
    {
        this.type = type;
    }

    public void setType(String type)
    {
        this.type = EnumRecommendationType.valueOf(type);
    }

    public void setType(int code)
    {
        this.type = EnumRecommendationType.valueOf(code);
    }

    public MessageStatisticsQuery getQuery()
    {
        return query;
    }

    public void setQuery(MessageStatisticsQuery query)
    {
        this.query = query;
    }
}
