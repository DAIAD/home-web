package eu.daiad.web.controller.action;

import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.daiad.web.controller.BaseController;
import eu.daiad.web.model.RestResponse;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.favourite.UpsertFavouriteRequest;
import eu.daiad.web.model.favourite.FavouriteAccountInfoResponse;
import eu.daiad.web.model.favourite.FavouriteGroupInfoResponse;
import eu.daiad.web.model.favourite.FavouritesListInfoResponse;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.repository.application.IFavouriteRepository;

/**
 * Provides actions for managing user favorites.
 */
@RestController
public class FavouriteController extends BaseController {
	
	private static final Log logger = LogFactory.getLog(FavouriteController.class);
	
	@Autowired
	private IFavouriteRepository repository;
	
	@RequestMapping(value = "/action/favourite/list", method = RequestMethod.GET, produces = "application/json")
	@Secured({"ROLE_SUPERUSER", "ROLE_ADMIN" })
	public RestResponse getFavouriteInfo() {
		RestResponse response = new RestResponse();
		
		try{						
			return new FavouritesListInfoResponse(repository.getFavourites());
			
		} catch (ApplicationException ex) {
			logger.error(ex.getMessage(), ex);

			response.add(this.getError(ex));
		}
		return response;
	}
	
	@RequestMapping(value = "/action/favourite/check/account/{account_id}", method = RequestMethod.GET, produces = "application/json")
	@Secured({"ROLE_SUPERUSER", "ROLE_ADMIN" })
	public RestResponse checkFavouriteAccountStatus(@PathVariable UUID account_id) {
		RestResponse response = new RestResponse();
		
		try{			
			return new FavouriteAccountInfoResponse(repository.checkFavouriteAccount(account_id));
			
		} catch (ApplicationException ex) {
			logger.error(ex.getMessage(), ex);

			response.add(this.getError(ex));
		}
		return response;
	}
	
	@RequestMapping(value = "/action/favourite/check/group/{group_id}", method = RequestMethod.GET, produces = "application/json")
	@Secured({"ROLE_SUPERUSER", "ROLE_ADMIN" })
	public RestResponse checkFavouriteGroupStatus(@PathVariable UUID group_id) {
		RestResponse response = new RestResponse();
		
		try{			
			return new FavouriteGroupInfoResponse(repository.checkFavouriteGroup(group_id));
			
		} catch (ApplicationException ex) {
			logger.error(ex.getMessage(), ex);

			response.add(this.getError(ex));
		}
		return response;
	}
	
	@RequestMapping(value = "/action/favourite/upsert", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@Secured({"ROLE_SUPERUSER", "ROLE_ADMIN"})
	public @ResponseBody RestResponse upsertFavourite(@AuthenticationPrincipal AuthenticatedUser user, @RequestBody UpsertFavouriteRequest favouriteInfo){
		RestResponse response = new RestResponse();
		
		try {
			repository.upsertFavourite(favouriteInfo);
			
		} catch (ApplicationException ex) {
			logger.error(ex.getMessage(), ex);
			response.add(this.getError(ex));
		}

		return response;
	}
	
	@RequestMapping(value = "/action/favourite/delete/{favourite_id}", method = RequestMethod.GET, consumes = "application/json", produces = "application/json")
	@Secured({"ROLE_SUPERUSER", "ROLE_ADMIN"})
	public @ResponseBody RestResponse deleteFavourite(@PathVariable UUID favourite_id){
		RestResponse response = new RestResponse();
		
		try {
			repository.deleteFavourite(favourite_id);
			
		} catch (ApplicationException ex) {
			logger.error(ex.getMessage(), ex);
			response.add(this.getError(ex));
		}

		return response;
	}
}
