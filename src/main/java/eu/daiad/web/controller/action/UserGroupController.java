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
import eu.daiad.web.model.group.CreateGroupSetRequest;
import eu.daiad.web.model.group.GroupInfoResponse;
import eu.daiad.web.model.group.GroupListInfoResponse;
import eu.daiad.web.model.group.GroupMemberInfoResponse;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.repository.application.IUserGroupRepository;

/**
 * Provides actions for managing user defined groups.
 *
 */
@RestController
public class UserGroupController extends BaseController {
	
	private static final Log logger = LogFactory.getLog(UserGroupController.class);
	
	@Autowired
	private IUserGroupRepository repository;
	
	/**
	 * Enumerates user defined groups.
	 * 
	 * @return the available groups.
	 */
	@RequestMapping(value = "/action/group/list", method = RequestMethod.GET, produces = "application/json")
	@Secured({"ROLE_SUPERUSER", "ROLE_ADMIN" })
	public RestResponse getGroupsInfo() {
		RestResponse response = new RestResponse();
		
		try{						
			return new GroupListInfoResponse(repository.getGroups());
			
		} catch (ApplicationException ex) {
			logger.error(ex.getMessage(), ex);

			response.add(this.getError(ex));
		}
		return response;
	}
	
	/**
	 * Get a group by its id.
	 * 
	 * @param group_id the group id.
	 * @return the controller's response.
	 */
	@RequestMapping(value = "/action/group/{group_id}", method = RequestMethod.GET, produces = "application/json")
	@Secured({"ROLE_SUPERUSER", "ROLE_ADMIN" })
	public RestResponse getGroupInfoByKey(@PathVariable UUID group_id) {
		RestResponse response = new RestResponse();
		
		try{
			return new GroupInfoResponse(repository.getSingleGroupByKey(group_id));
			
		} catch (ApplicationException ex) {
			logger.error(ex.getMessage(), ex);

			response.add(this.getError(ex));
		}
		return response;
	}
	
	/**
	 * Gets the members of a group.
	 * 
	 * @param group_id the group id.
	 * @return the contrller's response.
	 */
	@RequestMapping(value = "/action/group/members/current/{group_id}", method = RequestMethod.GET, produces = "application/json")
	@Secured({"ROLE_SUPERUSER", "ROLE_ADMIN" })
	public RestResponse getGroupCurrentMemberInfo(@PathVariable UUID group_id) {
		RestResponse response = new RestResponse();
		
		try{						
			return new GroupMemberInfoResponse(repository.getGroupCurrentMembers(group_id));
			
		} catch (ApplicationException ex) {
			logger.error(ex.getMessage(), ex);

			response.add(this.getError(ex));
		}
		return response;
	}
	
	/**
	 * Returns all the users that are eligible to join a new user defined group.
	 * 
	 * @return the users.
	 */
	@RequestMapping(value = "/action/group/members/possible/", method = RequestMethod.GET, produces = "application/json")
	@Secured({"ROLE_SUPERUSER", "ROLE_ADMIN" })
	public RestResponse getNewGroupPossibleMemberInfo() {
		RestResponse response = new RestResponse();
		
		try{		
			return new GroupMemberInfoResponse(repository.getGroupPossibleMembers(null));
			
		} catch (ApplicationException ex) {
			logger.error(ex.getMessage(), ex);

			response.add(this.getError(ex));
		}
		return response;
	}
	
	/**
	 * Returns the members that are eligible for joining a group.
	 * 
	 * @param group_id the group id.
	 * @return the controller's response.
	 */
	@RequestMapping(value = "/action/group/members/possible/{group_id}", method = RequestMethod.GET, produces = "application/json")
	@Secured({"ROLE_SUPERUSER", "ROLE_ADMIN" })
	public RestResponse getGroupPossibleMemberInfo(@PathVariable UUID group_id) {
		RestResponse response = new RestResponse();
		
		try{		
			return new GroupMemberInfoResponse(repository.getGroupPossibleMembers(group_id));
			
		} catch (ApplicationException ex) {
			logger.error(ex.getMessage(), ex);

			response.add(this.getError(ex));
		}
		return response;
	}
	
	/**
	 * Creates a new user defined set.
	 * 
	 * @param user the currently authenticated user
	 * @param groupSetInfo the group to create
	 * @return the controller's response
	 */
	@RequestMapping(value = "/action/group/set/create", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@Secured({"ROLE_SUPERUSER", "ROLE_ADMIN"})
	public @ResponseBody RestResponse createGroupSet(@AuthenticationPrincipal AuthenticatedUser user, @RequestBody CreateGroupSetRequest groupSetInfo){
		RestResponse response = new RestResponse();
		
		try {
			repository.createGroupSet(groupSetInfo);
			
		} catch (ApplicationException ex) {
			logger.error(ex.getMessage(), ex);
			response.add(this.getError(ex));
		}

		return response;
	}
	
	/**
	 * Deletes a user defined group by its id.
	 * 
	 * @param group_id the group id.
	 * @return the controller's response.
	 */
	@RequestMapping(value = "/action/group/delete/{group_id}", method = RequestMethod.GET, produces = "application/json")
	@Secured({"ROLE_SUPERUSER", "ROLE_ADMIN"})
	public @ResponseBody RestResponse deleteGroup(@PathVariable UUID group_id){
		RestResponse response = new RestResponse();
		
		try{
			repository.deleteGroup(group_id);
			
		} catch (ApplicationException ex) {
			logger.error(ex.getMessage(), ex);

			response.add(this.getError(ex));
		}
		return response;
	}
	
	/**
	 * It returns the list of groups in which the user is member.
	 * 
	 * @param user_id
	 * @return the user groups
	 */
	
	@RequestMapping(value = "/action/group/list/member/{key}", method = RequestMethod.GET, produces = "application/json")
	@Secured({"ROLE_SUPERUSER", "ROLE_ADMIN"})
	public @ResponseBody RestResponse getGroupsByMember(@PathVariable UUID key){
		RestResponse response = new RestResponse();
		
		try{
			return new GroupListInfoResponse(repository.getGroupsByMember(key));
			
		} catch (ApplicationException ex) {
			logger.error(ex.getMessage(), ex);

			response.add(this.getError(ex));
		}
		return response;
	}
}
