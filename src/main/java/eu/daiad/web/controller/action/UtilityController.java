package eu.daiad.web.controller.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.daiad.web.controller.BaseController;
import eu.daiad.web.model.RestResponse;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.utility.UtilityInfoResponse;
import eu.daiad.web.repository.application.IUtilityRepository;

@RestController
public class UtilityController extends BaseController {

	private static final Log logger = LogFactory.getLog(UtilityController.class);

	@Autowired
	private IUtilityRepository repository;

	@RequestMapping(value = "/action/utility/fetch/all", method = RequestMethod.GET, produces = "application/json")
	@Secured({ "ROLE_SUPERUSER", "ROLE_ADMIN" })
	public RestResponse getUtilityInfo() {
		RestResponse response = new RestResponse();

		try {
			return new UtilityInfoResponse(repository.getUtilities());

		} catch (ApplicationException ex) {
			if (!ex.isLogged()) {
				logger.error(ex.getMessage(), ex);
			}

			response.add(this.getError(ex));
		}
		return response;
	}
}