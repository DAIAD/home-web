package eu.daiad.web.controller.api;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.daiad.web.controller.BaseRestController;
import eu.daiad.web.model.RestResponse;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.message.EnumMessageType;
import eu.daiad.web.model.message.Message;
import eu.daiad.web.model.message.MessageResponse;
import eu.daiad.web.model.security.Credentials;
import eu.daiad.web.model.security.EnumRole;
import eu.daiad.web.repository.application.IMessageRepository;

@RestController("RestRecommendationController")
public class RecommendationController extends BaseRestController {

	private static final Log logger = LogFactory.getLog(RecommendationController.class);

	@Autowired
	private IMessageRepository messageRepository;

	@RequestMapping(value = "/api/v1/message", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public RestResponse getMessages(@RequestBody Credentials credentials) {
		RestResponse response = new RestResponse();

		try {
			this.authenticate(credentials, EnumRole.ROLE_USER);

			List<Message> messages = this.messageRepository.getMessages();

			return new MessageResponse(messages);
		} catch (ApplicationException ex) {
			if (!ex.isLogged()) {
				logger.error(ex.getMessage(), ex);
			}

			response.add(this.getError(ex));
		}

		return response;
	}

	@RequestMapping(value = "/api/v1/message/acknowledge/{type}/{id}", method = RequestMethod.POST, produces = "application/json")
	public RestResponse acknowledgeMessage(@RequestBody Credentials credentials, @PathVariable String type,
					@PathVariable int id) {
		RestResponse response = new RestResponse();

		try {
			this.authenticate(credentials, EnumRole.ROLE_USER);

			this.messageRepository.setMessageAcknowledgement(EnumMessageType.fromString(type), id, DateTime.now());
		} catch (ApplicationException ex) {
			if (!ex.isLogged()) {
				logger.error(ex.getMessage(), ex);
			}

			response.add(this.getError(ex));
		}

		return response;
	}

}
