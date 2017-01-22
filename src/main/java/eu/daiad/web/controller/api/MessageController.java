package eu.daiad.web.controller.api;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.daiad.web.controller.BaseRestController;
import eu.daiad.web.model.EnumApplication;
import eu.daiad.web.model.RestResponse;
import eu.daiad.web.model.message.MessageAcknowledgementRequest;
import eu.daiad.web.model.message.MessageRequest;
import eu.daiad.web.model.message.MessageResult;
import eu.daiad.web.model.message.MultiTypeMessageResponse;
import eu.daiad.web.model.profile.Profile;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.model.security.EnumRole;
import eu.daiad.web.repository.application.IMessageRepository;
import eu.daiad.web.repository.application.IProfileRepository;

/**
 * Provides actions for loading messages and saving acknowledgments.
 */
@RestController("RestRecommendationController")
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
    @RequestMapping(value = "/api/v1/message", method = RequestMethod.POST, produces = "application/json")
    public RestResponse getMessages(@RequestBody MessageRequest request)
    {
        try {
            AuthenticatedUser user = authenticate(request.getCredentials(), EnumRole.ROLE_USER);
            Profile profile = profileRepository.getProfileByUsername(EnumApplication.MOBILE);
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
    @RequestMapping(value = "/api/v1/message/acknowledge", method = RequestMethod.POST, produces = "application/json")
    public RestResponse acknowledgeMessage(@RequestBody MessageAcknowledgementRequest request)
    {
        RestResponse response = new RestResponse();

        try {
            AuthenticatedUser user = authenticate(request.getCredentials(), EnumRole.ROLE_USER);
            messageRepository.setMessageAcknowledgement(user, request.getMessages());
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            response.add(this.getError(ex));
        }

        return response;
    }
}
