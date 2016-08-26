package eu.daiad.web.repository.application;

import java.util.List;
import java.util.Map;

import eu.daiad.web.model.message.Message;
import eu.daiad.web.model.message.MessageAcknowledgement;
import eu.daiad.web.model.message.MessageRequest;
import eu.daiad.web.model.message.MessageResult;
import eu.daiad.web.model.message.StaticRecommendation;
import eu.daiad.web.model.message.AnnouncementRequest;
import eu.daiad.web.model.message.Announcement;
import eu.daiad.web.model.message.ReceiverAccount;
import eu.daiad.web.model.message.Alert;
import eu.daiad.web.model.message.DynamicRecommendation;
import eu.daiad.web.model.message.MessageStatisticsQuery;

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
    
    public void deleteAnnouncement(Announcement announcement);
    
    public Announcement getAnnouncement(int id, String locale);
    
    public List<ReceiverAccount> getAnnouncementReceivers(int id);
    
    public Map<Alert, Integer> getAlertStatistics(String locale, MessageStatisticsQuery query);
    
    public Map<DynamicRecommendation, Integer> getRecommendationStatistics(String locale);    
    
    public List<ReceiverAccount> getMessageReceivers(int messageId);

}
