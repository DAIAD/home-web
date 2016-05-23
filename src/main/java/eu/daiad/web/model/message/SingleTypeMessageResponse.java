package eu.daiad.web.model.message;

import java.util.ArrayList;
import java.util.List;

import eu.daiad.web.model.RestResponse;

public class SingleTypeMessageResponse extends RestResponse {

	private EnumMessageType type;

	private List<Message> messages = new ArrayList<>();

	public EnumMessageType getType() {
		return type;
	}

	public void setType(EnumMessageType type) {
		this.type = type;
	}

	public List<Message> getMessages() {
		return messages;
	}

	public void setMessages(List<Message> messages) {
		this.messages = messages;
	}

}
