package eu.daiad.web.controller.action;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.daiad.web.controller.BaseController;
import eu.daiad.web.model.RestResponse;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.message.EnumMessageType;
import eu.daiad.web.model.message.Message;
import eu.daiad.web.model.message.MessageResponse;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.repository.application.IMessageRepository;

@RestController
public class MessageController extends BaseController {

	private static final Log logger = LogFactory.getLog(MessageController.class);

	@Autowired
	private IMessageRepository messageRepository;

	@RequestMapping(value = "/action/message", method = RequestMethod.GET, produces = "application/json")
	@Secured("ROLE_USER")
	public RestResponse getMessages(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable String locale) {
		RestResponse response = new RestResponse();

		try {
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

	@RequestMapping(value = "/action/message/acknowledge/{type}/{id}", method = RequestMethod.GET, produces = "application/json")
	@Secured("ROLE_ADMIN")
	public RestResponse recieveMessageAcknowledge(@AuthenticationPrincipal AuthenticatedUser user,
					@PathVariable String type, @PathVariable int id) {
		RestResponse response = new RestResponse();

		try {
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
