package eu.daiad.web.repository.application;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.joda.time.DateTime;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.ibm.icu.text.MessageFormat;
import eu.daiad.web.domain.application.Account;

import eu.daiad.web.domain.application.AccountAlert;
import eu.daiad.web.domain.application.AccountAlertProperty;
import eu.daiad.web.domain.application.AccountAnnouncement;
import eu.daiad.web.domain.application.AccountDynamicRecommendation;
import eu.daiad.web.domain.application.AccountDynamicRecommendationProperty;
import eu.daiad.web.domain.application.AccountStaticRecommendation;
import eu.daiad.web.domain.application.AlertAnalytics;
import eu.daiad.web.domain.application.AlertTranslation;
import eu.daiad.web.domain.application.Announcement;
import eu.daiad.web.domain.application.AnnouncementChannel;
import eu.daiad.web.domain.application.AnnouncementTranslation;
import eu.daiad.web.domain.application.Channel;
import eu.daiad.web.domain.application.DynamicRecommendationTranslation;
import eu.daiad.web.domain.application.RecommendationAnalytics;
import eu.daiad.web.domain.application.StaticRecommendation;
import eu.daiad.web.domain.application.StaticRecommendationCategory;
import eu.daiad.web.model.error.MessageErrorCode;
import eu.daiad.web.model.error.SharedErrorCode;
import eu.daiad.web.model.message.Alert;
import eu.daiad.web.model.message.AnnouncementRequest;
import eu.daiad.web.model.message.DynamicRecommendation;
import eu.daiad.web.model.message.EnumAlertType;
import eu.daiad.web.model.message.EnumDynamicRecommendationType;
import eu.daiad.web.model.message.EnumMessageType;
import eu.daiad.web.model.message.Message;
import eu.daiad.web.model.message.MessageAcknowledgement;
import eu.daiad.web.model.message.MessageRequest;
import eu.daiad.web.model.message.MessageResult;
import eu.daiad.web.model.message.MessageStatisticsQuery;
import eu.daiad.web.model.message.ReceiverAccount;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.repository.BaseRepository;
import java.sql.Date;
import javax.persistence.Query;

@Repository
@Transactional("applicationTransactionManager")
public class JpaMessageRepository extends BaseRepository implements IMessageRepository {

	@PersistenceContext(unitName = "default")
	EntityManager entityManager;

	private final String currencyKey1 = "currency1";
	private final String currencyKey2 = "currency2";

