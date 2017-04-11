package eu.daiad.web.model.message;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AccountMessage <M extends Message>
{
    @JsonProperty
    private ReceiverAccount receiver;
    
    @JsonProperty
    private M message;
    
    public AccountMessage(ReceiverAccount receiver, M message)
    {
        this.receiver = receiver;
        this.message = message;
    }

    public ReceiverAccount getReceiver()
    {
        return receiver;
    }

    public void setReceiver(ReceiverAccount receiver)
    {
        this.receiver = receiver;
    }
    
    public M getMessage()
    {
        return message;
    }
    
    public void setMessage(M message)
    {
        this.message = message;
    }
}
