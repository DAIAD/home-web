package eu.daiad.web.repository.application;

import java.util.List;

import org.joda.time.DateTime;

import eu.daiad.web.model.message.EnumMessageType;
import eu.daiad.web.model.message.Message;

public interface IMessageRepository {

	public abstract List<Message> getMessages();

	public abstract void setMessageAcknowledgement(EnumMessageType type, int id, DateTime acknowledgedOn);

}
