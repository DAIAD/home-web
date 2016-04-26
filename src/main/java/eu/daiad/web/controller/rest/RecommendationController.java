package eu.daiad.web.controller.rest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.daiad.web.controller.BaseRestController;
import eu.daiad.web.model.RestResponse;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.recommendation.MessageResponse;
import eu.daiad.web.model.recommendation.StaticRecommendationResponse;
import eu.daiad.web.model.security.Credentials;
import eu.daiad.web.model.security.EnumRole;
import eu.daiad.web.repository.application.IRecommendationRepository;
import org.joda.time.DateTime;
import eu.daiad.web.repository.application.IMessageQueryRepository;

@RestController("RestRecommendationController")
public class RecommendationController extends BaseRestController {

	private static final Log logger = LogFactory.getLog(RecommendationController.class);

	@Autowired
	private IRecommendationRepository recommendationRepository;

	@Autowired
	private IMessageQueryRepository jpaMessageRepository;

	@RequestMapping(value = "/api/v1/recommendation/static/{locale}", method = RequestMethod.POST, produces = "application/json")
	public RestResponse getRecommendations(@PathVariable String locale, @RequestBody Credentials data) {
		RestResponse response = new RestResponse();

		try {
			this.authenticate(data, EnumRole.ROLE_USER);

			StaticRecommendationResponse recommendationResponse = new StaticRecommendationResponse();

			recommendationResponse.setRecommendations(this.recommendationRepository.getStaticRecommendations(locale));

			return recommendationResponse;
		} catch (ApplicationException ex) {
			if (!ex.isLogged()) {
				logger.error(ex.getMessage(), ex);
			}

			response.add(this.getError(ex));
		}

		return response;
	}

	@RequestMapping(value = "/api/v1/get/messages", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public RestResponse getMessages(@RequestBody Credentials credentials) {
		MessageResponse response = new MessageResponse();
		try {

			// response.setMessages(jpaMessageRepository.getMessages(credentials.getUsername()));
			response.setMessages(jpaMessageRepository.testGetMessages());

		} catch (ApplicationException ex) {
			if (!ex.isLogged()) {
				logger.error(ex.getMessage(), ex);
			}

			response.add(this.getError(ex));
		}

		return response;
	}

	@RequestMapping(value = "/api/v1/ack/{type}/{id}", method = RequestMethod.POST, produces = "application/json")
	public RestResponse acknowledgeMessage(@RequestBody Credentials credentials, @PathVariable String type,
					@PathVariable String id) {

		RestResponse response = new RestResponse();
		try {

			jpaMessageRepository.messageAcknowledged(credentials.getUsername(), type, Integer.parseInt(id),
							DateTime.now());

		} catch (ApplicationException ex) {
			if (!ex.isLogged()) {
				logger.error(ex.getMessage(), ex);
			}

			response.add(this.getError(ex));
		}

		return response;
	}

}
