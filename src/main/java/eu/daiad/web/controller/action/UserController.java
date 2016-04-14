package eu.daiad.web.controller.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.daiad.web.controller.BaseController;
import eu.daiad.web.model.EnumApplication;
import eu.daiad.web.model.RestResponse;
import eu.daiad.web.model.admin.AccountWhiteListInfo;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.profile.ProfileResponse;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.model.user.UserRegistrationRequest;
import eu.daiad.web.repository.application.IUserRepository;


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
	
	@RequestMapping(value = "/action/user/create", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@Secured({"ROLE_SUPERUSER", "ROLE_ADMIN"})
	public @ResponseBody RestResponse deactivateProfileTest(@AuthenticationPrincipal AuthenticatedUser user, @RequestBody AccountWhiteListInfo userInfo) 
			throws JsonProcessingException {
		RestResponse response = new RestResponse();
		
		try {
			ObjectMapper mapper = new ObjectMapper();
			System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(userInfo));
			repository.insertAccountWhiteListEntry(userInfo);
			
		} catch (ApplicationException ex) {
			logger.error(ex.getMessage(), ex);
			response.add(this.getError(ex));
		}

		return response;
	}
}
