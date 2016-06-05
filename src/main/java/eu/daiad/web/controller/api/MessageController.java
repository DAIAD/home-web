package eu.daiad.web.controller.api;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.daiad.web.controller.BaseRestController;
import eu.daiad.web.model.RestResponse;
import eu.daiad.web.model.message.Message;
import eu.daiad.web.model.message.MessageAcknowledgementRequest;
import eu.daiad.web.model.message.MessageRequest;
import eu.daiad.web.model.message.MessageResult;
import eu.daiad.web.model.message.MultiTypeMessageResponse;
import eu.daiad.web.model.security.EnumRole;
import eu.daiad.web.repository.application.IMessageRepository;

/**
 * 
 * Provides actions for loading messages and saving acknowledgments.
 *
 */
@RestController("RestRecommendationController")
public class MessageController extends BaseRestController {

	private static final Log logger = LogFactory.getLog(MessageController.class);

	@Autowired
	private IMessageRepository messageRepository;

	/**
	 * Loads messages i.e. alerts, recommendations and tips. Optionally filters messages.
	 * 
	 * @param request the request.
	 * @return the messages.
	 */
	@RequestMapping(value = "/api/v1/message", method = RequestMethod.POST, produces = "application/json")
	public RestResponse getMessages(@RequestBody MessageRequest request) {
		try {
			this.authenticate(request.getCredentials(), EnumRole.ROLE_USER);

			MultiTypeMessageResponse messageResponse = new MultiTypeMessageResponse();

			MessageResult result = this.messageRepository.getMessages(request);

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
					case RECOMMENDATION_DYNAMIC:
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

			RestResponse response = new RestResponse();
			response.add(this.getError(ex));
			return response;
		}

	}

	/**
	 * Saves one or more message acknowledgments.
	 * 
	 * @param request the messages to acknowledge.
	 * @return the controller response.
	 */
	@RequestMapping(value = "/api/v1/message/acknowledge", method = RequestMethod.POST, produces = "application/json")
	public RestResponse acknowledgeMessage(@RequestBody MessageAcknowledgementRequest request) {
		RestResponse response = new RestResponse();

		try {
			this.authenticate(request.getCredentials(), EnumRole.ROLE_USER);

			this.messageRepository.setMessageAcknowledgement(request.getMessages());
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);

			response.add(this.getError(ex));
		}

		return response;
	}
}
