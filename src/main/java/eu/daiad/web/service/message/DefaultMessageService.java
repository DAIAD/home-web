package eu.daiad.web.service.message;

import java.text.NumberFormat;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.daiad.web.domain.application.AccountAlertEntity;
import eu.daiad.web.domain.application.AccountAnnouncementEntity;
import eu.daiad.web.domain.application.AccountEntity;
import eu.daiad.web.domain.application.AccountRecommendationEntity;
import eu.daiad.web.domain.application.AccountTipEntity;
import eu.daiad.web.domain.application.AnnouncementEntity;
import eu.daiad.web.domain.application.TipEntity;
import eu.daiad.web.model.PagingOptions;
import eu.daiad.web.model.message.Alert;
import eu.daiad.web.model.message.AlertStatistics;
import eu.daiad.web.model.message.Announcement;
import eu.daiad.web.model.message.AnnouncementRequest;
import eu.daiad.web.model.message.EnumAlertType;
import eu.daiad.web.model.message.EnumMessageLevel;
import eu.daiad.web.model.message.EnumMessageType;
import eu.daiad.web.model.message.EnumRecommendationType;
import eu.daiad.web.model.message.Message;
import eu.daiad.web.model.message.MessageAcknowledgement;
import eu.daiad.web.model.message.MessageRequest;
import eu.daiad.web.model.message.MessageResult;
import eu.daiad.web.model.message.MessageStatisticsQuery;
import eu.daiad.web.model.message.ReceiverAccount;
import eu.daiad.web.model.message.Recommendation;
import eu.daiad.web.model.message.RecommendationStatistics;
import eu.daiad.web.model.message.Tip;
import eu.daiad.web.model.query.PopulationFilter;
import eu.daiad.web.model.query.TimeFilter;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.repository.application.IAccountAlertRepository;
import eu.daiad.web.repository.application.IAccountAnnouncementRepository;
import eu.daiad.web.repository.application.IAccountRecommendationRepository;
import eu.daiad.web.repository.application.IAccountTipRepository;
import eu.daiad.web.repository.application.IAnnouncementRepository;
import eu.daiad.web.repository.application.ITipRepository;
import eu.daiad.web.repository.application.IUserRepository;

