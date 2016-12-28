package eu.daiad.web.repository.application;

import java.util.List;

import eu.daiad.web.model.message.Message;
import eu.daiad.web.model.message.MessageAcknowledgement;
import eu.daiad.web.model.message.MessageRequest;
import eu.daiad.web.model.message.MessageResult;
import eu.daiad.web.model.message.StaticRecommendation;
import eu.daiad.web.model.message.AnnouncementRequest;
import eu.daiad.web.model.message.Announcement;
import eu.daiad.web.model.message.ReceiverAccount;
import eu.daiad.web.model.message.Alert;
import eu.daiad.web.model.message.Recommendation;
import eu.daiad.web.model.message.MessageStatisticsQuery;
import eu.daiad.web.domain.application.AlertAnalyticsEntity;
import eu.daiad.web.domain.application.RecommendationAnalyticsEntity;

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
    
    public List<AlertAnalyticsEntity> getAlertStatistics(String locale, int utilityId, MessageStatisticsQuery query);
    
    public List<RecommendationAnalyticsEntity> getRecommendationStatistics(String locale, int utilityId, MessageStatisticsQuery query);    
    
    public List<ReceiverAccount> getAlertReceivers(int alertId, int utilityId, MessageStatisticsQuery query);
    
    public List<ReceiverAccount> getRecommendationReceivers(int recommendationId, int utilityId, MessageStatisticsQuery query);   
    
    public Alert getAlert(int id, String locale);
    
    public Recommendation getRecommendation(int id, String locale);

}
