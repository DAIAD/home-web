package eu.daiad.common.model.message;

import java.util.Collections;
import java.util.List;

import eu.daiad.common.model.AuthenticatedRequest;

public class MessageAcknowledgementRequest extends AuthenticatedRequest {

	private List<MessageAcknowledgement> messages;

	public List<MessageAcknowledgement> getMessages()
	{
		return (messages == null)?
		    Collections.<MessageAcknowledgement>emptyList() : messages;
	}

	public void setMessages(List<MessageAcknowledgement> messages) {
		this.messages = messages;
	}

}
