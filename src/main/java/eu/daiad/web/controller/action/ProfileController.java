package eu.daiad.web.controller.action;

import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.daiad.web.controller.BaseController;
import eu.daiad.web.model.EnumApplication;
import eu.daiad.web.model.RestResponse;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.profile.ProfileDeactivateRequest;
import eu.daiad.web.model.profile.ProfileModesFilterOptionsResponse;
import eu.daiad.web.model.profile.ProfileModesRequest;
import eu.daiad.web.model.profile.ProfileModesResponse;
import eu.daiad.web.model.profile.ProfileModesSubmitChangesRequest;
import eu.daiad.web.model.profile.ProfileResponse;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.repository.application.IProfileRepository;

@RestController
public class ProfileController extends BaseController {

	private static final Log logger = LogFactory.getLog(ProfileController.class);

	@Autowired
	private IProfileRepository profileRepository;
	
	@RequestMapping(value = "/action/profile/test", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public RestResponse register2(@AuthenticationPrincipal AuthenticatedUser user, @RequestBody ProfileModesRequest data) {
		RestResponse response = new RestResponse();
		
		ObjectMapper mapper = new ObjectMapper();
		try {
			System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(data));
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(user.hasRole("ROLE_ADMIN")) {
			System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~ROLE_ADMIN~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		} else {
			System.out.println("~~~~~~~~~~~~~~~~~~~~~~~ROLE_NOT_ADMIN~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		}
		

		return response;
	}

	@RequestMapping(value = "/action/profile/load", method = RequestMethod.GET, produces = "application/json")
	@Secured({"ROLE_USER", "ROLE_SUPERUSER", "ROLE_ADMIN"})
	public RestResponse getProfile(@AuthenticationPrincipal AuthenticatedUser user) throws JsonProcessingException {
		RestResponse response = new RestResponse();
		
		try {
			if(user.hasRole("ROLE_ADMIN")) {
				return new ProfileResponse(profileRepository.getProfileByUsername(EnumApplication.UTILITY));
			}
			
			return  new ProfileResponse(profileRepository.getProfileByUsername(EnumApplication.HOME));
		} catch (ApplicationException ex) {
			logger.error(ex.getMessage(), ex);

			response.add(this.getError(ex));
		}

		return response;
	}
	
	@RequestMapping(value = "/action/profile/modes/list", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@Secured({"ROLE_SUPERUSER", "ROLE_ADMIN"})
	public RestResponse getProfileModes(@AuthenticationPrincipal AuthenticatedUser user, @RequestBody ProfileModesRequest filters) 
			throws JsonProcessingException {
		RestResponse response = new RestResponse();
		
		try {
				return new ProfileModesResponse(profileRepository.getProfileModes(filters));
		} catch (ApplicationException ex) {
			logger.error(ex.getMessage(), ex);
			response.add(this.getError(ex));
		}

		return response;
	}
	
	@RequestMapping(value = "/action/profile/modes/filter/options", method = RequestMethod.GET, produces = "application/json")
	@Secured({"ROLE_SUPERUSER", "ROLE_ADMIN"})
	public RestResponse getFilterOptions(@AuthenticationPrincipal AuthenticatedUser user) 
			throws JsonProcessingException {
		RestResponse response = new RestResponse();
		
		try {
				return new ProfileModesFilterOptionsResponse(profileRepository.getFilterOptions());
		} catch (ApplicationException ex) {
			logger.error(ex.getMessage(), ex);
			response.add(this.getError(ex));
		}

		return response;
	}
	
	@RequestMapping(value = "/action/profile/modes/save", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@Secured({"ROLE_SUPERUSER", "ROLE_ADMIN"})
	public RestResponse saveModeChanges(@AuthenticationPrincipal AuthenticatedUser user, @RequestBody ProfileModesSubmitChangesRequest modeChanges) 
			throws JsonProcessingException {
		RestResponse response = new RestResponse();
		
		try {
			profileRepository.setProfileModes(modeChanges);
			
		} catch (ApplicationException ex) {
			logger.error(ex.getMessage(), ex);
			response.add(this.getError(ex));
		}

		return response;
	}
	
	@RequestMapping(value = "/action/profile/deactivate", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@Secured({"ROLE_SUPERUSER", "ROLE_ADMIN"})
	public RestResponse deactivateProfile(@AuthenticationPrincipal AuthenticatedUser user, @RequestBody ProfileDeactivateRequest userDeactId) 
			throws JsonProcessingException {
		RestResponse response = new RestResponse();
		
		try {
			profileRepository.deactivateProfile(userDeactId);
			
		} catch (ApplicationException ex) {
			logger.error(ex.getMessage(), ex);
			response.add(this.getError(ex));
		}

		return response;
	}

}
