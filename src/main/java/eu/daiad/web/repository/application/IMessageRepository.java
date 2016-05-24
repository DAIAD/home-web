package eu.daiad.web.repository.application;

import java.util.List;

import eu.daiad.web.model.message.Message;
import eu.daiad.web.model.message.MessageAcknowledgement;
import eu.daiad.web.model.message.MessageRequest;
import eu.daiad.web.model.message.MessageResult;

public interface IMessageRepository {

	public abstract MessageResult getMessages(MessageRequest request);

	public abstract void setMessageAcknowledgement(List<MessageAcknowledgement> messages);

	public List<Message> getAdvisoryMessages(String locale);
}
