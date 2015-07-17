package eu.daiad.web.controller;

import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.daiad.web.data.UserRepository;
import eu.daiad.web.model.RestResponse;
import eu.daiad.web.security.Authenticator;
import eu.daiad.web.security.model.DaiadUser;
import eu.daiad.web.security.model.EnumRole;
import eu.daiad.web.security.model.PasswordChangeRequest;
import eu.daiad.web.security.model.RoleUpdateRequest;
import eu.daiad.web.security.model.UserRegistrationRequest;
import eu.daiad.web.security.model.UserRegistrationResponse;

@RestController
public class UserRestApiController {

	private static final int ERROR_PARSING_FAILED = 1;
	private static final int ERROR_AUTH_FAILED = 2;
	private static final int ERROR_USERNAME_UNAVAILABLE = 3;
	private static final int ERROR_PERMISSION_DENIED = 4;
	private static final int ERROR_INVALID_PASSWORD = 5;
	private static final int ERROR_USER_NOT_FOUND = 6;
	
	
	private static final int ERROR_UNKNOWN = 100;

	private static final Log logger = LogFactory
			.getLog(UserRestApiController.class);

    @Autowired
    private Authenticator authenticator;
    
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

				Set<String> zones = DateTimeZone.getAvailableIDs();
				if((!StringUtils.isBlank(data.getCountry())) &&
				   ((data.getTimezone() == null) || (!zones.contains(data.getTimezone())))) {
					String country = data.getCountry().toUpperCase();
					
					Iterator<String> it = zones.iterator();
				    while (it.hasNext()) {
				    	String zone = (String) it.next();
				    	String[] parts = StringUtils.split(zone, "/");
				    	if((parts.length==1) && (parts[0].toUpperCase().equals(country))) {
		    				data.setTimezone(zone);
			    			break;
				    	}
				    	if((parts.length==2) && (parts[1].toUpperCase().equals(country))) {
		    				data.setTimezone(zone);
			    			break;
				    	}
				    }
				}

				if(data.getTimezone() == null) {
					data.setTimezone("Europe/Athens");
				}

				DaiadUser user = repository.createUser(data.getUsername(),
												  	   data.getPassword(),
												  	   data.getFirstname(),
												  	   data.getLastname(),
												  	   data.getGender(),
												  	   data.getBirthdate(),
												  	   data.getCountry(),
												  	   data.getPostalCode(),
												  	   data.getTimezone());
				
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
	
	@RequestMapping(value = "/api/v1/user/password", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public RestResponse changePassword(@RequestBody PasswordChangeRequest data,
			BindingResult results) {

		try {
			if (results.hasErrors()) {
				return new RestResponse(ERROR_PARSING_FAILED,
						"Invalid credentials.");
			} else {
				DaiadUser user = this.authenticator.authenticateAndGetUser(data.getCredentials());
				if(user == null) {
					return new RestResponse(ERROR_AUTH_FAILED,
								"Authentication has failed.");
				}
				if(!user.hasRole("ROLE_USER")) {
					return new RestResponse(ERROR_PERMISSION_DENIED,
							"Unauthhorized request.");
				}
				if(StringUtils.isBlank(data.getPassword())) {
					return new RestResponse(ERROR_INVALID_PASSWORD,
							"Invalid password.");
				}
				
				repository.setPassword(data.getCredentials().getUsername(), data.getPassword());
		        
		        return new RestResponse();
			}
		} catch (Exception ex) {
			logger.error("Unhandled exception has occurred.", ex);
		}
		return new UserRegistrationResponse(ERROR_UNKNOWN,
				"Unhandled exception has occured.");
	}
	
	@RequestMapping(value = "/api/v1/user/role/grant", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public RestResponse addRole(@RequestBody RoleUpdateRequest data,
			BindingResult results) {

		try {
			if (results.hasErrors()) {
				return new RestResponse(ERROR_PARSING_FAILED,
						"Invalid credentials.");
			} else {
				DaiadUser admin = this.authenticator.authenticateAndGetUser(data.getCredentials());
				if(admin == null) {
					return new RestResponse(ERROR_AUTH_FAILED,
								"Authentication has failed.");
				}
				if(!admin.hasRole("ROLE_ADMIN")) {
					return new RestResponse(ERROR_PERMISSION_DENIED,
							"Unauthhorized request.");
				}
				DaiadUser user = this.repository.getUserByName(data.getUsername());
				if(user == null) {
					return new RestResponse(ERROR_USER_NOT_FOUND,
								"User does not exists.");
				}
				if(data.getRole() == EnumRole.ROLE_NONE) {
					logger.warn(String.format("Role [%s] does not exists.", data.getRole().toString()));
				} else {
					repository.setRole(data.getUsername(), data.getRole(), true);					
				}
		        
		        return new RestResponse();
			}
		} catch (Exception ex) {
			logger.error("Unhandled exception has occurred.", ex);
		}
		return new UserRegistrationResponse(ERROR_UNKNOWN,
				"Unhandled exception has occured.");
	}
	
	@RequestMapping(value = "/api/v1/user/role/revoke", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public RestResponse revokeRole(@RequestBody RoleUpdateRequest data,
			BindingResult results) {

		try {
			if (results.hasErrors()) {
				return new RestResponse(ERROR_PARSING_FAILED,
						"Invalid credentials.");
			} else {
				DaiadUser admin = this.authenticator.authenticateAndGetUser(data.getCredentials());
				if(admin == null) {
					return new RestResponse(ERROR_AUTH_FAILED,
								"Authentication has failed.");
				}
				if(!admin.hasRole("ROLE_ADMIN")) {
					return new RestResponse(ERROR_PERMISSION_DENIED,
							"Unauthhorized request.");
				}
				DaiadUser user = this.repository.getUserByName(data.getUsername());
				if(user == null) {
					return new RestResponse(ERROR_USER_NOT_FOUND,
								"User does not exists.");
				}
				if(data.getRole() == EnumRole.ROLE_NONE) {
					logger.warn(String.format("Role [%s] does not exists.", data.getRole().toString()));
				} else {
					repository.setRole(data.getUsername(), data.getRole(), false);					
				}
		        
		        return new RestResponse();
			}
		} catch (Exception ex) {
			logger.error("Unhandled exception has occurred.", ex);
		}
		return new UserRegistrationResponse(ERROR_UNKNOWN,
				"Unhandled exception has occured.");
	}
}
