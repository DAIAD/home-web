package eu.daiad.web.controller.action;

import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import eu.daiad.web.controller.BaseController;
import eu.daiad.web.model.RestResponse;
import eu.daiad.web.model.admin.AccountWhiteListInfo;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.model.user.UserInfoResponse;
import eu.daiad.web.repository.application.IUserRepository;

/**
 * Provides actions for managing users.
 *
 */
@Controller
public class UserController extends BaseController {

	private static final Log logger = LogFactory.getLog(UserController.class);

	@Autowired
	private IUserRepository repository;

	@Autowired
	private org.springframework.validation.Validator validator;

	@Value("${security.white-list}")
	private boolean enforceWhiteListCheck;

	@RequestMapping(value = "/action/user/fetch", method = RequestMethod.POST, produces = "application/json")
	@ResponseBody
	public RestResponse fetchUser() {
		RestResponse response = new RestResponse();
		return response;
	}

	/**
	 * Adds a user to the white list.
	 * 
	 * @param user the currently authenticated user.
	 * @param userInfo the user to add.
	 * @return the controller's response.
	 */
	@RequestMapping(value = "/action/user/create", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@Secured({ "ROLE_SUPERUSER", "ROLE_ADMIN" })
	public @ResponseBody RestResponse addUserToWhiteList(@AuthenticationPrincipal AuthenticatedUser user,
					@RequestBody AccountWhiteListInfo userInfo) {
		RestResponse response = new RestResponse();

		try {
			repository.insertAccountWhiteListEntry(userInfo);

		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);

			response.add(this.getError(ex));
		}

		return response;
	}
	
	/**
	 * Returns basic info for a user given his key.
	 * Notice that it does not provide info, regarding the groups it participates or the devices he owns.
	 *  
	 * @param user_id
	 * @return the user
	 */
	@RequestMapping(value = "/action/user/{user_id}", method = RequestMethod.GET, produces = "application/json")
	@Secured({"ROLE_SUPERUSER", "ROLE_ADMIN" })
	public @ResponseBody RestResponse getUserInfoByKey(@PathVariable UUID user_id) {
		RestResponse response = new RestResponse();
		try{
			
			return new UserInfoResponse(repository.getUserInfoByKey(user_id));
			
		} catch (ApplicationException ex) {
			logger.error(ex.getMessage(), ex);

			response.add(this.getError(ex));
		}
		return response;
	}
	
}