@Service
public class DefaultMessageService
    implements IMessageService
{
    private static final Log logger = LogFactory.getLog(DefaultMessageService.class);

    @Autowired
    IUserRepository userRepository;

    @Autowired
    IAccountRecommendationRepository accountRecommendationRepository;

    @Autowired
    IAccountAlertRepository accountAlertRepository;

    @Autowired
    IAccountAnnouncementRepository accountAnnouncementRepository;

    @Autowired
    IAnnouncementRepository announcementRepository;

    @Autowired
    ITipRepository tipRepository;

    @Autowired
    IAccountTipRepository accountTipRepository;

    @Override
    public void acknowledgeMessages(AuthenticatedUser user, List<MessageAcknowledgement> messages)
    {
        UUID accountKey = user.getKey();
        for (MessageAcknowledgement message: messages) {
            DateTime acknowledged = new DateTime(message.getTimestamp());
            switch (message.getType()) {
            case ALERT:
                accountAlertRepository.acknowledge(accountKey, message.getId(), acknowledged);
                break;
            case RECOMMENDATION:
                accountRecommendationRepository.acknowledge(accountKey, message.getId(), acknowledged);
                break;
            case TIP:
                accountTipRepository.acknowledge(accountKey, message.getId(), acknowledged);
                break;
            case ANNOUNCEMENT:
                accountAnnouncementRepository.acknowledge(accountKey, message.getId(), acknowledged);
                break;
            default:
                // Ignore unknown message types
                break;
            }
        }
    }

    @Override
    public MessageResult getMessages(AuthenticatedUser user, MessageRequest request)
    {
        MessageResult result = new MessageResult();
        
        Locale locale = request.getLocale();
        if (locale == null)
            locale = Locale.forLanguageTag(user.getLocale());
        
        UUID userKey = user.getKey();

        List<Message> messages = new ArrayList<>();
        MessageRequest.Options options = null;

        //
        // Alerts
        //

        options = request.getOptionsForType(EnumMessageType.ALERT);
        if (options != null) {
            int minMessageId = options.getMinMessageId();
            PagingOptions pagination = options.getPagination();

            result.setTotalAlerts(
                accountAlertRepository.countByAccount(userKey, minMessageId));

            List<AccountAlertEntity> alerts =
                accountAlertRepository.findByAccount(userKey, minMessageId, pagination);
            for (AccountAlertEntity r: alerts) {
                Alert message = accountAlertRepository.formatMessage(r, locale);
                if (message != null)
                    messages.add(message);
            }
        }

        //
        // Recommendations
        //

        options = request.getOptionsForType(EnumMessageType.RECOMMENDATION);
        if (options != null) {
            int minMessageId = options.getMinMessageId();
            PagingOptions pagination = options.getPagination();

            result.setTotalRecommendations(
                accountRecommendationRepository.countByAccount(userKey, minMessageId));

            List<AccountRecommendationEntity> recommendations =
                accountRecommendationRepository.findByAccount(userKey, minMessageId, pagination);
            for (AccountRecommendationEntity r: recommendations) {
                if (EnumMessageLevel.compare(r.getSignificant(), EnumMessageLevel.NOTIFY) < 0)
                    continue; // skip; not considered significant
                Recommendation message = accountRecommendationRepository.formatMessage(r, locale);
                if (message != null)
                    messages.add(message);
            }
        }

        //
        // Announcements
        //

        options = request.getOptionsForType(EnumMessageType.ANNOUNCEMENT);
        if (options != null) {
            int minMessageId = options.getMinMessageId();
            PagingOptions pagination = options.getPagination();

            result.setTotalAnnouncements(
                accountAnnouncementRepository.countByAccount(userKey, minMessageId));

            List<AccountAnnouncementEntity> announcements =
                accountAnnouncementRepository.findByAccount(userKey, minMessageId, pagination);
            for (AccountAnnouncementEntity a: announcements) {
                Announcement message = accountAnnouncementRepository.newMessage(a, locale);
                if (message != null)
                    messages.add(message);
            }
        }

        //
        // Tips (static recommendations)
        //

        options = request.getOptionsForType(EnumMessageType.TIP);
        if (options != null) {
            int minMessageId = options.getMinMessageId();
            PagingOptions pagination = options.getPagination();

            result.setTotalTips(
                accountTipRepository.countByAccount(userKey, minMessageId));

            for (AccountTipEntity r: accountTipRepository.findByAccount(userKey, minMessageId, pagination)) {
                Tip message = accountTipRepository.newMessage(r);
                if (message != null)
                    messages.add(message);
            }
        }

        result.setMessages(messages);
        return result;
    }

    @Override
    public List<Message> getTips(String lang)
    {
        Locale locale = Locale.forLanguageTag(lang);
        List<Message> messages = new ArrayList<>();
        for (TipEntity r: tipRepository.findByLocale(locale)) {
            Tip message = tipRepository.newMessage(r);
            if (message != null)
                messages.add(message);
        }
        return messages;
    }

    @Override
    public void setTipActiveStatus(int id, boolean active)
    {
        tipRepository.setActive(id, active);
    }

    @Override
    public void saveTip(Tip tip)
    {
        tipRepository.saveFrom(tip);
    }

    @Override
    public void deleteTip(int tipId)
    {
        tipRepository.delete(tipId);
    }

    @Override
    public void deleteAnnouncement(int announcementId)
    {
        announcementRepository.delete(announcementId);
    }

    @Override
    public List<Message> getAnnouncements(String lang)
    {
        Locale locale = Locale.forLanguageTag(lang);
        List<Message> messages = new ArrayList<>();
        for (AnnouncementEntity a: announcementRepository.list()) {
            Announcement message = announcementRepository.newMessage(a, locale);
            if (message != null)
                messages.add(message);
        }
        return messages;
    }

    @Override
    public Announcement getAnnouncement(int id, String lang)
    {
        return announcementRepository.newMessage(id, Locale.forLanguageTag(lang));
    }

    @Override
    public List<ReceiverAccount> getAnnouncementReceivers(int id)
    {
        List<ReceiverAccount> receivers = new ArrayList<>();
        for (AccountAnnouncementEntity aa: accountAnnouncementRepository.findByAnnouncement(id)) {
            AccountEntity accountEntity = aa.getAccount();
            ReceiverAccount receiver =
                new ReceiverAccount(accountEntity.getId(), accountEntity.getUsername());
            receivers.add(receiver);
        }
        return receivers;
    }

    @Override
    public AlertStatistics getAlertStatistics(UUID utilityKey, MessageStatisticsQuery query)
    {
        Interval interval = (query.getTime() == null)? null : query.getTime().asInterval();

        ArrayList<PopulationFilter> pf = query.getPopulation();
        if (pf != null && !pf.isEmpty())
            logger.warn("alert statistics: The population filter is ignored");

        return new AlertStatistics()
            .setCountByType(accountAlertRepository.countByType(utilityKey, interval));
    }

    @Override
    public RecommendationStatistics getRecommendationStatistics(UUID utilityKey, MessageStatisticsQuery query)
    {
        Interval interval = (query.getTime() == null)? null : query.getTime().asInterval();

        ArrayList<PopulationFilter> pf = query.getPopulation();
        if (pf != null && !pf.isEmpty())
            logger.warn("recommendation statistics: The population filter is ignored");

        return new RecommendationStatistics()
            .setCountByType(accountRecommendationRepository.countByType(utilityKey, interval));
    }

    @Override
    public List<ReceiverAccount> getAlertReceivers(
        EnumAlertType type, UUID utilityKey, MessageStatisticsQuery query)
    {
        TimeFilter tf = query.getTime();
        List<AccountAlertEntity> alerts = accountAlertRepository.findByType(
            type, utilityKey, (tf == null)? null : tf.asInterval());

        List<ReceiverAccount> receivers = new ArrayList<>();
        for (AccountAlertEntity alert: alerts) {
            AccountEntity accountEntity = alert.getAccount();
            ReceiverAccount account =
                new ReceiverAccount(accountEntity.getId(), accountEntity.getUsername());
            receivers.add(account);
        }
        return receivers;
    }

    @Override
    public List<ReceiverAccount> getRecommendationReceivers(
        EnumRecommendationType type, UUID utilityKey, MessageStatisticsQuery query)
    {
        TimeFilter tf = query.getTime();
        List<AccountRecommendationEntity> recommendations = accountRecommendationRepository.findByType(
            type, utilityKey, (tf == null)? null : tf.asInterval());

        List<ReceiverAccount> receivers = new ArrayList<>();
        for (AccountRecommendationEntity recommendation: recommendations) {
            AccountEntity accountEntity = recommendation.getAccount();
            ReceiverAccount account =
                new ReceiverAccount(accountEntity.getId(), accountEntity.getUsername());
            receivers.add(account);
        }
        return receivers;
    }

    /**
     * Create a new announcement and link it to receiver accounts.
     *
     * Note: This method creates an announcement with a single translation directed to a single channel.
     */
    @Override
    public void broadcastAnnouncement(AnnouncementRequest request, String channelName)
    {
        Announcement announcement = request.getAnnouncement();

        String lang = announcement.getLocale();
        Assert.state(lang != null && !lang.isEmpty());

        // 1. Create announcement

        AnnouncementEntity announcementEntity = announcementRepository.createWith(
            Collections.singletonList(channelName),
            Collections.singletonMap(lang, (Message) announcement));

        // 2. Link announcement with receiver accounts

        for (ReceiverAccount receiver: request.getReceivers()) {
            AccountEntity accountEntity =
                userRepository.findOne(receiver.getId());
            if (accountEntity == null)
                accountEntity = userRepository.getAccountByUsername(receiver.getUsername());
            if (accountEntity != null)
                accountAnnouncementRepository.createWith(accountEntity, announcementEntity);
        }
    }
}