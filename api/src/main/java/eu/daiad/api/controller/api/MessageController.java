package eu.daiad.api.controller.api;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import eu.daiad.api.controller.BaseRestController;
import eu.daiad.common.model.AuthenticatedRequest;
import eu.daiad.common.model.EnumApplication;
import eu.daiad.common.model.RestResponse;
import eu.daiad.common.model.message.EnumMessageType;
import eu.daiad.common.model.message.MessageAcknowledgementRequest;
import eu.daiad.common.model.message.MessageRequest;
import eu.daiad.common.model.message.MessageResult;
import eu.daiad.common.model.message.MultiTypeMessageResponse;
import eu.daiad.common.model.message.SingleTypeMessageResponse;
import eu.daiad.common.model.profile.Profile;
import eu.daiad.common.model.security.AuthenticatedUser;
import eu.daiad.common.model.security.EnumRole;
import eu.daiad.common.repository.application.IProfileRepository;
import eu.daiad.common.service.message.IMessageService;

/**
 * Provides actions for loading messages and saving acknowledgments.
 */
@RestController
public class MessageController extends BaseRestController {

    /**
     * Logger instance for writing events using the configured logging API.
     */
    private static final Log logger = LogFactory.getLog(MessageController.class);

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
    @PostMapping(value = "/api/v1/message")
    public RestResponse getMessages(@RequestBody MessageRequest request)
    {
        try {
            AuthenticatedUser user = authenticate(request.getCredentials(), EnumRole.ROLE_USER);
            Profile profile = profileRepository.getProfileByUserKey(user.getKey(), EnumApplication.MOBILE);
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
    @PostMapping(value = "/api/v1/message/acknowledge")
    public RestResponse acknowledgeMessage(@RequestBody MessageAcknowledgementRequest request)
    {
        RestResponse response = new RestResponse();

        try {
            AuthenticatedUser user = authenticate(request.getCredentials(), EnumRole.ROLE_USER);
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
     * @param request user credentials.
     * @param locale the locale
     * @return the static recommendations.
     */
    @PostMapping(value = "/api/v1/tip/localized/{locale}")
    public RestResponse getRecommendations(@RequestBody AuthenticatedRequest request, @PathVariable String locale)
    {
        try {
            authenticate(request.getCredentials(), EnumRole.ROLE_SYSTEM_ADMIN, EnumRole.ROLE_UTILITY_ADMIN);
            SingleTypeMessageResponse messages = new SingleTypeMessageResponse();
            messages.setType(EnumMessageType.TIP);
            messages.setMessages(service.getTips(locale));
            return messages;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return new RestResponse(getError(ex));
        }
    }

}
