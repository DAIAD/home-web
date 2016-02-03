package eu.daiad.web.controller.rest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.daiad.web.data.IRecommendationRepository;
import eu.daiad.web.model.Credentials;
import eu.daiad.web.model.Error;
import eu.daiad.web.model.RestResponse;
import eu.daiad.web.model.recommendation.StaticRecommendationResponse;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.model.security.EnumRole;
import eu.daiad.web.security.AuthenticationService;

@RestController("RestRecommendationController")
public class RecommendationController {

	private static final Log logger = LogFactory.getLog(RecommendationController.class);

	@Autowired
	private AuthenticationService authenticationService;
	
	@Autowired
	private IRecommendationRepository recommendationRepository;

	@RequestMapping(value = "/api/v1/recommendation/static/{locale}", method = RequestMethod.POST, produces = "application/json")
	public RestResponse getRecommendations(@PathVariable String locale, @RequestBody Credentials data) {
		try {
			AuthenticatedUser user = this.authenticationService.authenticateAndGetUser(data);
			if (user == null) {
				return new RestResponse(Error.ERROR_AUTHENTICATION, "Authentication has failed.");
			} else if (!user.hasRole(EnumRole.ROLE_USER)) {
				return new RestResponse(Error.ERROR_AUTHORIZATION, "Authorization has failed.");
			}

			StaticRecommendationResponse response = new StaticRecommendationResponse();

			response.setRecommendations(this.recommendationRepository.getStaticRecommendations(locale));

			return response;
		} catch (Exception ex) {
			logger.error("Failed to load profile.", ex);
		}
		return new RestResponse(Error.ERROR_UNKNOWN, "An unhandled exception has occurred.");
	}

}
