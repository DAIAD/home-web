package eu.daiad.web.repository.application;

import java.util.List;

import eu.daiad.web.model.message.Message;
import eu.daiad.web.model.message.MessageAcknowledgement;
import eu.daiad.web.model.message.MessageRequest;
import eu.daiad.web.model.message.MessageResult;
import eu.daiad.web.model.message.StaticRecommendation;
import eu.daiad.web.model.message.AnnouncementRequest;

public interface IMessageRepository {

	public abstract MessageResult getMessages(MessageRequest request);

	public abstract void setMessageAcknowledgement(List<MessageAcknowledgement> messages);

	public List<Message> getAdvisoryMessages(String locale);
        
    public void persistAdvisoryMessageActiveStatus(int id, boolean active);
        
    public void persistNewAdvisoryMessage(StaticRecommendation staticRecommendation);
    
    public void updateAdvisoryMessage(StaticRecommendation staticRecommendation);
    
    public void deleteAdvisoryMessage(StaticRecommendation staticRecommendation);
    
    public List<Message> getAnnouncements(String locale);
            
    public void broadcastAnnouncement(AnnouncementRequest announcementRequest, String locale, String channel);

}
