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
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.model.utility.UtilityInfo;
import eu.daiad.web.model.utility.UtilityInfoResponse;
import eu.daiad.web.repository.application.IUtilityRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

/**
 * Provides actions for querying utility data
 *
 */
@RestController
public class UtilityController extends BaseController {

	private static final Log logger = LogFactory.getLog(UtilityController.class);

	@Autowired
	private IUtilityRepository repository;

	/**
	 * Loads all utilities.
	 * 
	 * @return the utilities.
	 */
	@RequestMapping(value = "/action/utility/fetch/all", method = RequestMethod.GET, produces = "application/json")
	@Secured({ "ROLE_SUPERUSER", "ROLE_ADMIN" })
	public RestResponse getUtilityInfo() {
		RestResponse response = new RestResponse();

		try {
			return new UtilityInfoResponse(repository.getUtilities());
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);

			response.add(this.getError(ex));
		}
		return response;
	}

	@RequestMapping(value = "/action/utility/fetch/corresponding", method = RequestMethod.GET, produces = "application/json")
	@Secured({ "ROLE_SUPERUSER", "ROLE_ADMIN" })
	public RestResponse getCorrespondingUtilities(@AuthenticationPrincipal AuthenticatedUser user) {
		RestResponse response = new RestResponse();                
		try {
                    
            UtilityInfoResponse utilities = new UtilityInfoResponse(repository.getUtilities());
                        
            List<UtilityInfo> utilityResponse = new ArrayList<>();                    
            String utilityName;
            if(user.getUsername().contains("alicante")){
                utilityName = "Alicante";
            }
            else if (user.getUsername().contains("albans")){
                utilityName = "St Albans";
            }
            else{
                return utilities;
            }
            for(UtilityInfo utility : utilities.getUtilitiesInfo()){     
                if(utility.getName().equalsIgnoreCase(utilityName)){
                    utilityResponse.add(utility);
                    return new UtilityInfoResponse(utilityResponse);
                }
            }       
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			response.add(this.getError(ex));
		}
		return response;
	}        
        
}