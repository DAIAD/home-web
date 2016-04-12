package eu.daiad.web.controller.action;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import eu.daiad.web.controller.BaseController;
import eu.daiad.web.model.RestResponse;
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
	
}
