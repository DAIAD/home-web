package eu.daiad.web.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.daiad.web.data.UserRepository;
import eu.daiad.web.security.model.DaiadUser;
import eu.daiad.web.security.model.UserRegistrationRequest;
import eu.daiad.web.security.model.UserRegistrationResponse;

@RestController
public class UserController {

	private static final int ERROR_PARSING_FAILED = 1;
	private static final int ERROR_USERNAME_UNAVAILABLE = 2;
	private static final int ERROR_UNKNOWN = 100;

	private static final Log logger = LogFactory
			.getLog(UserController.class);

	/*
	@InitBinder
	protected void initBinder(WebDataBinder binder) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd hh:mm:ss");
		dateFormat.setLenient(false);

		binder.registerCustomEditor(DateTime.class, new CustomDateEditor(
				dateFormat, false));
	}
	*/
	
    @Autowired
    private UserRepository repository;
    
	@RequestMapping(value = "/api/v1/user/register", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public UserRegistrationResponse register(@RequestBody UserRegistrationRequest data,
			BindingResult results) {

		try {
			if (results.hasErrors()) {
				return new UserRegistrationResponse(ERROR_PARSING_FAILED,
						"Invalid credentials.");
			} else {
				if(repository.getUserByName(data.getUsername()) != null) {
					return new UserRegistrationResponse(ERROR_USERNAME_UNAVAILABLE,
							"Username is unavailable.");
				}
				DaiadUser user = repository.createUser(data.getUsername(),
												  	   data.getPassword(),
												  	   data.getFirstname(),
												  	   data.getLastname(),
												  	   data.getGender(),
												  	   data.getBirthdate(),
												  	   data.getCountry(),
												  	   data.getPostalCode());
				
		        UserRegistrationResponse response =  new UserRegistrationResponse();
		        response.setApplicationKey(user.getKey().toString());
		        
		        return response;
			}
		} catch (Exception ex) {
			logger.error("Unhandled exception has occurred.", ex);
		}
		return new UserRegistrationResponse(ERROR_UNKNOWN,
				"Unhandled exception has occured.");
	}
}
