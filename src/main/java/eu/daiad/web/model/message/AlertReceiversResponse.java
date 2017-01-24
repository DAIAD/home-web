package eu.daiad.web.model.message;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import eu.daiad.web.model.RestResponse;

public class AlertReceiversResponse extends RestResponse
{
    @JsonIgnore
    private EnumAlertType type;

    @JsonIgnore
    private List<ReceiverAccount> receivers;

    public AlertReceiversResponse() {}

    public AlertReceiversResponse(EnumAlertType type, List<ReceiverAccount> receivers)
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
}
