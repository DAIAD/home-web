package eu.daiad.home.controller.action;

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

import eu.daiad.common.model.EnumApplication;
import eu.daiad.common.model.RestResponse;
import eu.daiad.common.model.message.AlertReceiversResponse;
import eu.daiad.common.model.message.AlertStatisticsRequest;
import eu.daiad.common.model.message.Announcement;
import eu.daiad.common.model.message.AnnouncementDetailsResponse;
import eu.daiad.common.model.message.AnnouncementRequest;
import eu.daiad.common.model.message.EnumAlertType;
import eu.daiad.common.model.message.EnumMessageType;
import eu.daiad.common.model.message.EnumRecommendationType;
import eu.daiad.common.model.message.MessageAcknowledgementRequest;
import eu.daiad.common.model.message.MessageRequest;
import eu.daiad.common.model.message.MessageResult;
import eu.daiad.common.model.message.MessageStatisticsQuery;
import eu.daiad.common.model.message.MessageStatisticsRequest;
import eu.daiad.common.model.message.MessageStatisticsResponse;
import eu.daiad.common.model.message.MultiTypeMessageResponse;
import eu.daiad.common.model.message.ReceiverAccount;
import eu.daiad.common.model.message.RecommendationReceiversResponse;
import eu.daiad.common.model.message.RecommendationStatisticsRequest;
import eu.daiad.common.model.message.SingleTypeMessageResponse;
import eu.daiad.common.model.message.Tip;
import eu.daiad.common.model.profile.Profile;
import eu.daiad.common.model.security.AuthenticatedUser;
import eu.daiad.common.model.security.RoleConstant;
import eu.daiad.common.repository.application.IProfileRepository;
import eu.daiad.common.service.message.IMessageService;
import eu.daiad.home.controller.BaseController;

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
     * Service for accessing messages.
     */
    @Autowired
    private IMessageService service;

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
            Profile profile = profileRepository.getProfileByUserKey(user.getKey(), EnumApplication.HOME);
            if(!profile.isSendMessageEnabled()) {
                return new MultiTypeMessageResponse();
            } else {
                MessageResult result = service.getMessages(user, request);
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
            service.acknowledgeMessages(user, request.getMessages());
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            response.add(this.getError(ex));
        }
        return response;
    }

    /**
     * Gets localized tips
     *
     * @param user the user
     * @param locale the locale
     * @return the static recommendations.
     */
    @RequestMapping(value = "/action/tip/localized/{locale}", method = RequestMethod.GET, produces = "application/json")
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public RestResponse getRecommendations(
        @AuthenticationPrincipal AuthenticatedUser user, @PathVariable String locale)
    {
        try {
            SingleTypeMessageResponse messages = new SingleTypeMessageResponse();
            messages.setType(EnumMessageType.TIP);
            messages.setMessages(service.getTips(locale));
            return messages;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return new RestResponse(getError(ex));
        }
    }

    /**
     * Activate/Deactivate tips
     *
     * @param request the messages to change activity status
     * @return the controller response.
     */
    @RequestMapping(value = "/action/tip/status/save", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public RestResponse setTipsActivityStatus(@RequestBody List<Tip> request)
    {
        RestResponse response = new RestResponse();
        try {
            for (Tip tip: request){
                service.setTipActiveStatus(tip.getId(), tip.isActive());
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
    @RequestMapping(value = "/action/tip/save", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public RestResponse saveTip(@AuthenticationPrincipal AuthenticatedUser user, @RequestBody Tip request)
    {
        RestResponse response = new RestResponse();
        try {
            if (request.getLocale() == null)
                request.setLocale(user.getLocale());
            service.saveTip(request);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            response.add(this.getError(ex));
        }
        return response;
    }

    /**
     * Delete an existing recommendation (tip).
     *
     * @param user the authenticated user.
     * @param id the tip id.
     * @return an instance of {@link RestResponse}.
     */
    @RequestMapping(value = "/action/tip/delete/{id}", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public RestResponse deleteTip(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable String id)
    {
        RestResponse response = new RestResponse();
        try {
            int tipId = Integer.parseInt(id);
            service.deleteTip(tipId);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            response.add(this.getError(ex));
        }
        return response;
    }

    /**
     * Send announcement to the provided accounts.
     *
     * @param user the authenticated user.
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
            Announcement a = request.getAnnouncement();
            if (a.getLocale() == null)
                a.setLocale(user.getLocale());
            service.broadcastAnnouncement(request, DEFAULT_CHANNEL);
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
    public RestResponse getAnnouncements(@AuthenticationPrincipal AuthenticatedUser user)
    {
        try {
            SingleTypeMessageResponse messages = new SingleTypeMessageResponse();
            messages.setType(EnumMessageType.ANNOUNCEMENT);
            messages.setMessages(service.getAnnouncements(user.getLocale()));
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
     * @param id the announcement id
     * @return the controller response.
     */
    @RequestMapping(value = "/action/announcement/delete/{id}", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public RestResponse deleteAnnouncement(
        @AuthenticationPrincipal AuthenticatedUser user, @PathVariable String id)
    {
        RestResponse response = new RestResponse();
        try {
            int announcementId = Integer.parseInt(id);
            service.deleteAnnouncement(announcementId);
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
    public RestResponse getAnnouncementDetails(
        @AuthenticationPrincipal AuthenticatedUser user, @PathVariable String id)
    {
        try {
            AnnouncementDetailsResponse response = new AnnouncementDetailsResponse();
            int announcementId = Integer.parseInt(id);
            response.setAnnouncement(
                service.getAnnouncement(announcementId, user.getLocale()));
            response.setReceivers(
                service.getAnnouncementReceivers(announcementId));
            return response;
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
    @RequestMapping(value = "/action/recommendation/statistics", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
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
                service.getAlertStatistics(utilityKey, query));

            response.setRecommendationStats(
                service.getRecommendationStatistics(utilityKey, query));

            return response;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return new RestResponse(getError(ex));
        }
    }

    /**
     * Get receivers for alerts of a given type
     */
    @RequestMapping(value = "/action/alert/receivers", method = RequestMethod.POST, produces = "application/json")
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
                service.getAlertReceivers(alertType, utilityKey, query);
            return new AlertReceiversResponse(alertType, receivers);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return new RestResponse(getError(ex));
        }
    }

    /**
     * Get receivers for recommendations of a given type.
     */
    @RequestMapping(value = "/action/recommendation/receivers", method = RequestMethod.POST, produces = "application/json")
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
                service.getRecommendationReceivers(recommendationType, utilityKey, query);
            return new RecommendationReceiversResponse(recommendationType, receivers);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return new RestResponse(getError(ex));
        }
    }

}
