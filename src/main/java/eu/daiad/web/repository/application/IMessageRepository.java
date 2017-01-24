package eu.daiad.web.repository.application;

import java.util.List;
import java.util.UUID;

import eu.daiad.web.model.message.AlertStatistics;
import eu.daiad.web.model.message.Announcement;
import eu.daiad.web.model.message.AnnouncementRequest;
import eu.daiad.web.model.message.EnumAlertType;
import eu.daiad.web.model.message.EnumRecommendationType;
import eu.daiad.web.model.message.Message;
import eu.daiad.web.model.message.MessageAcknowledgement;
import eu.daiad.web.model.message.MessageRequest;
import eu.daiad.web.model.message.MessageResult;
import eu.daiad.web.model.message.MessageStatisticsQuery;
import eu.daiad.web.model.message.ReceiverAccount;
import eu.daiad.web.model.message.RecommendationStatistics;
import eu.daiad.web.model.message.StaticRecommendation;
import eu.daiad.web.model.security.AuthenticatedUser;

public interface IMessageRepository {

	public abstract MessageResult getMessages(AuthenticatedUser user, MessageRequest request);

	public abstract void setMessageAcknowledgement(AuthenticatedUser user, List<MessageAcknowledgement> messages);

	public List<Message> getTips(String locale);

    public void persistAdvisoryMessageActiveStatus(int id, boolean active);

    public void persistNewAdvisoryMessage(StaticRecommendation staticRecommendation);

    public void updateAdvisoryMessage(StaticRecommendation staticRecommendation);

    public void deleteAdvisoryMessage(StaticRecommendation staticRecommendation);

    public List<Message> getAnnouncements(String locale);

    public void broadcastAnnouncement(AnnouncementRequest announcementRequest, String locale, String channel);

    public void deleteAnnouncement(Announcement announcement);

    public Announcement getAnnouncement(int id, String locale);

    public List<ReceiverAccount> getAnnouncementReceivers(int id);

    public AlertStatistics getAlertStatistics(UUID utilityKey, MessageStatisticsQuery query);

    public RecommendationStatistics getRecommendationStatistics(UUID utilityKey, MessageStatisticsQuery query);

    public List<ReceiverAccount> getAlertReceivers(EnumAlertType type, UUID utilityKey, MessageStatisticsQuery query);

    public List<ReceiverAccount> getRecommendationReceivers(EnumRecommendationType type, UUID utilityKey, MessageStatisticsQuery query);

}