	private AuthenticatedUser getCurrentAuthenticatedUser() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		if (auth.getPrincipal() instanceof AuthenticatedUser) {
			return (AuthenticatedUser) auth.getPrincipal();
		} else {
			throw createApplicationException(SharedErrorCode.AUTHORIZATION_ANONYMOUS_SESSION);
		}
	}

	@Override
	public void setMessageAcknowledgement(List<MessageAcknowledgement> messages) {            
		if (messages != null) {
			for (MessageAcknowledgement message : messages) {
				switch (message.getType()) {
					case ALERT:
						persistAlertAcknowledgement(message.getId(), new DateTime(message.getTimestamp()));
						break;
					case RECOMMENDATION_DYNAMIC:
						persistDynamicRecommendationAcknowledgement(message.getId(),
										new DateTime(message.getTimestamp()));
						break;
					case RECOMMENDATION_STATIC:
                        persistStaticRecommendationAcknowledgement(message.getId(),
										new DateTime(message.getTimestamp()));
						break;
					case ANNOUNCEMENT:
                        persistAnnouncementAcknowledgement(message.getId(),
										new DateTime(message.getTimestamp()));
                        break;
					default:
						throw createApplicationException(MessageErrorCode.MESSAGE_TYPE_NOT_SUPPORTED).set("type.",
										message.getType());
				}
			}
		}
	}

	private MessageRequest.DataPagingOptions getMessageDataPagingOptions(MessageRequest request, EnumMessageType type) {
		if (request.getPagination() != null) {
			for (MessageRequest.DataPagingOptions p : request.getPagination()) {
				if (p.getType().equals(type)) {
					return p;
				}
			}
		}

		return null;
	}

	@Override
	public MessageResult getMessages(MessageRequest request) {
		MessageResult result = new MessageResult();

		AuthenticatedUser user = this.getCurrentAuthenticatedUser();

		String locale = resolveLocale(user.getLocale());

		Locale currencySymbol = resolveCurrency(user.getCountry());

		List<Message> messages = new ArrayList<>();

		// Get alerts
		MessageRequest.DataPagingOptions options = this.getMessageDataPagingOptions(request, EnumMessageType.ALERT);

		if (options != null) {
			// Get total count
			Integer totalAlerts;

			TypedQuery<Number> countAccountAlertsQuery = entityManager
							.createQuery("select count(a.id) from account_alert a "
											+ "where a.account.id = :accountId and a.id > :minMessageId ", Number.class);

			countAccountAlertsQuery.setParameter("accountId", user.getId());
			countAccountAlertsQuery.setParameter("minMessageId", options.getMinMessageId());

			totalAlerts = ((Number) countAccountAlertsQuery.getSingleResult()).intValue();

			result.setTotalAlerts(totalAlerts);

			// Build query
			TypedQuery<eu.daiad.web.domain.application.AccountAlert> accountAlertsQuery;

			if ((options.getAscending() != null) && (options.getAscending() == true)) {
				// Ascending order
				accountAlertsQuery = entityManager.createQuery("select a from account_alert a "
								+ "where a.account.id = :accountId and a.id > :minMessageId order by a.id",
								eu.daiad.web.domain.application.AccountAlert.class);
			} else {
				// Descending order
				accountAlertsQuery = entityManager.createQuery("select a from account_alert a "
								+ "where a.account.id = :accountId and a.id > :minMessageId order by a.id desc",
								eu.daiad.web.domain.application.AccountAlert.class);
			}

			if (options.getIndex() != null) {
				accountAlertsQuery.setFirstResult(options.getIndex());
			}
			if (options.getSize() != null) {
				accountAlertsQuery.setMaxResults(options.getSize());
			}

			accountAlertsQuery.setParameter("accountId", user.getId());
			accountAlertsQuery.setParameter("minMessageId", options.getMinMessageId());

			for (eu.daiad.web.domain.application.AccountAlert accountAlert : accountAlertsQuery.getResultList()) {
				// Find translation by locale
				AlertTranslation alertTranslation = null;

				for (AlertTranslation translation : accountAlert.getAlert().getTranslations()) {
					if (translation.getLocale().equals(locale)) {
						alertTranslation = translation;
						break;
					}

				}

				if (alertTranslation == null) {
					continue;
				}

				// Build localized strings using translation and properties
				Map<String, String> formatProperties = new HashMap<>();

				for (AccountAlertProperty property : accountAlert.getProperties()) {
					setFormatProperties(property.getKey(), property.getValue(), formatProperties, currencySymbol);
				}

				MessageFormat titleTemplate = new MessageFormat(alertTranslation.getTitle(), currencySymbol);
				String title = titleTemplate.format(formatProperties);

				// Create message
				String description = null;

				if (alertTranslation.getDescription() != null) {
					MessageFormat descriptionTemplate = new MessageFormat(alertTranslation.getDescription(),
									new Locale(locale));
					description = descriptionTemplate.format(formatProperties);
				}
				eu.daiad.web.model.message.AccountAlert message = new eu.daiad.web.model.message.AccountAlert(
								EnumAlertType.fromInteger(accountAlert.getAlert().getId()));

				message.setId(accountAlert.getId());
				message.setPriority(accountAlert.getAlert().getPriority());
				message.setTitle(title);
				message.setDescription(description);
				message.setImageLink(alertTranslation.getImageLink());
				message.setCreatedOn(accountAlert.getCreatedOn().getMillis());
				if (accountAlert.getAcknowledgedOn() != null) {
					message.setAcknowledgedOn(accountAlert.getAcknowledgedOn().getMillis());
				}

				messages.add(message);
			}
		}

		// Get dynamic recommendations
		options = this.getMessageDataPagingOptions(request, EnumMessageType.RECOMMENDATION_DYNAMIC);

		if (options != null) {
			// Get total count
			Integer totalRecommendations;

			TypedQuery<Number> countAccountAlertsQuery = entityManager
							.createQuery("select count(a.id) from account_dynamic_recommendation a "
											+ "where a.account.id = :accountId and a.id > :minMessageId ", Number.class);

			countAccountAlertsQuery.setParameter("accountId", user.getId());
			countAccountAlertsQuery.setParameter("minMessageId", options.getMinMessageId());

			totalRecommendations = ((Number) countAccountAlertsQuery.getSingleResult()).intValue();

			result.setTotalRecommendations(totalRecommendations);

			// Build query
			TypedQuery<eu.daiad.web.domain.application.AccountDynamicRecommendation> accountRecommendationQuery;

			if ((options.getAscending() != null) && (options.getAscending() == true)) {
				// Ascending order
				accountRecommendationQuery = entityManager
								.createQuery("select a from account_dynamic_recommendation a "
												+ "where a.account.id = :accountId and a.id > :minMessageId order by a.id",
												eu.daiad.web.domain.application.AccountDynamicRecommendation.class);
			} else {
				// Descending order
				accountRecommendationQuery = entityManager
								.createQuery("select a from account_dynamic_recommendation a "
												+ "where a.account.id = :accountId and a.id > :minMessageId order by a.id desc",
												eu.daiad.web.domain.application.AccountDynamicRecommendation.class);
			}

			if (options.getIndex() != null) {
				accountRecommendationQuery.setFirstResult(options.getIndex());
			}
			if (options.getSize() != null) {
				accountRecommendationQuery.setMaxResults(options.getSize());
			}

			accountRecommendationQuery.setParameter("accountId", user.getId());
			accountRecommendationQuery.setParameter("minMessageId", options.getMinMessageId());

			for (eu.daiad.web.domain.application.AccountDynamicRecommendation accountRecommendation : accountRecommendationQuery
							.getResultList()) {
				// Find translation by locale
				DynamicRecommendationTranslation recommendationTranslation = null;

				for (DynamicRecommendationTranslation translation : accountRecommendation.getRecommendation()
								.getTranslations()) {
					if (translation.getLocale().equals(locale)) {
						recommendationTranslation = translation;
						break;
					}

				}

				if (recommendationTranslation == null) {
					continue;
				}

				// Build localized strings using translation and properties
				Map<String, String> formatProperties = new HashMap<>();

				for (AccountDynamicRecommendationProperty property : accountRecommendation.getProperties()) {
					setFormatProperties(property.getKey(), property.getValue(), formatProperties, currencySymbol);
				}

				MessageFormat titleTemplate = new MessageFormat(recommendationTranslation.getTitle(), currencySymbol);
				String title = titleTemplate.format(formatProperties);

				MessageFormat descriptionTemplate = new MessageFormat(recommendationTranslation.getDescription());
				String description = descriptionTemplate.format(formatProperties);

				// Create recommendation
				eu.daiad.web.model.message.AccountDynamicRecommendation message = new eu.daiad.web.model.message.AccountDynamicRecommendation(
								EnumDynamicRecommendationType.fromInteger(accountRecommendation.getRecommendation()
												.getId()));

				message.setId(accountRecommendation.getId());
				message.setPriority(accountRecommendation.getRecommendation().getPriority());
				message.setTitle(title);
				message.setDescription(description);
				message.setImageLink(recommendationTranslation.getImageLink());
				message.setCreatedOn(accountRecommendation.getCreatedOn().getMillis());
				if (accountRecommendation.getAcknowledgedOn() != null) {
					message.setAcknowledgedOn(accountRecommendation.getAcknowledgedOn().getMillis());
				}

				messages.add(message);

			}
		}

        //Get Announcements
		options = this.getMessageDataPagingOptions(request, EnumMessageType.ANNOUNCEMENT);

		if (options != null) {
			// Get total count
			Integer totalAnnouncements;

			TypedQuery<Number> countAccountAnnouncementsQuery = entityManager
							.createQuery("select count(a.id) from account_announcement a "
											+ "where a.account.id = :accountId and a.id > :minMessageId ", Number.class);

			countAccountAnnouncementsQuery.setParameter("accountId", user.getId());
			countAccountAnnouncementsQuery.setParameter("minMessageId", options.getMinMessageId());

			totalAnnouncements = ((Number) countAccountAnnouncementsQuery.getSingleResult()).intValue();

			result.setTotalAnnouncements(totalAnnouncements);

			// Build query
			TypedQuery<eu.daiad.web.domain.application.AccountAnnouncement> accountAnnouncementsQuery;

			if ((options.getAscending() != null) && (options.getAscending() == true)) {
				// Ascending order
				accountAnnouncementsQuery = entityManager.createQuery("select a from account_announcement a "
								+ "where a.account.id = :accountId and a.id > :minMessageId order by a.id",
								eu.daiad.web.domain.application.AccountAnnouncement.class);
			} else {
				// Descending order
				accountAnnouncementsQuery = entityManager.createQuery("select a from account_announcement a "
								+ "where a.account.id = :accountId and a.id > :minMessageId order by a.id desc",
								eu.daiad.web.domain.application.AccountAnnouncement.class);
			}

			if (options.getIndex() != null) {
				accountAnnouncementsQuery.setFirstResult(options.getIndex());
			}
			if (options.getSize() != null) {
				accountAnnouncementsQuery.setMaxResults(options.getSize());
			}

			accountAnnouncementsQuery.setParameter("accountId", user.getId());
			accountAnnouncementsQuery.setParameter("minMessageId", options.getMinMessageId());

			for (eu.daiad.web.domain.application.AccountAnnouncement accountAnnouncement : accountAnnouncementsQuery.getResultList()) {
				// Find translation by locale
				AnnouncementTranslation announcementTranslation = null;

				for (AnnouncementTranslation translation : accountAnnouncement.getAnnouncement().getTranslations()) {
					if (translation.getLocale().equals(locale)) {
						announcementTranslation = translation;
						break;
					}

				}

				if (announcementTranslation == null) {
					continue;
				}

				eu.daiad.web.model.message.AccountAnnouncement message = new eu.daiad.web.model.message.AccountAnnouncement();

				message.setId(accountAnnouncement.getId());
				message.setPriority(accountAnnouncement.getAnnouncement().getPriority());

                message.setTitle(announcementTranslation.getTitle());                              
                if(announcementTranslation.getContent() != null){
                    message.setContent(announcementTranslation.getContent());
                }
				
				message.setCreatedOn(accountAnnouncement.getCreatedOn().getMillis());
				if (accountAnnouncement.getAcknowledgedOn() != null) {
					message.setAcknowledgedOn(accountAnnouncement.getAcknowledgedOn().getMillis());
				}

				messages.add(message);
			}
		} 
        
        
		// Add a random static tip every week.
		options = this.getMessageDataPagingOptions(request, EnumMessageType.RECOMMENDATION_STATIC);

		if (options != null) {
			// Get total count
			Integer totalTips;

			TypedQuery<Number> countAccountAlertsQuery = entityManager
							.createQuery("select count(a.id) from account_static_recommendation a "
											+ "where a.account.id = :accountId and a.id > :minMessageId ", Number.class);

			countAccountAlertsQuery.setParameter("accountId", user.getId());
			countAccountAlertsQuery.setParameter("minMessageId", options.getMinMessageId());

			totalTips = ((Number) countAccountAlertsQuery.getSingleResult()).intValue();

			result.setTotalTips(totalTips);

			// Build query
			TypedQuery<eu.daiad.web.domain.application.AccountStaticRecommendation> accountTipQuery;

			if ((options.getAscending() != null) && (options.getAscending() == true)) {
				// Ascending order
				accountTipQuery = entityManager.createQuery("select a from account_static_recommendation a "
								+ "where a.account.id = :accountId and a.id > :minMessageId order by a.id",
								eu.daiad.web.domain.application.AccountStaticRecommendation.class);
			} else {
				// Descending order
				accountTipQuery = entityManager.createQuery("select a from account_static_recommendation a "
								+ "where a.account.id = :accountId and a.id > :minMessageId order by a.id desc",
								eu.daiad.web.domain.application.AccountStaticRecommendation.class);
			}

			if (options.getIndex() != null) {
				accountTipQuery.setFirstResult(options.getIndex());
			}
			if (options.getSize() != null) {
				accountTipQuery.setMaxResults(options.getSize());
			}

			accountTipQuery.setParameter("accountId", user.getId());
			accountTipQuery.setParameter("minMessageId", options.getMinMessageId());

			for (eu.daiad.web.domain.application.AccountStaticRecommendation tip : accountTipQuery.getResultList()) {
				eu.daiad.web.model.message.AccountStaticRecommendation message = new eu.daiad.web.model.message.AccountStaticRecommendation();

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
	public List<Message> getAdvisoryMessages(String locale) {
		List<Message> messages = new ArrayList<>();

		switch (locale) {
			case "en":
			case "es":
				// Ignore
				break;
			default:
				// Set default
				locale = "en";
		}

		TypedQuery<eu.daiad.web.domain.application.StaticRecommendation> accountAlertsQuery = entityManager
						.createQuery("select a from static_recommendation a where a.locale = :locale",
										eu.daiad.web.domain.application.StaticRecommendation.class);
		accountAlertsQuery.setParameter("locale", locale);

		for (StaticRecommendation staticRecommendation : accountAlertsQuery.getResultList()) {
			eu.daiad.web.model.message.StaticRecommendation message = new eu.daiad.web.model.message.StaticRecommendation();

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
			if (staticRecommendation.getCreatedOn() != null) {
				message.setCreatedOn(staticRecommendation.getCreatedOn().getMillis());
			}
			if (staticRecommendation.getModifiedOn() != null) {
				message.setModifiedOn(staticRecommendation.getModifiedOn().getMillis());
			}
			message.setActive(staticRecommendation.isActive());

			messages.add(message);
		}

		return messages;
	}

    @Override
    public void persistAdvisoryMessageActiveStatus(int id, boolean active){
        TypedQuery<eu.daiad.web.domain.application.StaticRecommendation> advisoryMessage = entityManager
                .createQuery("select s from static_recommendation s where s.id = :id",
                    eu.daiad.web.domain.application.StaticRecommendation.class);    
        advisoryMessage.setParameter("id", id);

        List<StaticRecommendation> advisoryMessages = advisoryMessage.getResultList();
            
        if(!advisoryMessages.isEmpty()){
            advisoryMessages.get(0).setActive(active);
        }
    }
        
    @Override
    public void persistNewAdvisoryMessage(eu.daiad.web.model.message.StaticRecommendation staticRecommendation){
        AuthenticatedUser user = this.getCurrentAuthenticatedUser();
        
  		TypedQuery<StaticRecommendationCategory> staticRecommendationCategoryQuery = entityManager
						.createQuery("select c from static_recommendation_category c where c.id = :id",
										StaticRecommendationCategory.class);

		staticRecommendationCategoryQuery.setParameter("id", 7); //General Tips

        List<StaticRecommendationCategory> staticRecommendationsCategoryList = staticRecommendationCategoryQuery.getResultList();    
        
        StaticRecommendationCategory category = null;
        if(staticRecommendationsCategoryList.size() == 1){
            category = staticRecommendationsCategoryList.get(0);
        }
                
        Integer maxIndex = entityManager.createQuery("select max(s.index) from static_recommendation s", Integer.class).getSingleResult();
        int nextIndex = maxIndex+1;
               
        StaticRecommendation newStaticRecommendation = new StaticRecommendation();
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

		TypedQuery<eu.daiad.web.domain.application.StaticRecommendation> staticRecommendationQuery = entityManager
						.createQuery("select s from static_recommendation s where s.id = :id",
										eu.daiad.web.domain.application.StaticRecommendation.class);

		staticRecommendationQuery.setParameter("id", staticRecommendation.getId());

		List<StaticRecommendation> staticRecommendations = staticRecommendationQuery.getResultList();

		if (staticRecommendations.size() == 1) {
            staticRecommendations.get(0).setTitle(staticRecommendation.getTitle());
            staticRecommendations.get(0).setDescription(staticRecommendation.getDescription());
			staticRecommendations.get(0).setModifiedOn(DateTime.now());
		}                          
    }

    @Override
    public void deleteAdvisoryMessage(eu.daiad.web.model.message.StaticRecommendation staticRecommendation){

		TypedQuery<eu.daiad.web.domain.application.StaticRecommendation> staticRecommendationQuery = entityManager
						.createQuery("select s from static_recommendation s where s.id = :id",
										eu.daiad.web.domain.application.StaticRecommendation.class).setFirstResult(0).setMaxResults(1);

		staticRecommendationQuery.setParameter("id", staticRecommendation.getId());

		List<StaticRecommendation> staticRecommendations = staticRecommendationQuery.getResultList();

		if (staticRecommendations.size() == 1) {
            
            StaticRecommendation toBeDeleted = staticRecommendations.get(0);
            this.entityManager.remove(toBeDeleted);    

		}                          
    }    

    @Override
    public void deleteAnnouncement(eu.daiad.web.model.message.Announcement announcement){

		TypedQuery<eu.daiad.web.domain.application.Announcement> announcementQuery = entityManager
						.createQuery("select a from announcement a where a.id = :id",
										eu.daiad.web.domain.application.Announcement.class).setFirstResult(0).setMaxResults(1);

		announcementQuery.setParameter("id", announcement.getId());

		List<Announcement> announcements = announcementQuery.getResultList();

		if (announcements.size() == 1) {
            
            Announcement toBeDeleted = announcements.get(0);
            this.entityManager.remove(toBeDeleted);    

		}                          
    } 
    
	@Override
	public List<Message> getAnnouncements(String locale) {
		List<Message> messages = new ArrayList<>();

		switch (locale) {
			case "en":
			case "es":
				// Ignore
				break;
			default:
				// Set default
				locale = "en";
		}

		TypedQuery<eu.daiad.web.domain.application.AnnouncementTranslation> accountAnnouncementsQuery = entityManager
						.createQuery("select a from announcement_translation a where a.locale = :locale order by a.id desc",
										eu.daiad.web.domain.application.AnnouncementTranslation.class);
		accountAnnouncementsQuery.setParameter("locale", locale);

		for (AnnouncementTranslation announcementTranslation : accountAnnouncementsQuery.getResultList()) {
			eu.daiad.web.model.message.AnnouncementTranslation message = new eu.daiad.web.model.message.AnnouncementTranslation();

			message.setId(announcementTranslation.getId());
			message.setTitle(announcementTranslation.getTitle());
			message.setContent(announcementTranslation.getContent());
            if(announcementTranslation.getDispatchedOn() != null ){
               message.setDispatchedOn(announcementTranslation.getDispatchedOn().getMillis()); 
            }
			messages.add(message);
		}

		return messages;
	}

	@Override
	public eu.daiad.web.model.message.Announcement getAnnouncement(int id, String locale) {
        
        eu.daiad.web.model.message.Announcement message = new eu.daiad.web.model.message.Announcement();

		switch (locale) {
			case "en":
			case "es":
				// Ignore
				break;
			default:
				// Set default
				locale = "en";
		}

		TypedQuery<eu.daiad.web.domain.application.AnnouncementTranslation> accountAnnouncementQuery = entityManager
						.createQuery("select a from announcement_translation a where a.locale = :locale and a.id = :id",
										eu.daiad.web.domain.application.AnnouncementTranslation.class);
		accountAnnouncementQuery.setParameter("locale", locale);
        accountAnnouncementQuery.setParameter("id", id);

        List<AnnouncementTranslation> announcements = accountAnnouncementQuery.getResultList();
        if(accountAnnouncementQuery.getResultList().size() == 1){
            AnnouncementTranslation announcementTranslation = announcements.get(0);
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
	public List<ReceiverAccount> getAnnouncementReceivers(int announcementId) {
        
        List<ReceiverAccount> receivers = new ArrayList<>();

		TypedQuery<eu.daiad.web.domain.application.AccountAnnouncement> accountAnnouncementQuery = entityManager
						.createQuery("select a from account_announcement a where a.announcement.id = :id",
										eu.daiad.web.domain.application.AccountAnnouncement.class);

        accountAnnouncementQuery.setParameter("id", announcementId);

		for (AccountAnnouncement accountAnnouncement : accountAnnouncementQuery.getResultList()) {
            
            ReceiverAccount receiverAccount = new ReceiverAccount();
            receiverAccount.setAccountId(accountAnnouncement.getAccount().getId());
            receiverAccount.setUsername(accountAnnouncement.getAccount().getUsername());
            receiverAccount.setLastName(accountAnnouncement.getAccount().getLastname());
            receiverAccount.setAcknowledgedOn(accountAnnouncement.getAcknowledgedOn());
            receivers.add(receiverAccount);
            
        }        
		return receivers;
	}
    
	@Override
	public List<AlertAnalytics> getAlertStatistics(String locale, int utilityId, MessageStatisticsQuery query) {

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
            "at.alert_id,at.title,at.locale, at.description", AlertAnalytics.class);
        
        nativeQuery.setParameter(1, utilityId);
        nativeQuery.setParameter(2, slqDateStart);
        nativeQuery.setParameter(3, slqDateEnd);
        
        List<AlertAnalytics> alertAnalytics = nativeQuery.getResultList();

		return alertAnalytics;
	}    

	@Override
	public List<RecommendationAnalytics> getRecommendationStatistics(String locale, int utilityId, MessageStatisticsQuery query) {
        
        Date slqDateStart = new Date(query.getTime().getStart());
        Date slqDateEnd = new Date(query.getTime().getEnd());

        Query nativeQuery = entityManager.createNativeQuery("select\n" +
            "rt.dynamic_recommendation_id as id,\n" +
            "rt.title as title,\n" +
            "rt.description as description,\n" +
            "rt.locale as locale,\n" +
            "count(distinct (ar.id)) as total\n" +
            "from\n" +
            "public.dynamic_recommendation_translation rt \n" +
            "left join account acc on acc.locale = rt.locale\n" +
            "\n" +
            "left join public.account_dynamic_recommendation ar on rt.dynamic_recommendation_id = ar.dynamic_recommendation_id and acc.id=ar.account_id\n" +
            "where \n" +
            "(acc.utility_id=?1) and (ar.created_on >= ?2 and ar.created_on <= ?3 or ar.created_on is NULL)\n" +
            "group by\n" +
            "rt.dynamic_recommendation_id,rt.title,rt.locale, rt.description", RecommendationAnalytics.class);

        nativeQuery.setParameter(1, utilityId);
        nativeQuery.setParameter(2, slqDateStart);
        nativeQuery.setParameter(3, slqDateEnd);
        
        List<RecommendationAnalytics> recommendationAnalytics = nativeQuery.getResultList();

		return recommendationAnalytics;
	}
    
	@Override
	public List<ReceiverAccount> getAlertReceivers(int alertId, int utilityId, MessageStatisticsQuery query) {
        
        DateTime startDate = new DateTime(query.getTime().getStart());
        DateTime endDate = new DateTime(query.getTime().getEnd());
         
        
        List<ReceiverAccount> receivers = new ArrayList<>();

		TypedQuery<eu.daiad.web.domain.application.AccountAlert> accountAlertQuery = entityManager
						.createQuery("select a from account_alert a where a.account.utility.id = :utilityId and a.alert.id = :id and a.createdOn > :startDate and a.createdOn < :endDate",
										eu.daiad.web.domain.application.AccountAlert.class);

        accountAlertQuery.setParameter("utilityId", utilityId);
        accountAlertQuery.setParameter("id", alertId);
        accountAlertQuery.setParameter("startDate", startDate);
        accountAlertQuery.setParameter("endDate", endDate);        

		for (AccountAlert accountAlert : accountAlertQuery.getResultList()) {
            
            ReceiverAccount receiverAccount = new ReceiverAccount();
            receiverAccount.setAccountId(accountAlert.getAccount().getId());
            receiverAccount.setUsername(accountAlert.getAccount().getUsername());
            receiverAccount.setLastName(accountAlert.getAccount().getLastname());
            receiverAccount.setAcknowledgedOn(accountAlert.getAcknowledgedOn());
            receivers.add(receiverAccount);
            
        }        
		return receivers;
	}

	@Override
	public List<ReceiverAccount> getRecommendationReceivers(int recommendationId, int utilityId, MessageStatisticsQuery query) {
        
        DateTime startDate = new DateTime(query.getTime().getStart());
        DateTime endDate = new DateTime(query.getTime().getEnd());        
        
        List<ReceiverAccount> receivers = new ArrayList<>();

		TypedQuery<eu.daiad.web.domain.application.AccountDynamicRecommendation> accountRecommendationQuery = entityManager
						.createQuery("select a from account_dynamic_recommendation a where a.account.utility.id = :utilityId and a.recommendation = :id and a.createdOn > :startDate and a.createdOn < :endDate",
										eu.daiad.web.domain.application.AccountDynamicRecommendation.class);
        
        accountRecommendationQuery.setParameter("utilityId", utilityId);
        accountRecommendationQuery.setParameter("id", recommendationId);
        accountRecommendationQuery.setParameter("startDate", startDate);
        accountRecommendationQuery.setParameter("endDate", endDate);        

		for (AccountDynamicRecommendation accountRecommendation : accountRecommendationQuery.getResultList()) {
            
            ReceiverAccount receiverAccount = new ReceiverAccount();
            receiverAccount.setAccountId(accountRecommendation.getAccount().getId());
            receiverAccount.setUsername(accountRecommendation.getAccount().getUsername());
            receiverAccount.setLastName(accountRecommendation.getAccount().getLastname());
            receiverAccount.setAcknowledgedOn(accountRecommendation.getAcknowledgedOn());
            receivers.add(receiverAccount);
            
        }        
		return receivers;
	}

	@Override
	public Alert getAlert(int id, String locale) {
        
        Alert alert = new Alert(EnumAlertType.UNDEFINED);
		switch (locale) {
			case "en":
			case "es":
				// Ignore
				break;
			default:
				// Set default
				locale = "en";
		}

		TypedQuery<eu.daiad.web.domain.application.AlertTranslation> accountAlertQuery = entityManager
						.createQuery("select a from alert_translation a where a.locale = :locale and a.id = :id",
										eu.daiad.web.domain.application.AlertTranslation.class);
		accountAlertQuery.setParameter("locale", locale);
        accountAlertQuery.setParameter("id", id);

        List<AlertTranslation> alerts = accountAlertQuery.getResultList();
        if(accountAlertQuery.getResultList().size() == 1){
            AlertTranslation alertTranslation = alerts.get(0);
			alert.setId(alertTranslation.getId());
			alert.setTitle(alertTranslation.getTitle());
			alert.setDescription(alertTranslation.getDescription());

        }

		return alert;
	}
    
	@Override
	public DynamicRecommendation getRecommendation(int id, String locale) {
        
        DynamicRecommendation recommendation = new DynamicRecommendation(EnumDynamicRecommendationType.UNDEFINED);
		switch (locale) {
			case "en":
			case "es":
				// Ignore
				break;
			default:
				// Set default
				locale = "en";
		}

		TypedQuery<eu.daiad.web.domain.application.DynamicRecommendationTranslation> accountRecommendationQuery = entityManager
						.createQuery("select a from dynamic_recommendation_translation a where a.locale = :locale and a.id = :id",
										eu.daiad.web.domain.application.DynamicRecommendationTranslation.class);
		accountRecommendationQuery.setParameter("locale", locale);
        accountRecommendationQuery.setParameter("id", id);

        List<DynamicRecommendationTranslation> recommendations = accountRecommendationQuery.getResultList();
        if(accountRecommendationQuery.getResultList().size() == 1){
            DynamicRecommendationTranslation recommendationTranslation = recommendations.get(0);
			recommendation.setId(recommendationTranslation.getId());
			recommendation.setTitle(recommendationTranslation.getTitle());
			recommendation.setDescription(recommendationTranslation.getDescription());
        }

		return recommendation;
	}
    
    @Override
    public void broadcastAnnouncement(AnnouncementRequest announcementRequest, String locale, String channel){
        AuthenticatedUser user = this.getCurrentAuthenticatedUser();
         
		TypedQuery<eu.daiad.web.domain.application.Channel> channelQuery = entityManager
						.createQuery("select c from channel c where c.name = :name",
										eu.daiad.web.domain.application.Channel.class).setFirstResult(0).setMaxResults(1);

		channelQuery.setParameter("name", channel);
        List<Channel> channels = channelQuery.getResultList();
        
        int channelId = 1;
        if(channels.size() == 1){
            Channel c = channels.get(0);
            channelId = c.getId();
        }

        AnnouncementTranslation announcementTranslation = new AnnouncementTranslation();
        announcementTranslation.setTitle(announcementRequest.getAnnouncement().getTitle());
        announcementTranslation.setContent(announcementRequest.getAnnouncement().getContent());
        announcementTranslation.setLocale(locale);
        announcementTranslation.setDispatchedOn(DateTime.now());
        
		this.entityManager.persist(announcementTranslation);
        this.entityManager.flush();
        
        Announcement domainAnnouncement = new Announcement();
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
    
    private void persistAccountAnnouncement(List<ReceiverAccount> receiverAccountList, Announcement domainAnnouncement) {
        DateTime createdOn = DateTime.now();

        for(ReceiverAccount receiver : receiverAccountList){
            
            TypedQuery<eu.daiad.web.domain.application.Account> accountQuery = entityManager
                            .createQuery("select a from account a where a.id = :id",
                                            eu.daiad.web.domain.application.Account.class).setFirstResult(0).setMaxResults(1);           
            accountQuery.setParameter("id", receiver.getAccountId());    
            List<Account> accounts = accountQuery.getResultList();    
            Account receiverAccount= null;
            
            if(accounts.size() == 1){
                receiverAccount = accounts.get(0);
            }
        
            if(receiverAccount != null){
                AccountAnnouncement accountAnnouncement = new AccountAnnouncement();
                accountAnnouncement.setAccount(receiverAccount); 
                accountAnnouncement.setAnnouncement(domainAnnouncement);
                accountAnnouncement.setCreatedOn(createdOn);
                
                this.entityManager.persist(accountAnnouncement);
            }               
        }
    }    
    
    
	// TODO : When sending an acknowledgement for an alert of a specific type,
	// an older (not acknowledged) alert of the same type may appear in the next
	// get messages call

	private void persistAlertAcknowledgement(int id, DateTime acknowledgedOn) {
		AuthenticatedUser user = this.getCurrentAuthenticatedUser();

		TypedQuery<eu.daiad.web.domain.application.AccountAlert> accountAlertsQuery = entityManager
						.createQuery("select a from account_alert a "
										+ "where a.account.id = :accountId and a.id = :alertId and a.acknowledgedOn is null",
										eu.daiad.web.domain.application.AccountAlert.class);

		accountAlertsQuery.setParameter("accountId", user.getId());
		accountAlertsQuery.setParameter("alertId", id);

		List<AccountAlert> alerts = accountAlertsQuery.getResultList();

		if (alerts.size() == 1) {
			alerts.get(0).setAcknowledgedOn(acknowledgedOn);
			alerts.get(0).setReceiveAcknowledgedOn(DateTime.now());
		}
	}

	private void persistDynamicRecommendationAcknowledgement(int id, DateTime acknowledgedOn) {
		AuthenticatedUser user = this.getCurrentAuthenticatedUser();

		TypedQuery<eu.daiad.web.domain.application.AccountDynamicRecommendation> accountDynamicRecommendationQuery = entityManager
						.createQuery("select a from account_dynamic_recommendation a "
										+ "where a.account.id = :accountId and a.id = :dynamicRecommendationId and a.acknowledgedOn is null",
										eu.daiad.web.domain.application.AccountDynamicRecommendation.class);

		accountDynamicRecommendationQuery.setParameter("accountId", user.getId());
		accountDynamicRecommendationQuery.setParameter("dynamicRecommendationId", id);

		List<AccountDynamicRecommendation> recommendations = accountDynamicRecommendationQuery.getResultList();

		if (recommendations.size() == 1) {
			recommendations.get(0).setAcknowledgedOn(acknowledgedOn);
			recommendations.get(0).setReceiveAcknowledgedOn(DateTime.now());
		}
	}
        
	private void persistStaticRecommendationAcknowledgement(int id, DateTime acknowledgedOn) {
		AuthenticatedUser user = this.getCurrentAuthenticatedUser();

		TypedQuery<eu.daiad.web.domain.application.AccountStaticRecommendation> accountStaticRecommendationQuery = entityManager
						.createQuery("select a from account_static_recommendation a "
                                                                                + "where a.account.id = :accountId and a.id = :staticRecommendationId and a.acknowledgedOn is null",
										eu.daiad.web.domain.application.AccountStaticRecommendation.class);

		accountStaticRecommendationQuery.setParameter("accountId", user.getId());
		accountStaticRecommendationQuery.setParameter("staticRecommendationId", id);

		List<AccountStaticRecommendation> staticRecommendations = accountStaticRecommendationQuery.getResultList();

		if (staticRecommendations.size() == 1) {
			staticRecommendations.get(0).setAcknowledgedOn(acknowledgedOn);
			staticRecommendations.get(0).setReceiveAcknowledgedOn(DateTime.now());
		}
	}     
    
	private void persistAnnouncementAcknowledgement(int id, DateTime acknowledgedOn) {
		AuthenticatedUser user = this.getCurrentAuthenticatedUser();

		TypedQuery<eu.daiad.web.domain.application.AccountAnnouncement> accountAnnouncementQuery = entityManager
						.createQuery("select a from account_announcement a "
                                        + "where a.account.id = :accountId and a.id = :announcementId and a.acknowledgedOn is null",
										eu.daiad.web.domain.application.AccountAnnouncement.class);

		accountAnnouncementQuery.setParameter("accountId", user.getId());
		accountAnnouncementQuery.setParameter("announcementId", id);

		List<AccountAnnouncement> announcements = accountAnnouncementQuery.getResultList();

		if (announcements.size() == 1) {
			announcements.get(0).setAcknowledgedOn(acknowledgedOn);
		}
	}     

	private Locale resolveCurrency(String country) {
		Locale currency;

        if(country == null){
            return Locale.GERMANY;
        }
        
		// TODO: check fixed values of countries
		switch (country) {
			case "United Kingdom":
				// currencyRate = "GBP";
				currency = Locale.UK;
				break;
			default:
				// currencyRate = "EUR";
				currency = Locale.GERMANY;
		}
		return currency;
	}

	private String resolveLocale(String locale) {
		switch (locale) {
			case "en":
			case "es":
				// Ignore
				break;
			default:
				// Set default
				locale = "en";
				break;
		}
		return locale;
	}

	private void setFormatProperties(String key, String value, Map<String, String> formatProperties,
					Locale currencySymbol) {
		switch (key) {
			case currencyKey1: {
				NumberFormat numberFormat = NumberFormat.getCurrencyInstance(currencySymbol);
				numberFormat.setMaximumFractionDigits(1);
				float euros = Float.parseFloat(value);
				float money = convertCurrencyIfNeed(euros, currencySymbol);

				String currencyFormatted = numberFormat.format(money);
				formatProperties.put(key, currencyFormatted);
				break;
			}
			case currencyKey2: {
				NumberFormat numberFormat = NumberFormat.getCurrencyInstance(currencySymbol);
				numberFormat.setMaximumFractionDigits(1);
				float euros = Float.parseFloat(value);
				float money = convertCurrencyIfNeed(euros, currencySymbol);

				String currencyFormatted = numberFormat.format(money);
				formatProperties.put(key, currencyFormatted);
				break;
			}
			default:
				formatProperties.put(key, value);
				break;
		}
	}

	private float convertCurrencyIfNeed(float euros, Locale currencySymbol) {
		// this is dummy method for future use. Currently returns only euros.
		// The currency is converted in the message computation for now and only
		// for KWH prices
		if (currencySymbol.equals(Locale.GERMANY)) {
			return euros;
		} else if (currencySymbol.equals(Locale.UK)) {
			return euros;
			// return (float) (euros*0.8); //get currency rate from db
		} else {
			return euros;
		}
	}

	// private float convertEURtoGBP(float euros){
	// TypedQuery<eu.daiad.web.domain.Currency> query = entityManager
	// .createQuery("select a from currency a where a.ISO_code = :ISO_code",
	// eu.daiad.web.domain.Currency.class).setFirstResult(0)
	// .setMaxResults(1);
	// query.setParameter("ISO_code", "EUR");
	// Currency c = query.getSingleResult();
	// float p = c.getToGbp();
	//
	// return 4;
	// }

}

//select at.alert_id as id, at.title as title, at.description as description,at.locale as locale,count(distinct (aa.id)) as total 
//from public.alert_translation at left join account acc on acc.locale = at.locale left join public.account_alert aa on at.alert_id = aa.alert_id and acc.id=aa.account_id
//where 
//            (acc.utility_id=?1) and (aa.created_on > ?2 and aa.created_on < ?3 or aa.created_on is NULL)
//            group by
//            at.alert_id,at.title,at.locale, at.description