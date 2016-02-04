package eu.daiad.web.controller.rest;

import javax.validation.Valid;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.daiad.web.controller.BaseController;
import eu.daiad.web.data.ICommunityRepository;
import eu.daiad.web.model.RestResponse;
import eu.daiad.web.model.commons.CreateCommonsRequest;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.error.SharedErrorCode;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.model.security.EnumRole;
import eu.daiad.web.security.AuthenticationService;

@RestController("RestCommunityController")
public class CommunityController extends BaseController {

	private static final Log logger = LogFactory.getLog(CommunityController.class);

	@Autowired
	private AuthenticationService authenticationService;

	@Autowired
	private ICommunityRepository repository;

	@RequestMapping(value = "/api/v1/commons/create", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public RestResponse createCommunity(@RequestBody @Valid CreateCommonsRequest data, BindingResult results) {
		RestResponse response = new RestResponse();
		
		try {
			if (results.hasErrors()) {
				for (FieldError e : results.getFieldErrors()) {
					response.add(this.getError(e));
				}

				return response;
			}
			
			AuthenticatedUser user = this.authenticationService.authenticateAndGetUser(data.getCredentials());
			if (user == null) {
				throw new ApplicationException(SharedErrorCode.AUTHENTICATION);
			}
			if (!user.hasRole(EnumRole.ROLE_ADMIN)) {
				throw new ApplicationException(SharedErrorCode.AUTHORIZATION);
			}
			
			repository.create(data.getCommunity());	
		} catch (ApplicationException ex) {
			logger.error(ex);

			response.add(this.getError(ex));
		}

		return response;
	}
}
