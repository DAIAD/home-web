package eu.daiad.common.service.message;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.daiad.common.domain.application.AccountAlertEntity;
import eu.daiad.common.domain.application.AccountAnnouncementEntity;
import eu.daiad.common.domain.application.AccountEntity;
import eu.daiad.common.domain.application.AccountRecommendationEntity;
import eu.daiad.common.domain.application.AccountTipEntity;
import eu.daiad.common.domain.application.AnnouncementEntity;
import eu.daiad.common.domain.application.TipEntity;
import eu.daiad.common.model.PagingOptions;
import eu.daiad.common.model.message.Alert;
import eu.daiad.common.model.message.AlertStatistics;
import eu.daiad.common.model.message.Announcement;
import eu.daiad.common.model.message.AnnouncementRequest;
import eu.daiad.common.model.message.EnumAlertType;
import eu.daiad.common.model.message.EnumMessageLevel;
import eu.daiad.common.model.message.EnumMessageType;
import eu.daiad.common.model.message.EnumRecommendationType;
import eu.daiad.common.model.message.Message;
import eu.daiad.common.model.message.MessageAcknowledgement;
import eu.daiad.common.model.message.MessageRequest;
import eu.daiad.common.model.message.MessageResult;
import eu.daiad.common.model.message.MessageStatisticsQuery;
import eu.daiad.common.model.message.ReceiverAccount;
import eu.daiad.common.model.message.Recommendation;
import eu.daiad.common.model.message.RecommendationStatistics;
import eu.daiad.common.model.message.Tip;
import eu.daiad.common.model.query.PopulationFilter;
import eu.daiad.common.model.query.TimeFilter;
import eu.daiad.common.model.security.AuthenticatedUser;
import eu.daiad.common.repository.application.IAccountAlertRepository;
import eu.daiad.common.repository.application.IAccountAnnouncementRepository;
import eu.daiad.common.repository.application.IAccountRecommendationRepository;
import eu.daiad.common.repository.application.IAccountTipRepository;
import eu.daiad.common.repository.application.IAnnouncementRepository;
import eu.daiad.common.repository.application.ITipRepository;
import eu.daiad.common.repository.application.IUserRepository;

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
                accountRecommendationRepository.countByAccount(userKey, minMessageId, EnumMessageLevel.NOTIFY));

            List<AccountRecommendationEntity> recommendations =
                accountRecommendationRepository.findByAccount(userKey, minMessageId, pagination, EnumMessageLevel.NOTIFY);
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
            ReceiverAccount receiver = new ReceiverAccount(accountEntity.getId(),
                                                           accountEntity.getUsername(),
                                                           accountEntity.getFirstname(),
                                                           accountEntity.getLastname(),
                                                           aa.getAcknowledgedOn());
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
                new ReceiverAccount(accountEntity.getId(),
                                    accountEntity.getUsername(),
                                    accountEntity.getFirstname(),
                                    accountEntity.getLastname(),
                                    alert.getAcknowledgedOn());
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
                new ReceiverAccount(accountEntity.getId(),
                                    accountEntity.getUsername(),
                                    accountEntity.getFirstname(),
                                    accountEntity.getUsername(),
                                    recommendation.getAcknowledgedOn());
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
        Assert.state(lang != null && !lang.isEmpty(), "[Assertion failed] - Database is inconsistent");

        // 1. Create announcement

        AnnouncementEntity announcementEntity = announcementRepository.createWith(
            Collections.singletonList(channelName),
            Collections.singletonMap(lang, (Message) announcement));

        // 2. Link announcement with receiver accounts

        for (ReceiverAccount receiver : request.getReceivers()) {
            AccountEntity accountEntity = null;
            if (receiver.getId() != null) {
                accountEntity = userRepository.findOne(receiver.getId());
            }
            if (accountEntity == null) {
                accountEntity = userRepository.getAccountByUsername(receiver.getUsername());
            }
            if (accountEntity != null) {
                accountAnnouncementRepository.createWith(accountEntity, announcementEntity);
            }
        }
    }
}