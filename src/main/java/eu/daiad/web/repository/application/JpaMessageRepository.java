package eu.daiad.web.repository.application;

import java.sql.Date;
import java.text.NumberFormat;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.ibm.icu.text.MessageFormat;

import eu.daiad.web.domain.application.AccountAlertEntity;
import eu.daiad.web.domain.application.AccountAlertPropertyEntity;
import eu.daiad.web.domain.application.AccountAnnouncementEntity;
import eu.daiad.web.domain.application.AccountEntity;
import eu.daiad.web.domain.application.AccountRecommendationEntity;
import eu.daiad.web.domain.application.AccountStaticRecommendationEntity;
import eu.daiad.web.domain.application.AlertAnalyticsEntity;
import eu.daiad.web.domain.application.AlertEntity;
import eu.daiad.web.domain.application.AlertTranslationEntity;
import eu.daiad.web.domain.application.AnnouncementChannel;
import eu.daiad.web.domain.application.AnnouncementEntity;
import eu.daiad.web.domain.application.AnnouncementTranslationEntity;
import eu.daiad.web.domain.application.RecommendationAnalyticsEntity;
import eu.daiad.web.domain.application.StaticRecommendationCategoryEntity;
import eu.daiad.web.domain.application.StaticRecommendationEntity;
import eu.daiad.web.model.PagingOptions;
import eu.daiad.web.model.error.MessageErrorCode;
import eu.daiad.web.model.error.SharedErrorCode;
import eu.daiad.web.model.message.Alert;
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
import eu.daiad.web.model.message.StaticRecommendation;
import eu.daiad.web.model.query.TimeFilter;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.repository.BaseRepository;

