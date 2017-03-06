package eu.daiad.web.model.message;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PerAccountMessages
{
    @JsonProperty
    private ReceiverAccount receiver;
    
    @JsonProperty
    private List<Message> messages;

    public PerAccountMessages(ReceiverAccount receiver, List<Message> messages)
    {
        this.receiver = receiver;
        this.messages = messages;
    }

    public ReceiverAccount getReceiver()
    {
        return receiver;
    }

    public void setReceiver(ReceiverAccount receiver)
    {
        this.receiver = receiver;
    }

    public List<Message> getMessages()
    {
        return messages;
    }

    public void setMessages(List<Message> messages)
    {
        this.messages = messages;
    }
}
