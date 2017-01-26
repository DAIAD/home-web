package eu.daiad.web.model.message;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import eu.daiad.web.model.AuthenticatedRequest;

public class AlertStatisticsRequest extends AuthenticatedRequest
{
    @JsonIgnore
    private EnumAlertType type;

    @JsonIgnore
    private MessageStatisticsQuery query;

    @JsonProperty("type")
    public EnumAlertType getType()
    {
        return type;
    }

    @JsonIgnore
    public void setType(EnumAlertType type)
    {
        this.type = type;
    }

    @JsonProperty("type")
    public void setType(String type)
    {
        this.type = EnumAlertType.valueOf(type);
    }

    @JsonProperty("query")
    public MessageStatisticsQuery getQuery()
    {
        return query;
    }

    @JsonProperty("query")
    public void setQuery(MessageStatisticsQuery query)
    {
        this.query = query;
    }
}