@Repository
@Transactional("applicationTransactionManager")
public class JpaMessageRepository extends BaseRepository
    implements IMessageRepository
{
    @PersistenceContext(unitName = "default")
    EntityManager entityManager;

    @Autowired
    IAccountRecommendationRepository accountRecommendationRepository;

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
    public void setMessageAcknowledgement(AuthenticatedUser user, List<MessageAcknowledgement> messages)
    {
        UUID accountKey = user.getKey();
        for (MessageAcknowledgement message: messages) {
            int messageId = message.getId();
            DateTime acknowledged = new DateTime(message.getTimestamp());
            switch (message.getType()) {
            case ALERT:
                persistAlertAcknowledgement(messageId, acknowledged);
                break;
            case RECOMMENDATION:
                accountRecommendationRepository.acknowledge(accountKey, messageId, acknowledged);
                break;
            case RECOMMENDATION_STATIC:
                persistStaticRecommendationAcknowledgement(messageId, acknowledged);
                break;
            case ANNOUNCEMENT:
                persistAnnouncementAcknowledgement(messageId, acknowledged);
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
        // Get alerts
        //

        options = this.getMessageOptions(request, EnumMessageType.ALERT);
        if (options != null) {
            int minMessageId = options.getMinMessageId();
            PagingOptions pagination = options.getPagination();

            // Get total count; Todo Move to AccountAlertRepository
            TypedQuery<Number> countAlertsQuery = entityManager.createQuery(
                "SELECT count(a.id) from account_alert a "
                    + "WHERE a.account.id = :accountId and a.id > :minMessageId ",
                 Number.class);
            countAlertsQuery.setParameter("accountId", user.getId());
            countAlertsQuery.setParameter("minMessageId", minMessageId);
            int totalAlerts = countAlertsQuery.getSingleResult().intValue();

            result.setTotalAlerts(totalAlerts);

            // Build query; Todo Move to AccountAlertRepository
            TypedQuery<AccountAlertEntity> alertsQuery = entityManager.createQuery(
                "SELECT a FROM account_alert a " +
                    "WHERE a.account.id = :accountId and a.id > :minMessageId " +
                    "ORDER BY a.id " + (pagination.isAscending()? "ASC" : "DESC"),
                AccountAlertEntity.class);

            alertsQuery.setFirstResult(pagination.getOffset());
            alertsQuery.setMaxResults(pagination.getLimit());
            alertsQuery.setParameter("accountId", user.getId());
            alertsQuery.setParameter("minMessageId", minMessageId);

            for (AccountAlertEntity alert: alertsQuery.getResultList()) {
                // Find translation by locale
                AlertTranslationEntity alertTranslation = null;
                for (AlertTranslationEntity translation : alert.getAlert().getTranslations()) {
                    if (translation.getLocale().equals(localeName)) {
                        alertTranslation = translation;
                        break;
                    }
                }
                if (alertTranslation == null) {
                    continue;
                }

                // Build localized strings using translation and properties

                Map<String, String> formatParams = new HashMap<>();
                for (AccountAlertPropertyEntity p : alert.getProperties()) {
                    Map.Entry<String, String> p1 = preprocessFormatParameter(p.getKey(), p.getValue(), locale);
                    formatParams.put(p1.getKey(), p1.getValue());
                }

                MessageFormat titleTemplate = new MessageFormat(alertTranslation.getTitle(), locale);
                String title = titleTemplate.format(formatParams);

                String description = null;
                if (alertTranslation.getDescription() != null) {
                    MessageFormat descriptionTemplate = new MessageFormat(alertTranslation.getDescription(), locale);
                    description = descriptionTemplate.format(formatParams);
                }

                // Create message

                EnumAlertType alertType = EnumAlertType.fromInteger(alert.getAlert().getId());
                Alert message = new Alert(alertType, alert.getId());
                message.setPriority(alert.getAlert().getPriority());
                message.setTitle(title);
                message.setDescription(description);
                message.setImageLink(alertTranslation.getImageLink());
                message.setCreatedOn(alert.getCreatedOn().getMillis());
                if (alert.getAcknowledgedOn() != null) {
                    message.setAcknowledgedOn(alert.getAcknowledgedOn().getMillis());
                }

                messages.add(message);
            }
        }

        //
        // Get recommendations
        //

        options = this.getMessageOptions(request, EnumMessageType.RECOMMENDATION);
        if (options != null) {
            int minMessageId = options.getMinMessageId();
            PagingOptions pagination = options.getPagination();

            long n = accountRecommendationRepository.countByAccount(userKey, minMessageId);
            result.setTotalRecommendations((int) n);

            List<AccountRecommendationEntity> recommendations =
                accountRecommendationRepository.findByAccount(userKey, minMessageId, pagination);
            for (AccountRecommendationEntity r: recommendations) {
                Recommendation message = accountRecommendationRepository.formatMessage(r, locale);
                if (message != null)
                    messages.add(message);
            }
        }

        //
        // Get Announcements
        //

        options = this.getMessageOptions(request, EnumMessageType.ANNOUNCEMENT);
        if (options != null) {
            int minMessageId = options.getMinMessageId();
            PagingOptions pagination = options.getPagination();

            // Get total count; Todo move to AccountAnnouncementRepository
            TypedQuery<Number> countAnnouncementsQuery = entityManager.createQuery(
                "SELECT count(a.id) from account_announcement a "
                    + "WHERE a.account.id = :accountId and a.id > :minMessageId ",
                Number.class);

            countAnnouncementsQuery.setParameter("accountId", user.getId());
            countAnnouncementsQuery.setParameter("minMessageId", minMessageId);
            int totalAnnouncements = countAnnouncementsQuery.getSingleResult().intValue();
            result.setTotalAnnouncements(totalAnnouncements);

            // Build query; Todo move to AccountAnnouncementRepository
            TypedQuery<AccountAnnouncementEntity> announcementsQuery = entityManager.createQuery(
                "SELECT a FROM account_announcement a " +
                    "WHERE a.account.id = :accountId and a.id > :minMessageId " +
                    "ORDER BY a.id " + (pagination.isAscending()? "ASC" : "DESC"),
                AccountAnnouncementEntity.class);

            announcementsQuery.setFirstResult(pagination.getOffset());
            announcementsQuery.setMaxResults(pagination.getLimit());
            announcementsQuery.setParameter("accountId", user.getId());
            announcementsQuery.setParameter("minMessageId", minMessageId);

            for (AccountAnnouncementEntity announcement: announcementsQuery.getResultList()) {
                // Find translation by locale
                AnnouncementTranslationEntity announcementTranslation = null;
                for (AnnouncementTranslationEntity translation: announcement.getAnnouncement().getTranslations()) {
                    if (translation.getLocale().equals(localeName)) {
                        announcementTranslation = translation;
                        break;
                    }

                }
                if (announcementTranslation == null)
                    continue;

                Announcement message = new Announcement();
                message.setId(announcement.getId());
                message.setPriority(announcement.getAnnouncement().getPriority());
                message.setTitle(announcementTranslation.getTitle());
                if(announcementTranslation.getContent() != null){
                    message.setContent(announcementTranslation.getContent());
                }
                message.setCreatedOn(announcement.getCreatedOn().getMillis());
                if (announcement.getAcknowledgedOn() != null) {
                    message.setAcknowledgedOn(announcement.getAcknowledgedOn().getMillis());
                }
                messages.add(message);
            }
        }

        //
        // Get tips (static recommendations)
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
                    "ORDER BY a.id " + (pagination.isAscending()? "ASC" : "DESC"),
                AccountStaticRecommendationEntity.class);

            tipsQuery.setFirstResult(pagination.getOffset());
            tipsQuery.setMaxResults(pagination.getLimit());
            tipsQuery.setParameter("accountId", user.getId());
            tipsQuery.setParameter("minMessageId", minMessageId);

            for (AccountStaticRecommendationEntity tip : tipsQuery.getResultList()) {
                StaticRecommendation message = new StaticRecommendation();
                message.setId(tip.getId());
                message.setIndex(tip.getRecommendation().getIndex());
                message.setTitle(tip.getRecommendation().getTitle());
                message.setDescription(tip.getRecommendation().getDescription());
                message.setImageEncoded(tip.getRecommendation().getImage());
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
            if(announcementTranslation.getDispatchedOn() != null )
               message.setDispatchedOn(announcementTranslation.getDispatchedOn().getMillis());
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
            if(announcementTranslation.getDispatchedOn() != null ){
               message.setDispatchedOn(announcementTranslation.getDispatchedOn().getMillis());
            }
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
            ReceiverAccount receiverAccount = new ReceiverAccount();
            receiverAccount.setAccountId(accountAnnouncement.getAccount().getId());
            receiverAccount.setUsername(accountAnnouncement.getAccount().getUsername());
            receiverAccount.setAcknowledgedOn(accountAnnouncement.getAcknowledgedOn());
            receivers.add(receiverAccount);
        }
        return receivers;
    }

    @Override
    public List<AlertAnalyticsEntity> getAlertStatistics(String locale, int utilityId, MessageStatisticsQuery query)
    {
        //TODO - align sql dates with joda datetimes
        Date slqDateStart = new Date(query.getTime().getStart());
        Date slqDateEnd = new Date(query.getTime().getEnd());

        Query nativeQuery = entityManager.createNativeQuery("select\n" +
            "at.alert_id as id,\n" +
            "at.title as title,\n" +
            "at.description as description,\n" +
            "at.locale as locale,\n" +
            "count(distinct (aa.id)) as total\n" +
            "from\n" +
            "public.alert_translation at \n" +
            "left join account acc on acc.locale = at.locale\n" +
            "\n" +
            "left join public.account_alert aa on at.alert_id = aa.alert_id and acc.id=aa.account_id\n" +
            "where \n" +
            "(acc.utility_id=?1) and (aa.created_on >= ?2 and aa.created_on <= ?3 or aa.created_on is NULL)\n" +
            "group by\n" +
            "at.alert_id,at.title,at.locale, at.description", AlertAnalyticsEntity.class);

        nativeQuery.setParameter(1, utilityId);
        nativeQuery.setParameter(2, slqDateStart);
        nativeQuery.setParameter(3, slqDateEnd);

        List<AlertAnalyticsEntity> alertAnalytics = nativeQuery.getResultList();

        return alertAnalytics;
    }

    @Override
    public List<RecommendationAnalyticsEntity> getRecommendationStatistics(String locale, int utilityId, MessageStatisticsQuery query) {

        Date slqDateStart = new Date(query.getTime().getStart());
        Date slqDateEnd = new Date(query.getTime().getEnd());

        // Todo rewrite query
        Query nativeQuery = entityManager.createNativeQuery("select\n" +
            "rt.recommendation_id as id,\n" +
            "rt.title as title,\n" +
            "rt.description as description,\n" +
            "rt.locale as locale,\n" +
            "count(distinct (ar.id)) as total\n" +
            "from\n" +
            "public.recommendation_message rt \n" +
            "left join account acc on acc.locale = rt.locale\n" +
            "\n" +
            "left join public.account_recommendation ar on rt.recommendation_id = ar.recommendation_id and acc.id=ar.account_id\n" +
            "where \n" +
            "(acc.utility_id=?1) and (ar.created_on >= ?2 and ar.created_on <= ?3 or ar.created_on is NULL)\n" +
            "group by\n" +
            "rt.recommendation_id,rt.title,rt.locale, rt.description", RecommendationAnalyticsEntity.class);

        nativeQuery.setParameter(1, utilityId);
        nativeQuery.setParameter(2, slqDateStart);
        nativeQuery.setParameter(3, slqDateEnd);

        List<RecommendationAnalyticsEntity> recommendationAnalytics = nativeQuery.getResultList();

        return recommendationAnalytics;
    }

    @Override
    public List<ReceiverAccount> getAlertReceivers(int alertId, int utilityId, MessageStatisticsQuery query)
    {
        // Todo: Replace query with repo.findByType

        DateTime startDate = new DateTime(query.getTime().getStart());
        DateTime endDate = new DateTime(query.getTime().getEnd());

        TypedQuery<AccountAlertEntity> accountAlertQuery = entityManager.createQuery(
            "SELECT a FROM account_alert a " +
                "WHERE a.account.utility.id = :utilityId and a.alert.id = :id and a.createdOn > :startDate and a.createdOn < :endDate",
            AccountAlertEntity.class);

        accountAlertQuery.setParameter("utilityId", utilityId);
        accountAlertQuery.setParameter("id", alertId);
        accountAlertQuery.setParameter("startDate", startDate);
        accountAlertQuery.setParameter("endDate", endDate);

        List<ReceiverAccount> receivers = new ArrayList<>();
        for (AccountAlertEntity accountAlert : accountAlertQuery.getResultList()) {
            ReceiverAccount receiverAccount = new ReceiverAccount();
            receiverAccount.setAccountId(accountAlert.getAccount().getId());
            receiverAccount.setUsername(accountAlert.getAccount().getUsername());
            receiverAccount.setAcknowledgedOn(accountAlert.getAcknowledgedOn());
            receivers.add(receiverAccount);

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
            ReceiverAccount account = new ReceiverAccount();
            account.setAccountId(accountEntity.getId());
            account.setUsername(accountEntity.getUsername());
            account.setAcknowledgedOn(recommendation.getAcknowledgedOn());
            receivers.add(account);
        }
        return receivers;
    }

    @Override
    public Alert getAlert(int id, String locale)
    {
        TypedQuery<AlertTranslationEntity> query = entityManager.createQuery(
            "select a from alert_translation a where a.locale = :locale and a.id = :id",
            AlertTranslationEntity.class);
        query.setParameter("locale", locale);
        query.setParameter("id", id);

        Alert alert = null;
        AlertTranslationEntity translationEntity = null;
        try {
            translationEntity = query.getSingleResult();
        } catch (NoResultException ex) {
            translationEntity = null;
            alert = new Alert(EnumAlertType.UNDEFINED, -1);
        }

        if (translationEntity != null) {
            AlertEntity alertEntity = translationEntity.getAlert();
            alert = new Alert(EnumAlertType.fromInteger(alertEntity.getId()), -1);
            alert.setTitle(translationEntity.getTitle());
            alert.setDescription(translationEntity.getDescription());
        }

        return alert;
    }

    @Override
    public void broadcastAnnouncement(AnnouncementRequest announcementRequest, String locale, String channel)
    {
        TypedQuery<Integer> channelQuery = entityManager.createQuery(
            "SELECT c.id FROM channel c WHERE c.name = :name", Integer.class);
        channelQuery.setParameter("name", channel);
        int channelId = channelQuery.getSingleResult();

        AnnouncementTranslationEntity announcementTranslation = new AnnouncementTranslationEntity();
        announcementTranslation.setTitle(announcementRequest.getAnnouncement().getTitle());
        announcementTranslation.setContent(announcementRequest.getAnnouncement().getContent());
        announcementTranslation.setLocale(locale);
        announcementTranslation.setDispatchedOn(DateTime.now());

        this.entityManager.persist(announcementTranslation);

        AnnouncementEntity domainAnnouncement = new AnnouncementEntity();
        domainAnnouncement.setId(announcementTranslation.getId());
        domainAnnouncement.setPriority(1);

        announcementTranslation.setAnnouncement(domainAnnouncement);

        AnnouncementChannel announcementChannel = new AnnouncementChannel();
        announcementChannel.setAnnouncementId(domainAnnouncement.getId());
        announcementChannel.setChannelId(channelId);

        this.entityManager.persist(domainAnnouncement);
        this.entityManager.persist(announcementChannel);

        persistAccountAnnouncement(announcementRequest.getReceiverAccountList(), domainAnnouncement);
    }

    // Todo: Move to AccountAnnouncementRepository
    private void persistAccountAnnouncement(List<ReceiverAccount> receiverAccountList, AnnouncementEntity announcementEntity)
    {
        DateTime createdOn = DateTime.now();
        for (ReceiverAccount receiver : receiverAccountList) {
            AccountEntity account = entityManager.find(AccountEntity.class, receiver.getAccountId());
            if (account != null) {
                AccountAnnouncementEntity e = new AccountAnnouncementEntity();
                e.setAccount(account);
                e.setAnnouncement(announcementEntity);
                e.setCreatedOn(createdOn);
                entityManager.persist(e);
            }
        }
    }

    // Todo : When sending an acknowledgement for an alert of a specific type, an older (not acknowledged)
    // alert of the same type may appear in the next get messages call
    // Todo: Move to AccountAlertRepository
    private void persistAlertAcknowledgement(int id, DateTime acknowledgedOn)
    {
        AuthenticatedUser user = this.getCurrentAuthenticatedUser();

        TypedQuery<AccountAlertEntity> accountAlertsQuery = entityManager.createQuery(
            "select a from account_alert a "
                + "where a.account.id = :accountId and a.id = :alertId and a.acknowledgedOn is null",
            AccountAlertEntity.class);
        accountAlertsQuery.setParameter("accountId", user.getId());
        accountAlertsQuery.setParameter("alertId", id);

        List<AccountAlertEntity> alerts = accountAlertsQuery.getResultList();
        if (alerts.size() == 1) {
            alerts.get(0).setAcknowledgedOn(acknowledgedOn);
            alerts.get(0).setReceiveAcknowledgedOn(DateTime.now());
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

    // Todo: Move to AccountAnnouncementRepository
    private void persistAnnouncementAcknowledgement(int id, DateTime acknowledgedOn)
    {
        AuthenticatedUser user = this.getCurrentAuthenticatedUser();

        TypedQuery<AccountAnnouncementEntity> accountAnnouncementQuery = entityManager.createQuery(
            "select a from account_announcement a " +
                "where a.account.id = :accountId and a.id = :announcementId and a.acknowledgedOn is null",
            AccountAnnouncementEntity.class);

        accountAnnouncementQuery.setParameter("accountId", user.getId());
        accountAnnouncementQuery.setParameter("announcementId", id);

        List<AccountAnnouncementEntity> announcements = accountAnnouncementQuery.getResultList();

        if (announcements.size() == 1) {
            announcements.get(0).setAcknowledgedOn(acknowledgedOn);
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