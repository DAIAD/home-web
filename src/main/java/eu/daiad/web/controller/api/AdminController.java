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
import eu.daiad.web.model.group.GroupQueryRequest;
import eu.daiad.web.model.group.GroupQueryResponse;
import eu.daiad.web.model.security.Credentials;
import eu.daiad.web.model.security.EnumRole;
import eu.daiad.web.repository.application.IGroupRepository;
import eu.daiad.web.service.scheduling.ISchedulerService;

/**
 * Provides actions for performing administration tasks e.g. starting a job or querying 
 * generic application data such as user groups, areas etc.
 *
 */
@RestController("ApiAdminController")
public class AdminController extends BaseRestController {

	private static final Log logger = LogFactory.getLog(AdminController.class);

	@Autowired
	private IGroupRepository groupRepository;

	@Autowired
	private ISchedulerService schedulerService;

	/**
	 * Launches a job by its name.
	 * 
	 * @param credentials the user credentials.
	 * @param jobName the name of the job
	 * @return the controller's response.
	 */
	@RequestMapping(value = "/api/v1/admin/scheduler/launch/{jobName}", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public RestResponse launch(@RequestBody Credentials credentials, @PathVariable String jobName) {
		RestResponse response = new RestResponse();

		try {
			this.authenticate(credentials, EnumRole.ROLE_ADMIN);

			this.schedulerService.launch(jobName);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);

			response.add(this.getError(ex));
		}

		return response;
	}

	/**
	 * Returns all available groups including clusters, segments and user defined user groups. Optionally
	 * filters data.
	 * 
	 * @param request the query to filter data. 
	 * @return the selected groups.
	 */
	@RequestMapping(value = "/api/v1/admin/group/query", method = RequestMethod.POST, produces = "application/json")
	public RestResponse getGroups(@RequestBody GroupQueryRequest request) {
		RestResponse response = null;

		try {
			this.authenticate(request.getCredentials(), EnumRole.ROLE_ADMIN);

			return new GroupQueryResponse(this.groupRepository.getAll());
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);

			response = new RestResponse();
			response.add(this.getError(ex));
		}

		return response;
	}

}
