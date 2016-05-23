package eu.daiad.web.controller.api;

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
import eu.daiad.web.model.security.Credentials;
import eu.daiad.web.model.security.EnumRole;
import eu.daiad.web.service.scheduling.ISchedulerService;

@RestController("ApiAdminController")
public class AdminController extends BaseRestController {

	private static final Log logger = LogFactory.getLog(AdminController.class);

	@Autowired
	private ISchedulerService schedulerService;

	@RequestMapping(value = "/api/v1/admin/scheduler/launch/{jobName}", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public RestResponse launch(@RequestBody Credentials credentials, @PathVariable String jobName) {
		RestResponse response = new RestResponse();

		try {
			this.authenticate(credentials, EnumRole.ROLE_ADMIN);

			this.schedulerService.launch(jobName);
		} catch (ApplicationException ex) {
			if (!ex.isLogged()) {
				logger.error(ex.getMessage(), ex);
			}

			response.add(this.getError(ex));
		}

		return response;
	}

}
