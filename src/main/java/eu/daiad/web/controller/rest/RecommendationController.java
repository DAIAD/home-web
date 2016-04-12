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
import eu.daiad.web.model.recommendation.StaticRecommendationResponse;
import eu.daiad.web.model.security.Credentials;
import eu.daiad.web.model.security.EnumRole;
import eu.daiad.web.repository.application.IRecommendationRepository;

@RestController("RestRecommendationController")
public class RecommendationController extends BaseRestController {

	private static final Log logger = LogFactory.getLog(RecommendationController.class);

	@Autowired
	private IRecommendationRepository recommendationRepository;

	@RequestMapping(value = "/api/v1/recommendation/static/{locale}", method = RequestMethod.POST, produces = "application/json")
	public RestResponse getRecommendations(@PathVariable String locale, @RequestBody Credentials data) {
		RestResponse response = new RestResponse();

		try {
			this.authenticate(data, EnumRole.ROLE_USER);

			StaticRecommendationResponse recommendationResponse = new StaticRecommendationResponse();

			recommendationResponse.setRecommendations(this.recommendationRepository.getStaticRecommendations(locale));

			return recommendationResponse;
		} catch (ApplicationException ex) {
			logger.error(ex.getMessage(), ex);

			response.add(this.getError(ex));
		}

		return response;
	}

}
