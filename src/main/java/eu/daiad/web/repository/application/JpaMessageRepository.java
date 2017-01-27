package eu.daiad.web.repository.application;

import java.text.NumberFormat;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.daiad.web.domain.application.AccountAlertEntity;
import eu.daiad.web.domain.application.AccountAnnouncementEntity;
import eu.daiad.web.domain.application.AccountEntity;
import eu.daiad.web.domain.application.AccountRecommendationEntity;
import eu.daiad.web.domain.application.AccountTipEntity;
import eu.daiad.web.domain.application.AnnouncementEntity;
import eu.daiad.web.domain.application.TipCategoryEntity;
import eu.daiad.web.domain.application.TipEntity;
import eu.daiad.web.model.PagingOptions;
import eu.daiad.web.model.error.MessageErrorCode;
import eu.daiad.web.model.error.SharedErrorCode;
import eu.daiad.web.model.message.Alert;
import eu.daiad.web.model.message.AlertStatistics;
import eu.daiad.web.model.message.Announcement;
import eu.daiad.web.model.message.AnnouncementRequest;
import eu.daiad.web.model.message.EnumAlertType;
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
import eu.daiad.web.repository.BaseRepository;

@Repository
@Transactional("applicationTransactionManager")
public class JpaMessageRepository extends BaseRepository
    implements IMessageRepository
{
    private static final Log logger = LogFactory.getLog(JpaMessageRepository.class);

    @PersistenceContext(unitName = "default")
    EntityManager entityManager;

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

    @Deprecated
    // Fixme Move this to controller
    private AuthenticatedUser getCurrentAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth.getPrincipal() instanceof AuthenticatedUser) {
            return (AuthenticatedUser) auth.getPrincipal();
        } else {
            throw createApplicationException(SharedErrorCode.AUTHORIZATION_ANONYMOUS_SESSION);
        }
    }

    @Override
    public void acknowledgeMessages(AuthenticatedUser user, List<MessageAcknowledgement> messages)
    {
        UUID accountKey = user.getKey();
        for (MessageAcknowledgement message: messages) {
            int messageId = message.getId();
            DateTime acknowledged = new DateTime(message.getTimestamp());
            switch (message.getType()) {
            case ALERT:
                accountAlertRepository.acknowledge(accountKey, messageId, acknowledged);
                break;
            case RECOMMENDATION:
                accountRecommendationRepository.acknowledge(accountKey, messageId, acknowledged);
                break;
            case TIP:
                persistStaticRecommendationAcknowledgement(messageId, acknowledged);
                break;
            case ANNOUNCEMENT:
                accountAnnouncementRepository.acknowledge(accountKey, messageId, acknowledged);
                break;
            default:
                throw createApplicationException(MessageErrorCode.MESSAGE_TYPE_NOT_SUPPORTED)
                    .set("type.", message.getType());
            }
        }

    }

    private MessageRequest.Options getMessageOptions(MessageRequest request, EnumMessageType type)
    {
        for (MessageRequest.Options p: request.getMessages()) {
            if (p.getType() == type)
                return p;
        }
        return null;
    }

    @Override
    public MessageResult getMessages(AuthenticatedUser user, MessageRequest request)
    {
        MessageResult result = new MessageResult();

        String lang = user.getLocale();
        Locale locale = Locale.forLanguageTag(lang);

        UUID userKey = user.getKey();

        List<Message> messages = new ArrayList<>();
        MessageRequest.Options options = null;

        //
        // Alerts
        //

        options = this.getMessageOptions(request, EnumMessageType.ALERT);
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

        options = this.getMessageOptions(request, EnumMessageType.RECOMMENDATION);
        if (options != null) {
            int minMessageId = options.getMinMessageId();
            PagingOptions pagination = options.getPagination();

            result.setTotalRecommendations(
                accountRecommendationRepository.countByAccount(userKey, minMessageId));

            List<AccountRecommendationEntity> recommendations =
                accountRecommendationRepository.findByAccount(userKey, minMessageId, pagination);
            for (AccountRecommendationEntity r: recommendations) {
                Recommendation message = accountRecommendationRepository.formatMessage(r, locale);
                if (message != null)
                    messages.add(message);
            }
        }

        //
        // Announcements
        //

        options = this.getMessageOptions(request, EnumMessageType.ANNOUNCEMENT);
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

        options = this.getMessageOptions(request, EnumMessageType.TIP);
        if (options != null) {
            int minMessageId = options.getMinMessageId();
            PagingOptions pagination = options.getPagination();

            // Get total count; Todo Move to AccountStaticRecommendationQuery
            TypedQuery<Number> countTipsQuery = entityManager.createQuery(
                "SELECT count(a.id) from account_static_recommendation a " +
                    "WHERE a.account.id = :accountId and a.id > :minMessageId ",
                 Number.class);
            countTipsQuery.setParameter("accountId", user.getId());
            countTipsQuery.setParameter("minMessageId", minMessageId);
            int totalTips = countTipsQuery.getSingleResult().intValue();
            result.setTotalTips(totalTips);

            // Build query; Todo Move to AccountStaticRecommendationQuery
            TypedQuery<AccountTipEntity> tipsQuery = entityManager.createQuery(
                "SELECT r FROM account_static_recommendation r " +
                    "WHERE r.account.id = :accountId and r.id > :minMessageId " +
                    "ORDER BY r.id " + (pagination.isAscending()? "ASC" : "DESC"),
                AccountTipEntity.class);

            tipsQuery.setFirstResult(pagination.getOffset());
            tipsQuery.setMaxResults(pagination.getLimit());
            tipsQuery.setParameter("accountId", user.getId());
            tipsQuery.setParameter("minMessageId", minMessageId);

            for (AccountTipEntity tip: tipsQuery.getResultList()) {
                Tip message = new Tip(tip.getId());
                message.setIndex(tip.getTip().getIndex());
                message.setTitle(tip.getTip().getTitle());
                message.setDescription(tip.getTip().getDescription());
                //message.setImageEncoded(tip.getRecommendation().getImage());
                message.setImageMimeType(tip.getTip().getImageMimeType());
                message.setImageLink(tip.getTip().getImageLink());
                message.setPrompt(tip.getTip().getPrompt());
                message.setExternalLink(tip.getTip().getExternalLink());
                message.setSource(tip.getTip().getSource());
                message.setCreatedOn(tip.getCreatedOn());
                if (tip.getTip().getModifiedOn() != null) {
                    message.setModifiedOn(tip.getTip().getModifiedOn());
                }
                if (tip.getAcknowledgedOn() != null) {
                    message.setAcknowledgedOn(tip.getAcknowledgedOn());
                }
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
    public void persistTipActiveStatus(int id, boolean active)
    {
        tipRepository.setActive(id, active);
    }

    @Override
    public void createTip(Tip tip, String lang)
    {
        TypedQuery<TipCategoryEntity> categoryQuery = entityManager.createQuery(
            "select c from static_recommendation_category c where c.id = :id",
            TipCategoryEntity.class);
        categoryQuery.setParameter("id", 7); //General Tips
        TipCategoryEntity category = categoryQuery.getSingleResult();

        // Todo: use a @GeneratedValue instead
        Integer maxIndex = entityManager.createQuery(
            "select max(s.index) from static_recommendation s", Integer.class).getSingleResult();
        int nextIndex = maxIndex+1;

        TipEntity tipEntity = new TipEntity();
        tipEntity.setIndex(nextIndex);
        tipEntity.setActive(false);
        tipEntity.setLocale(lang);
        tipEntity.setCategory(category);
        tipEntity.setTitle(tip.getTitle());
        tipEntity.setDescription(tip.getDescription());
        tipEntity.setCreatedOn(DateTime.now());

        this.entityManager.persist(tipEntity);
    }

    @Override
    public void updateTip(Tip tip)
    {
        TipEntity tipEntity = entityManager.find(TipEntity.class, tip.getId());
        if (tipEntity != null) {
            tipEntity.setTitle(tip.getTitle());
            tipEntity.setDescription(tip.getDescription());
            tipEntity.setModifiedOn(DateTime.now());
        }
    }

    @Override
    public void deleteTip(Tip tip)
    {
        TipEntity tipEntity = entityManager.find(TipEntity.class, tip.getId());
        if (tipEntity != null)
            entityManager.remove(tipEntity);
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
            receiver.setAcknowledgedOn(aa.getAcknowledgedOn());
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
            account.setAcknowledgedOn(alert.getAcknowledgedOn());
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
            account.setAcknowledgedOn(recommendation.getAcknowledgedOn());
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
    public void broadcastAnnouncement(AnnouncementRequest request, String lang, String channelName)
    {
        Locale locale = Locale.forLanguageTag(lang);
        Announcement announcement = request.getAnnouncement();

        // 1. Create announcement

        AnnouncementEntity announcementEntity = announcementRepository.createWith(
            Collections.singletonList(channelName),
            Collections.singletonMap(lang, (Message) announcement));

        // 2. Link announcement with receiver accounts

        for (ReceiverAccount receiver: request.getReceivers()) {
            AccountEntity accountEntity =
                userRepository.findOne(receiver.getAccountId());
            if (accountEntity == null)
                accountEntity = userRepository.getAccountByUsername(receiver.getUsername());
            if (accountEntity != null)
                accountAnnouncementRepository.createWith(accountEntity, announcementEntity);
        }
    }

    // Todo: Move to AccountStaticRecommendationRepository
    private void persistStaticRecommendationAcknowledgement(int id, DateTime acknowledgedOn)
    {
        AuthenticatedUser user = this.getCurrentAuthenticatedUser();

        TypedQuery<AccountTipEntity> accountStaticRecommendationQuery = entityManager.createQuery(
            "select a from account_static_recommendation a "
                + "where a.account.id = :accountId and a.id = :staticRecommendationId and a.acknowledgedOn is null",
            AccountTipEntity.class);

        accountStaticRecommendationQuery.setParameter("accountId", user.getId());
        accountStaticRecommendationQuery.setParameter("staticRecommendationId", id);

        List<AccountTipEntity> staticRecommendations = accountStaticRecommendationQuery.getResultList();
        if (staticRecommendations.size() == 1) {
            staticRecommendations.get(0).setAcknowledgedOn(acknowledgedOn);
            staticRecommendations.get(0).setReceiveAcknowledgedOn(DateTime.now());
        }
    }

    @Deprecated
    private Map.Entry<String, String> preprocessFormatParameter(String key, String value, Locale locale)
    {
        final String currencyKey1 = "currency1";
        final String currencyKey2 = "currency2";
        final String dayKey = "day_of_week";

        String key1 = key, value1 = value;
        // Transform (key, value) pair to (key1, value1)
        switch (key) {
            // Todo: replace with ICU message formatting directives (e.g. {x,number,currency})
            case currencyKey1:
            case currencyKey2: {
                NumberFormat numberFormatter = NumberFormat.getCurrencyInstance(locale);
                numberFormatter.setMaximumFractionDigits(1);
                float money = Float.parseFloat(value);
                value1 = numberFormatter.format(money);
                break;
            }
            // Todo: replace with message formatting directives (e.g. {x,date,EEEE})
            case dayKey: {
                int dayOfWeek = Integer.parseInt(value);
                value1 = (new DateTime()).withDayOfWeek(dayOfWeek).toString("EEEE");
            }
            default:
                // no-op
                break;
        }
        return new SimpleEntry<>(key1, value1);
    }
}