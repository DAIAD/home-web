package eu.daiad.web.controller.action;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.daiad.web.controller.BaseController;
import eu.daiad.web.model.EnumApplication;
import eu.daiad.web.model.RestResponse;
import eu.daiad.web.model.message.AlertReceiversResponse;
import eu.daiad.web.model.message.Announcement;
import eu.daiad.web.model.message.AnnouncementDetailsResponse;
import eu.daiad.web.model.message.AnnouncementRequest;
import eu.daiad.web.model.message.EnumMessageType;
import eu.daiad.web.model.message.Message;
import eu.daiad.web.model.message.MessageAcknowledgementRequest;
import eu.daiad.web.model.message.MessageRequest;
import eu.daiad.web.model.message.MessageResult;
import eu.daiad.web.model.message.MessageStatisticsQuery;
import eu.daiad.web.model.message.MessageStatisticsQueryRequest;
import eu.daiad.web.model.message.MessageStatisticsResponse;
import eu.daiad.web.model.message.MultiTypeMessageResponse;
import eu.daiad.web.model.message.RecommendationReceiversResponse;
import eu.daiad.web.model.message.SingleTypeMessageResponse;
import eu.daiad.web.model.message.StaticRecommendation;
import eu.daiad.web.model.profile.Profile;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.model.security.RoleConstant;
import eu.daiad.web.repository.application.IMessageRepository;
import eu.daiad.web.repository.application.IProfileRepository;

/**
 * Provides actions for loading messages and saving acknowledgments.
 *
 */
@RestController
public class MessageController extends BaseController {

    /**
     * Logger instance for writing events using the configured logging API.
     */
    private static final Log logger = LogFactory.getLog(MessageController.class);

    /**
     * Default message channel.
     */
    private static final String DEFAULT_CHANNEL = "web";

    /**
     * Repository for accessing profile data.
     */
    @Autowired
    private IProfileRepository profileRepository;

    /**
     * Repository for accessing messages.
     */
    @Autowired
    private IMessageRepository messageRepository;

    /**
     * Loads messages i.e. alerts, recommendations and tips. Optionally filters messages.
     *
     * @param request the request.
     * @return the messages.
     */
    @RequestMapping(value = "/action/message", method = RequestMethod.POST, produces = "application/json")
    @Secured(RoleConstant.ROLE_USER)
    public RestResponse getMessages(@AuthenticationPrincipal AuthenticatedUser user, @RequestBody MessageRequest request) {
        try {
            MultiTypeMessageResponse messageResponse = new MultiTypeMessageResponse();

            Profile profile = profileRepository.getProfileByUsername(EnumApplication.HOME);
            if(!profile.isSendMessageEnabled()) {
                return messageResponse;
            }

            MessageResult result = messageRepository.getMessages(request);

            messageResponse.setTotalAlerts(result.getTotalAlerts());
            messageResponse.setTotalAnnouncements(result.getTotalAnnouncements());
            messageResponse.setTotalRecommendations(result.getTotalRecommendations());
            messageResponse.setTotalTips(result.getTotalTips());

            for (Message message : result.getMessages()) {
                switch (message.getType()) {
                    case ALERT:
                        messageResponse.getAlerts().add(message);
                        break;
                    case RECOMMENDATION_STATIC:
                        messageResponse.getTips().add(message);
                        break;
                    case RECOMMENDATION:
                        messageResponse.getRecommendations().add(message);
                        break;
                    case ANNOUNCEMENT:
                        messageResponse.getAnnouncements().add(message);
                        break;
                    default:
                        // Ignore
                }
            }

            return messageResponse;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            return new RestResponse(getError(ex));
        }
    }

    /**
     * Saves one or more message acknowledgments.
     *
     * @param request the messages to acknowledge.
     * @return the controller response.
     */
    @RequestMapping(value = "/action/message/acknowledge", method = RequestMethod.POST, produces = "application/json")
    @Secured(RoleConstant.ROLE_USER)
    public RestResponse acknowledgeMessage(@RequestBody MessageAcknowledgementRequest request) {
        RestResponse response = new RestResponse();

        try {
            messageRepository.setMessageAcknowledgement(request.getMessages());
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            response.add(this.getError(ex));
        }
        return response;
    }

