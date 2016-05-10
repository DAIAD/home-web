package eu.daiad.web.model.message;

import java.util.ArrayList;
import java.util.List;

import eu.daiad.web.model.RestResponse;

public class MessageResponse extends RestResponse {
	List<Message> messages = new ArrayList<>();

	public MessageResponse() {

	}

	public MessageResponse(List<Message> messages) {
		this.messages = messages;
	}

	public List<Message> getMessages() {
		return messages;
	}

	public void setMessages(List<Message> messages) {
		this.messages = messages;
	}

}
