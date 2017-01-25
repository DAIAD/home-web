package eu.daiad.web.repository.application;

import java.text.NumberFormat;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
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
import eu.daiad.web.domain.application.AccountStaticRecommendationEntity;
import eu.daiad.web.domain.application.AnnouncementEntity;
import eu.daiad.web.domain.application.AnnouncementTranslationEntity;
import eu.daiad.web.domain.application.ChannelEntity;
import eu.daiad.web.domain.application.StaticRecommendationCategoryEntity;
import eu.daiad.web.domain.application.StaticRecommendationEntity;
import eu.daiad.web.model.PagingOptions;
import eu.daiad.web.model.error.MessageErrorCode;
import eu.daiad.web.model.error.SharedErrorCode;
import eu.daiad.web.model.message.Alert;
import eu.daiad.web.model.message.AlertStatistics;
import eu.daiad.web.model.message.Announcement;
import eu.daiad.web.model.message.AnnouncementRequest;
import eu.daiad.web.model.message.AnnouncementTranslation;
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
import eu.daiad.web.model.message.StaticRecommendation;
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
    IAccountRecommendationRepository accountRecommendationRepository;

    @Autowired
    IAccountAlertRepository accountAlertRepository;

    @Autowired
    IAccountAnnouncementRepository accountAnnouncementRepository;

    @Autowired
    IAccountStaticRecommendationRepository accountTipRepository;

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
            case RECOMMENDATION_STATIC:
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

        String localeName = user.getLocale();
        Locale locale = Locale.forLanguageTag(localeName);

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
                Announcement message = accountAnnouncementRepository.formatMessage(a, locale);
                if (message != null)
                    messages.add(message);
            }
        }

        //
        // Tips (static recommendations)
        //

        options = this.getMessageOptions(request, EnumMessageType.RECOMMENDATION_STATIC);
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
            TypedQuery<AccountStaticRecommendationEntity> tipsQuery = entityManager.createQuery(
                "SELECT r FROM account_static_recommendation r " +
                    "WHERE r.account.id = :accountId and r.id > :minMessageId " +
                    "ORDER BY r.id " + (pagination.isAscending()? "ASC" : "DESC"),
                AccountStaticRecommendationEntity.class);

            tipsQuery.setFirstResult(pagination.getOffset());
            tipsQuery.setMaxResults(pagination.getLimit());
            tipsQuery.setParameter("accountId", user.getId());
            tipsQuery.setParameter("minMessageId", minMessageId);

            for (AccountStaticRecommendationEntity tip: tipsQuery.getResultList()) {
                StaticRecommendation message = new StaticRecommendation();
                message.setId(tip.getId());
                message.setIndex(tip.getRecommendation().getIndex());
                message.setTitle(tip.getRecommendation().getTitle());
                message.setDescription(tip.getRecommendation().getDescription());
                //message.setImageEncoded(tip.getRecommendation().getImage());
                message.setImageMimeType(tip.getRecommendation().getImageMimeType());
                message.setImageLink(tip.getRecommendation().getImageLink());
                message.setPrompt(tip.getRecommendation().getPrompt());
                message.setExternalLink(tip.getRecommendation().getExternalLink());
                message.setSource(tip.getRecommendation().getSource());
                message.setCreatedOn(tip.getCreatedOn().getMillis());
                if (tip.getRecommendation().getModifiedOn() != null) {
                    message.setModifiedOn(tip.getRecommendation().getModifiedOn().getMillis());
                }
                if (tip.getAcknowledgedOn() != null) {
                    message.setAcknowledgedOn(tip.getAcknowledgedOn().getMillis());
                }
                messages.add(message);
            }
        }

        result.setMessages(messages);
        return result;
    }

    @Override
    public List<Message> getTips(String locale)
    {
        TypedQuery<StaticRecommendationEntity> tipsQuery = entityManager.createQuery(
            "SELECT a FROM static_recommendation a where a.locale = :locale",
            StaticRecommendationEntity.class);
        tipsQuery.setParameter("locale", locale);

        List<Message> messages = new ArrayList<>();
        for (StaticRecommendationEntity staticRecommendation : tipsQuery.getResultList()) {
            StaticRecommendation message = new StaticRecommendation();
            message.setId(staticRecommendation.getId());
            message.setIndex(staticRecommendation.getIndex());
            message.setTitle(staticRecommendation.getTitle());
            message.setDescription(staticRecommendation.getDescription());
            //message.setImageEncoded(staticRecommendation.getImage());
            message.setImageMimeType(staticRecommendation.getImageMimeType());
            message.setImageLink(staticRecommendation.getImageLink());
            message.setPrompt(staticRecommendation.getPrompt());
            message.setExternalLink(staticRecommendation.getExternalLink());
            message.setSource(staticRecommendation.getSource());
            if (staticRecommendation.getCreatedOn() != null)
                message.setCreatedOn(staticRecommendation.getCreatedOn().getMillis());
            if (staticRecommendation.getModifiedOn() != null)
                message.setModifiedOn(staticRecommendation.getModifiedOn().getMillis());
            message.setActive(staticRecommendation.isActive());
            messages.add(message);
        }
        return messages;
    }

    @Override
    public void persistAdvisoryMessageActiveStatus(int id, boolean active)
    {
        TypedQuery<StaticRecommendationEntity> advisoryMessage = entityManager.createQuery(
            "select s from static_recommendation s where s.id = :id",
            StaticRecommendationEntity.class);
        advisoryMessage.setParameter("id", id);
        List<StaticRecommendationEntity> advisoryMessages = advisoryMessage.getResultList();
        if(!advisoryMessages.isEmpty()){
            advisoryMessages.get(0).setActive(active);
        }
    }

    @Override
    public void persistNewAdvisoryMessage(eu.daiad.web.model.message.StaticRecommendation staticRecommendation)
    {
        AuthenticatedUser user = this.getCurrentAuthenticatedUser();

        TypedQuery<StaticRecommendationCategoryEntity> staticRecommendationCategoryQuery = entityManager
                        .createQuery("select c from static_recommendation_category c where c.id = :id",
                                        StaticRecommendationCategoryEntity.class);

        staticRecommendationCategoryQuery.setParameter("id", 7); //General Tips

        List<StaticRecommendationCategoryEntity> staticRecommendationsCategoryList = staticRecommendationCategoryQuery.getResultList();

        StaticRecommendationCategoryEntity category = null;
        if(staticRecommendationsCategoryList.size() == 1){
            category = staticRecommendationsCategoryList.get(0);
        }

        Integer maxIndex = entityManager.createQuery("select max(s.index) from static_recommendation s", Integer.class).getSingleResult();
        int nextIndex = maxIndex+1;

        StaticRecommendationEntity newStaticRecommendation = new StaticRecommendationEntity();
        newStaticRecommendation.setIndex(nextIndex);
        newStaticRecommendation.setActive(false);
        newStaticRecommendation.setLocale(user.getLocale());
        newStaticRecommendation.setCategory(category);
        newStaticRecommendation.setTitle(staticRecommendation.getTitle());
        newStaticRecommendation.setDescription(staticRecommendation.getDescription());
        newStaticRecommendation.setCreatedOn(DateTime.now());

        this.entityManager.persist(newStaticRecommendation);
    }

    @Override
    public void updateAdvisoryMessage(eu.daiad.web.model.message.StaticRecommendation staticRecommendation){

        TypedQuery<eu.daiad.web.domain.application.StaticRecommendationEntity> staticRecommendationQuery = entityManager
                        .createQuery("select s from static_recommendation s where s.id = :id",
                                        eu.daiad.web.domain.application.StaticRecommendationEntity.class);

        staticRecommendationQuery.setParameter("id", staticRecommendation.getId());

        List<StaticRecommendationEntity> staticRecommendations = staticRecommendationQuery.getResultList();

        if (staticRecommendations.size() == 1) {
            staticRecommendations.get(0).setTitle(staticRecommendation.getTitle());
            staticRecommendations.get(0).setDescription(staticRecommendation.getDescription());
            staticRecommendations.get(0).setModifiedOn(DateTime.now());
        }
    }

    @Override
    public void deleteAdvisoryMessage(eu.daiad.web.model.message.StaticRecommendation staticRecommendation)
    {
        TypedQuery<eu.daiad.web.domain.application.StaticRecommendationEntity> staticRecommendationQuery = entityManager
                        .createQuery("select s from static_recommendation s where s.id = :id",
                                        eu.daiad.web.domain.application.StaticRecommendationEntity.class).setFirstResult(0).setMaxResults(1);

        staticRecommendationQuery.setParameter("id", staticRecommendation.getId());

        List<StaticRecommendationEntity> staticRecommendations = staticRecommendationQuery.getResultList();

        if (staticRecommendations.size() == 1) {
            StaticRecommendationEntity toBeDeleted = staticRecommendations.get(0);
            this.entityManager.remove(toBeDeleted);
        }
    }

    @Override
    public void deleteAnnouncement(eu.daiad.web.model.message.Announcement announcement)
    {
        TypedQuery<eu.daiad.web.domain.application.AnnouncementEntity> announcementQuery = entityManager
                        .createQuery("select a from announcement a where a.id = :id",
                                        eu.daiad.web.domain.application.AnnouncementEntity.class).setFirstResult(0).setMaxResults(1);
        announcementQuery.setParameter("id", announcement.getId());

        List<AnnouncementEntity> announcements = announcementQuery.getResultList();

        if (announcements.size() == 1) {
            AnnouncementEntity toBeDeleted = announcements.get(0);
            this.entityManager.remove(toBeDeleted);
        }
    }

    @Override
    public List<Message> getAnnouncements(String locale)
    {
        List<Message> messages = new ArrayList<>();

        TypedQuery<AnnouncementTranslationEntity> accountAnnouncementsQuery = entityManager.createQuery(
            "select a from announcement_translation a where a.locale = :locale order by a.id desc",
            AnnouncementTranslationEntity.class);
        accountAnnouncementsQuery.setParameter("locale", locale);

        for (AnnouncementTranslationEntity announcementTranslation : accountAnnouncementsQuery.getResultList()) {
            AnnouncementTranslation message = new AnnouncementTranslation();
            message.setId(announcementTranslation.getId());
            message.setTitle(announcementTranslation.getTitle());
            message.setContent(announcementTranslation.getContent());
            messages.add(message);
        }
        return messages;
    }

    @Override
    public Announcement getAnnouncement(int id, String locale)
    {
        Announcement message = new Announcement();

        TypedQuery<AnnouncementTranslationEntity> accountAnnouncementQuery = entityManager.createQuery(
            "select a from announcement_translation a where a.locale = :locale and a.id = :id",
            AnnouncementTranslationEntity.class);
        accountAnnouncementQuery.setParameter("locale", locale);
        accountAnnouncementQuery.setParameter("id", id);

        List<AnnouncementTranslationEntity> announcements = accountAnnouncementQuery.getResultList();
        if(accountAnnouncementQuery.getResultList().size() == 1){
            AnnouncementTranslationEntity announcementTranslation = announcements.get(0);
            message.setId(announcementTranslation.getId());
            message.setTitle(announcementTranslation.getTitle());
            message.setContent(announcementTranslation.getContent());
        }
        return message;
    }

    @Override
    public List<ReceiverAccount> getAnnouncementReceivers(int announcementId)
    {
        List<ReceiverAccount> receivers = new ArrayList<>();

        TypedQuery<AccountAnnouncementEntity> accountAnnouncementQuery = entityManager.createQuery(
            "SELECT a FROM account_announcement a WHERE a.announcement.id = :id",
            AccountAnnouncementEntity.class);
        accountAnnouncementQuery.setParameter("id", announcementId);

        for (AccountAnnouncementEntity accountAnnouncement : accountAnnouncementQuery.getResultList()) {
            AccountEntity accountEntity = accountAnnouncement.getAccount();
            ReceiverAccount receiverAccount =
                new ReceiverAccount(accountEntity.getId(), accountEntity.getUsername());
            receiverAccount.setAcknowledgedOn(accountAnnouncement.getAcknowledgedOn());
            receivers.add(receiverAccount);
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

    // Todo Move to AccountAnnouncementRepository
    @Override
    public void broadcastAnnouncement(AnnouncementRequest request, String lang, String channelName)
    {
        Locale locale = Locale.forLanguageTag(lang);
        Announcement announcement = request.getAnnouncement();

        TypedQuery<ChannelEntity> channelQuery =
            entityManager.createQuery("FROM channel c WHERE c.name = :name", ChannelEntity.class);
        channelQuery.setParameter("name", channelName);
        ChannelEntity channel = channelQuery.getSingleResult();

        AnnouncementEntity announcementEntity = new AnnouncementEntity();
        announcementEntity.addChannel(channel);
        announcementEntity.addTranslation(locale, announcement.getTitle(), announcement.getContent());
        entityManager.persist(announcementEntity);

        DateTime created = DateTime.now();
        for (ReceiverAccount receiver : request.getReceiverAccountList()) {
            AccountEntity accountEntity =
                entityManager.find(AccountEntity.class, receiver.getAccountId());
            AccountAnnouncementEntity accountAnnouncementEntity =
                new AccountAnnouncementEntity(accountEntity, announcementEntity);
            accountAnnouncementEntity.setCreatedOn(created);
            entityManager.persist(accountAnnouncementEntity);
        }
    }

    // Todo: Move to AccountStaticRecommendationRepository
    private void persistStaticRecommendationAcknowledgement(int id, DateTime acknowledgedOn)
    {
        AuthenticatedUser user = this.getCurrentAuthenticatedUser();

        TypedQuery<AccountStaticRecommendationEntity> accountStaticRecommendationQuery = entityManager.createQuery(
            "select a from account_static_recommendation a "
                + "where a.account.id = :accountId and a.id = :staticRecommendationId and a.acknowledgedOn is null",
            AccountStaticRecommendationEntity.class);

        accountStaticRecommendationQuery.setParameter("accountId", user.getId());
        accountStaticRecommendationQuery.setParameter("staticRecommendationId", id);

        List<AccountStaticRecommendationEntity> staticRecommendations = accountStaticRecommendationQuery.getResultList();
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