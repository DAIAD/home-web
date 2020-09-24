package eu.daiad.common.service.message;

import java.util.List;
import java.util.UUID;

import eu.daiad.common.model.message.AlertStatistics;
import eu.daiad.common.model.message.Announcement;
import eu.daiad.common.model.message.AnnouncementRequest;
import eu.daiad.common.model.message.EnumAlertType;
import eu.daiad.common.model.message.EnumRecommendationType;
import eu.daiad.common.model.message.Message;
import eu.daiad.common.model.message.MessageAcknowledgement;
import eu.daiad.common.model.message.MessageRequest;
import eu.daiad.common.model.message.MessageResult;
import eu.daiad.common.model.message.MessageStatisticsQuery;
import eu.daiad.common.model.message.ReceiverAccount;
import eu.daiad.common.model.message.RecommendationStatistics;
import eu.daiad.common.model.message.Tip;
import eu.daiad.common.model.security.AuthenticatedUser;

public interface IMessageService
{
	public MessageResult getMessages(AuthenticatedUser user, MessageRequest request);
	
	public void acknowledgeMessages(AuthenticatedUser user, List<MessageAcknowledgement> messages);

	public List<Message> getTips(String lang);

    public void setTipActiveStatus(int id, boolean active);

    public void saveTip(Tip tip);

    public void deleteTip(int id);

    public List<Message> getAnnouncements(String locale);

    public void broadcastAnnouncement(AnnouncementRequest announcementRequest, String channel);

    public void deleteAnnouncement(int id);

    public Announcement getAnnouncement(int id, String lang);

    public List<ReceiverAccount> getAnnouncementReceivers(int id);

    public AlertStatistics getAlertStatistics(UUID utilityKey, MessageStatisticsQuery query);

    public RecommendationStatistics getRecommendationStatistics(UUID utilityKey, MessageStatisticsQuery query);

    public List<ReceiverAccount> getAlertReceivers(EnumAlertType type, UUID utilityKey, MessageStatisticsQuery query);

    public List<ReceiverAccount> getRecommendationReceivers(EnumRecommendationType type, UUID utilityKey, MessageStatisticsQuery query);

}
