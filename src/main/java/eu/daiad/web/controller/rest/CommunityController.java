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

import eu.daiad.web.controller.BaseRestController;
import eu.daiad.web.model.RestResponse;
import eu.daiad.web.model.commons.CreateCommonsRequest;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.security.EnumRole;
import eu.daiad.web.repository.application.ICommunityRepository;

@RestController("RestCommunityController")
public class CommunityController extends BaseRestController {

	private static final Log logger = LogFactory.getLog(CommunityController.class);

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

			this.authenticate(data.getCredentials(), EnumRole.ROLE_ADMIN);

			repository.create(data.getCommunity());
		} catch (ApplicationException ex) {
			logger.error(ex.getMessage(), ex);

			response.add(this.getError(ex));
		}

		return response;
	}
}
