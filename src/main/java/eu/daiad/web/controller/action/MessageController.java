package eu.daiad.web.controller.action;

import java.util.List;
import java.util.UUID;

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
import eu.daiad.web.model.message.AlertStatisticsRequest;
import eu.daiad.web.model.message.Announcement;
import eu.daiad.web.model.message.AnnouncementDetailsResponse;
import eu.daiad.web.model.message.AnnouncementRequest;
import eu.daiad.web.model.message.EnumAlertType;
import eu.daiad.web.model.message.EnumMessageType;
import eu.daiad.web.model.message.EnumRecommendationType;
import eu.daiad.web.model.message.MessageAcknowledgementRequest;
import eu.daiad.web.model.message.MessageRequest;
import eu.daiad.web.model.message.MessageResult;
import eu.daiad.web.model.message.MessageStatisticsQuery;
import eu.daiad.web.model.message.MessageStatisticsRequest;
import eu.daiad.web.model.message.MessageStatisticsResponse;
import eu.daiad.web.model.message.MultiTypeMessageResponse;
import eu.daiad.web.model.message.ReceiverAccount;
import eu.daiad.web.model.message.RecommendationReceiversResponse;
import eu.daiad.web.model.message.RecommendationStatisticsRequest;
import eu.daiad.web.model.message.SingleTypeMessageResponse;
import eu.daiad.web.model.message.StaticRecommendation;
import eu.daiad.web.model.profile.Profile;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.model.security.RoleConstant;
import eu.daiad.web.repository.application.IMessageRepository;
import eu.daiad.web.repository.application.IProfileRepository;

/**
 * Provides actions for loading messages and saving acknowledgments.
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
    public RestResponse getMessages(
        @AuthenticationPrincipal AuthenticatedUser user, @RequestBody MessageRequest request)
    {
        try {
            Profile profile = profileRepository.getProfileByUsername(EnumApplication.HOME);
            if(!profile.isSendMessageEnabled()) {
                return new MultiTypeMessageResponse();
            } else {
                MessageResult result = messageRepository.getMessages(user, request);
                return new MultiTypeMessageResponse(result);
            }
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
    public RestResponse acknowledgeMessage(
        @AuthenticationPrincipal AuthenticatedUser user, @RequestBody MessageAcknowledgementRequest request)
    {
        RestResponse response = new RestResponse();
        try {
            messageRepository.acknowledgeMessages(user, request.getMessages());
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
    public RestResponse getRecommendations(
        @AuthenticationPrincipal AuthenticatedUser user, @PathVariable String locale)
    {
        try {
            SingleTypeMessageResponse messages = new SingleTypeMessageResponse();
            messages.setType(EnumMessageType.RECOMMENDATION_STATIC);
            messages.setMessages(messageRepository.getTips(locale));
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
            for (StaticRecommendation st : request){
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
    public RestResponse deleteStaticRecommendation(@RequestBody StaticRecommendation request)
    {
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
    public RestResponse broadCastAnnouncement(
        @AuthenticationPrincipal AuthenticatedUser user, @RequestBody AnnouncementRequest request)
    {
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
    public RestResponse getMessageStatistics(
        @AuthenticationPrincipal AuthenticatedUser user, @RequestBody MessageStatisticsRequest request)
    {
        try {
            MessageStatisticsQuery query = request.getQuery();

            if (query != null) {
                if (StringUtils.isBlank(query.getTimezone())) {
                    query.setTimezone(user.getTimezone());
                }
            }

            UUID utilityKey = user.getUtilityKey();
            MessageStatisticsResponse response = new MessageStatisticsResponse();

            response.setAlertStatistics(
                messageRepository.getAlertStatistics(utilityKey, query));

            response.setRecommendationStats(
                messageRepository.getRecommendationStatistics(utilityKey, query));

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
     * @param request the request
     * @return the alert details, including receivers.
     */
    @RequestMapping(value = "/action/recommendation/dynamic/alert/receivers", method = RequestMethod.POST, produces = "application/json")
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public RestResponse getAlertReceivers(
        @AuthenticationPrincipal AuthenticatedUser user, @RequestBody AlertStatisticsRequest request)
    {
        try {
            EnumAlertType alertType = request.getType();
            UUID utilityKey = user.getUtilityKey();

            MessageStatisticsQuery query = request.getQuery();
            if (query != null) {
                if (StringUtils.isBlank(query.getTimezone()))
                    query.setTimezone(user.getTimezone());
            }

            List<ReceiverAccount> receivers =
                messageRepository.getAlertReceivers(alertType, utilityKey, query);
            return new AlertReceiversResponse(alertType, receivers);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return new RestResponse(getError(ex));
        }
    }

    /**
     * Get recommendation details and receivers.
     *
     * @param user the user
     * @param request the request
     * @return the announcement details.
     */
    @RequestMapping(value = "/action/recommendation/dynamic/recommendation/receivers", method = RequestMethod.POST, produces = "application/json")
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public RestResponse getRecommendationReceivers(
        @AuthenticationPrincipal AuthenticatedUser user, @RequestBody RecommendationStatisticsRequest request)
    {
        try {
            EnumRecommendationType recommendationType = request.getType();
            UUID utilityKey = user.getUtilityKey();

            MessageStatisticsQuery query = request.getQuery();
            if (query != null) {
                if (StringUtils.isBlank(query.getTimezone()))
                    query.setTimezone(user.getTimezone());
            }

            List<ReceiverAccount> receivers =
                messageRepository.getRecommendationReceivers(recommendationType, utilityKey, query);
            return new RecommendationReceiversResponse(recommendationType, receivers);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return new RestResponse(getError(ex));
        }
    }

}
