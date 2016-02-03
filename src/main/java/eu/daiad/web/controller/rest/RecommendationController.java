package eu.daiad.web.controller.rest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.daiad.web.controller.BaseController;
import eu.daiad.web.data.IRecommendationRepository;
import eu.daiad.web.model.Credentials;
import eu.daiad.web.model.RestResponse;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.error.SharedErrorCode;
import eu.daiad.web.model.recommendation.StaticRecommendationResponse;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.model.security.EnumRole;
import eu.daiad.web.security.AuthenticationService;

@RestController("RestRecommendationController")
public class RecommendationController extends BaseController {

	private static final Log logger = LogFactory.getLog(RecommendationController.class);

	@Autowired
	private AuthenticationService authenticationService;

	@Autowired
	private IRecommendationRepository recommendationRepository;

	@RequestMapping(value = "/api/v1/recommendation/static/{locale}", method = RequestMethod.POST, produces = "application/json")
	public RestResponse getRecommendations(@PathVariable String locale, @RequestBody Credentials data) {
		RestResponse response = new RestResponse();

		try {
			AuthenticatedUser user = this.authenticationService.authenticateAndGetUser(data);
			if (user == null) {
				throw new ApplicationException(SharedErrorCode.AUTHENTICATION);
			} else if (!user.hasRole(EnumRole.ROLE_USER)) {
				throw new ApplicationException(SharedErrorCode.AUTHORIZATION);
			}

			StaticRecommendationResponse recommendationResponse = new StaticRecommendationResponse();

			recommendationResponse.setRecommendations(this.recommendationRepository.getStaticRecommendations(locale));

			return recommendationResponse;
		} catch (ApplicationException ex) {
			logger.error(ex);

			response.add(this.getError(ex));
		}

		return response;
	}

}
