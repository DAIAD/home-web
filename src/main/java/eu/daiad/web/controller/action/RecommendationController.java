package eu.daiad.web.controller.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.daiad.web.controller.BaseController;
import eu.daiad.web.data.IRecommendationRepository;
import eu.daiad.web.model.RestResponse;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.recommendation.StaticRecommendationResponse;
import eu.daiad.web.model.security.AuthenticatedUser;

@RestController
public class RecommendationController extends BaseController {

	private static final Log logger = LogFactory.getLog(RecommendationController.class);

	@Autowired
	private IRecommendationRepository recommendationRepository;

	@RequestMapping(value = "/action/recommendation/static/{locale}", method = RequestMethod.GET, produces = "application/json")
	@Secured("ROLE_USER")
	public RestResponse getRecommendations(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable String locale) {
		StaticRecommendationResponse response = new StaticRecommendationResponse();

		try {
			response.setRecommendations(this.recommendationRepository.getStaticRecommendations(locale));
		} catch (ApplicationException ex) {
			logger.error(ex);

			response.add(this.getError(ex));
		}

		return response;
	}

}
