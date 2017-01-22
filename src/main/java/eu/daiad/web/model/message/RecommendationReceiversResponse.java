package eu.daiad.web.model.message;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import eu.daiad.web.model.RestResponse;

public class RecommendationReceiversResponse extends RestResponse
{
    @JsonIgnore
    private EnumRecommendationType type;

    @JsonIgnore
    private List<ReceiverAccount> receivers;

    public RecommendationReceiversResponse() {}

    public RecommendationReceiversResponse(EnumRecommendationType type, List<ReceiverAccount> receivers)
    {
        this.type = type;
        this.receivers = receivers;
    }

    @JsonProperty("receivers")
    public List<ReceiverAccount> getReceivers() {
        return receivers;
    }

    @JsonProperty("receivers")
    public void setReceivers(List<ReceiverAccount> receivers) {
        this.receivers = receivers;
    }

    @JsonProperty("type")
    public EnumRecommendationType getType()
    {
        return type;
    }

    @JsonIgnore
    public void setType(EnumRecommendationType type)
    {
        this.type = type;
    }

    @JsonProperty("type")
    public void setType(String type)
    {
        this.type = EnumRecommendationType.valueOf(type);
    }
}