    /**
     * Gets static localized recommendations (tips) based on locale.
     *
     * @param user the user
     * @param locale the locale
     * @return the static recommendations.
     */
    @RequestMapping(value = "/action/recommendation/static/{locale}", method = RequestMethod.GET, produces = "application/json")
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public RestResponse getRecommendations(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable String locale) {
        try {
            SingleTypeMessageResponse messages = new SingleTypeMessageResponse();

            messages.setType(EnumMessageType.RECOMMENDATION_STATIC);
            messages.setMessages(messageRepository.getAdvisoryMessages(locale));

            return messages;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            return new RestResponse(getError(ex));
        }
    }

    /**
     * Activate/Deactivate the received recommendations (tips).
     *
     * @param request the messages to change activity status
     * @return the controller response.
     */
    @RequestMapping(value = "/action/recommendation/static/status/save/", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public RestResponse setStaticTipsActivityStatusChange(@RequestBody List<StaticRecommendation> request) {
        RestResponse response = new RestResponse();
        try {
            for(StaticRecommendation st : request){
                messageRepository.persistAdvisoryMessageActiveStatus(st.getId(), st.isActive());
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            response.add(this.getError(ex));
        }
        return response;
    }

    /**
     * Add a new or edit an existing recommendation (tip).
     *
     * @param request the message to add or edit
     * @return the controller response.
     */
    @RequestMapping(value = "/action/recommendation/static/insert", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public RestResponse insertStaticRecommendation(@RequestBody StaticRecommendation request) {
        RestResponse response = new RestResponse();

        try {
            if(request.getId() == 0 || request.getIndex() == 0){
                messageRepository.persistNewAdvisoryMessage(request);
            }
            else{
                messageRepository.updateAdvisoryMessage(request);
            }
        } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);

                response.add(this.getError(ex));
        }
        return response;
    }

    /**
     * Delete an existing recommendation (tip).
     *
     * @param request the message to delete
     * @return the controller response.
     */
    @RequestMapping(value = "/action/recommendation/static/delete", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public RestResponse deleteStaticRecommendation(@RequestBody StaticRecommendation request) {
        RestResponse response = new RestResponse();

        try {
            messageRepository.deleteAdvisoryMessage(request);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            response.add(this.getError(ex));

        }
        return response;
    }

    /**
     * Send announcement to the provided accounts.
     *
     * @param user the user.
     * @param request the request containing the announcement and the receivers.
     * @return the controller response.
     */
    @RequestMapping(value = "/action/announcement/broadcast", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public RestResponse broadCastAnnouncement(@AuthenticationPrincipal AuthenticatedUser user, @RequestBody AnnouncementRequest request) {
        RestResponse response = new RestResponse();

        try {
            messageRepository.broadcastAnnouncement(request, user.getLocale(), DEFAULT_CHANNEL);
        } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);

                response.add(this.getError(ex));
        }
        return response;
    }

    /**
     * Get localized announcements history.
     *
     * @param user the user
     * @return the announcements.
     */
    @RequestMapping(value = "/action/announcement/history", method = RequestMethod.GET, produces = "application/json")
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public RestResponse getAnnouncements(@AuthenticationPrincipal AuthenticatedUser user) {
        try {
            SingleTypeMessageResponse messages = new SingleTypeMessageResponse();

            messages.setType(EnumMessageType.ANNOUNCEMENT);
            messages.setMessages(messageRepository.getAnnouncements(user.getLocale()));

            return messages;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            return new RestResponse(getError(ex));
        }
    }

    /**
     * Delete an existing announcement.
     *
     * @param user the user
     * @param request the announcement to delete
     * @return the controller response.
     */
    @RequestMapping(value = "/action/announcement/delete", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public RestResponse deleteAnnouncement(@AuthenticationPrincipal AuthenticatedUser user, @RequestBody Announcement request) {
        RestResponse response = new RestResponse();

        try {
            messageRepository.deleteAnnouncement(request);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            response.add(this.getError(ex));
        }
        return response;
    }

    /**
     * Get announcement details (including receiver accounts).
     *
     * @param user the user
     * @param id the announcement id
     * @return the announcement details.
     */
    @RequestMapping(value = "/action/announcement/details/{id}", method = RequestMethod.GET, produces = "application/json")
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public RestResponse getAnnouncementDetails(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable String id) {
        try {

            AnnouncementDetailsResponse announcementDetailsResponse = new AnnouncementDetailsResponse();

            int intId = Integer.parseInt(id);
            String locale = user.getLocale();

            announcementDetailsResponse.setAnnouncement(messageRepository.getAnnouncement(intId, locale));
            announcementDetailsResponse.setReceivers(messageRepository.getAnnouncementReceivers(intId));

            return announcementDetailsResponse;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            return new RestResponse(getError(ex));
        }
    }

    /**
     * Get message (alerts/recommendations) statistics (including count of receiver accounts).
     *
     * @param user the user
     * @param request the request
     * @return the message statistics.
     */
    @RequestMapping(value = "/action/recommendation/dynamic/statistics", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public RestResponse getMessageStatistics(@AuthenticationPrincipal AuthenticatedUser user, @RequestBody MessageStatisticsQueryRequest request) {
        try {

            MessageStatisticsQuery query = request.getQuery();

            // Set defaults if needed
            if (query != null) {
                // Initialize time zone
                if (StringUtils.isBlank(query.getTimezone())) {
                    query.setTimezone(user.getTimezone());
                }
            }

            int utilityId = user.getUtilityId();
            String localeName = user.getLocale();
            
            MessageStatisticsResponse response = new MessageStatisticsResponse();
            response.setAlertStatistics(
                messageRepository.getAlertStatistics(localeName, utilityId, query));
            response.setRecommendationStatistics(
                messageRepository.getRecommendationStatistics(localeName, utilityId, query));

            return response;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            return new RestResponse(getError(ex));
        }
    }

    /**
     * Get alert details receivers.
     *
     * @param user the user
     * @param id the alert id
     * @param request the request
     * @return the alert details, including receivers.
     */
    @RequestMapping(value = "/action/recommendation/dynamic/alert/receivers/{id}", method = RequestMethod.POST, produces = "application/json")
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public RestResponse getAlertReceivers(@AuthenticationPrincipal AuthenticatedUser user,
                                          @PathVariable String id,
                                          @RequestBody MessageStatisticsQueryRequest request) {
        try {
            int intId = Integer.parseInt(id);

            MessageStatisticsQuery query = request.getQuery();
            // Set defaults if needed
            if (query != null) {
                // Initialize time zone
                if (StringUtils.isBlank(query.getTimezone())) {
                    query.setTimezone(user.getTimezone());
                }
            }

            AlertReceiversResponse alertReceiversResponse = new AlertReceiversResponse();
            alertReceiversResponse.setAlert(messageRepository.getAlert(intId, user.getLocale()));
            alertReceiversResponse.setReceivers(messageRepository
                    .getAlertReceivers(intId, user.getUtilityId(), query));

            return alertReceiversResponse;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            return new RestResponse(getError(ex));
        }
    }

    /**
     * Get recommendation details and receivers.
     *
     * @param user the user
     * @param id the recommendation id
     * @param request the request
     * @return the announcement details.
     */
    @RequestMapping(value = "/action/recommendation/dynamic/recommendation/receivers/{id}", method = RequestMethod.POST, produces = "application/json")
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public RestResponse getRecommendationReceivers(@AuthenticationPrincipal AuthenticatedUser user,
                                                   @PathVariable String id,
                                                   @RequestBody MessageStatisticsQueryRequest request) {

        try {
            int intId = Integer.parseInt(id);

            MessageStatisticsQuery query = request.getQuery();

            // Set defaults if needed
            if (query != null) {
                // Initialize time zone
                if (StringUtils.isBlank(query.getTimezone())) {
                    query.setTimezone(user.getTimezone());
                }
            }

            RecommendationReceiversResponse recommendationReceiversResponse = new RecommendationReceiversResponse();
            recommendationReceiversResponse.setRecommendation(messageRepository
                    .getRecommendation(intId, user.getLocale()));
            recommendationReceiversResponse.setReceivers(messageRepository
                    .getRecommendationReceivers(intId, user.getUtilityId(), query));

            return recommendationReceiversResponse;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            return new RestResponse(getError(ex));
        }
    }

}
