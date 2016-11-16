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
import eu.daiad.web.model.admin.AccountActivityResponse;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.group.CreateGroupSetRequest;
import eu.daiad.web.model.group.GroupInfoResponse;
import eu.daiad.web.model.group.GroupListInfoResponse;
import eu.daiad.web.model.group.GroupMemberInfoResponse;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.model.security.RoleConstant;
import eu.daiad.web.repository.application.IUserGroupRepository;

/**
 * Provides actions for managing user defined groups.
 */
@RestController
public class UserGroupController extends BaseController {

    /**
     * Logger instance for writing events using the configured logging API.
     */
    private static final Log logger = LogFactory.getLog(UserGroupController.class);

    /**
     * Repository for accessing user defined groups.
     */
    @Autowired
    private IUserGroupRepository userGroupRepository;

    /**
     * Enumerates user defined groups.
     *
     * @return the available groups.
     */
    @RequestMapping(value = "/action/group/list", method = RequestMethod.GET, produces = "application/json")
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public RestResponse getGroupsInfo() {
        try{
            return new GroupListInfoResponse(userGroupRepository.getGroups());
        } catch (ApplicationException ex) {
            logger.error(ex.getMessage(), ex);

            return new RestResponse(getError(ex));
        }
    }

    /**
     * Get a group by its id.
     *
     * @param key the group key.
     * @return the controller's response.
     */
    @RequestMapping(value = "/action/group/{key}", method = RequestMethod.GET, produces = "application/json")
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public RestResponse getGroupInfoByKey(@PathVariable UUID key) {
        try{
            return new GroupInfoResponse(userGroupRepository.getSingleGroupByKey(key));
        } catch (ApplicationException ex) {
            logger.error(ex.getMessage(), ex);

            return new RestResponse(getError(ex));
        }
    }

    /**
     * Gets the members of a group.
     *
     * @param key the group key.
     * @return the contrller's response.
     */
    @RequestMapping(value = "/action/group/members/current/{key}", method = RequestMethod.GET, produces = "application/json")
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public RestResponse getGroupCurrentMemberInfo(@PathVariable UUID key) {
        try{
            return new GroupMemberInfoResponse(userGroupRepository.getGroupCurrentMembers(key));
        } catch (ApplicationException ex) {
            logger.error(ex.getMessage(), ex);

            return new RestResponse(getError(ex));
        }
    }

    /**
     * Returns the accounts that are members of the group.
     *
     * @param key the group key.
     * @return the controller's response.
     */
    @RequestMapping(value = "/action/group/accounts/current/{key}", method = RequestMethod.GET, produces = "application/json")
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public RestResponse getGroupAccounts(@PathVariable UUID key) {
        try{
            AccountActivityResponse activityResponse = new AccountActivityResponse();
            activityResponse.getAccounts().addAll(userGroupRepository.getGroupAccounts(key));

            return activityResponse;
        } catch (ApplicationException ex) {
            logger.error(ex.getMessage(), ex);

            return new RestResponse(getError(ex));
        }
    }

    /**
     * Returns all the users that are eligible to join a new user defined group.
     *
     * @return the users.
     */
    @RequestMapping(value = "/action/group/members/possible/", method = RequestMethod.GET, produces = "application/json")
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public RestResponse getNewGroupPossibleMemberInfo() {
        try{
            return new GroupMemberInfoResponse(userGroupRepository.getGroupPossibleMembers(null));

        } catch (ApplicationException ex) {
            logger.error(ex.getMessage(), ex);

             return new RestResponse(getError(ex));
        }
    }

    /**
     * Returns the members that are eligible for joining a group.
     *
     * @param key the group key.
     * @return the controller's response.
     */
    @RequestMapping(value = "/action/group/members/possible/{key}", method = RequestMethod.GET, produces = "application/json")
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public RestResponse getGroupPossibleMemberInfo(@PathVariable UUID key) {
        try{
            return new GroupMemberInfoResponse(userGroupRepository.getGroupPossibleMembers(key));
        } catch (ApplicationException ex) {
            logger.error(ex.getMessage(), ex);

            return new RestResponse(getError(ex));
        }
    }

    /**
     * Creates a new user defined set.
     *
     * @param user the currently authenticated user
     * @param groupSetInfo the group to create
     * @return the controller's response
     */
    @RequestMapping(value = "/action/group/set/create", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public @ResponseBody RestResponse createGroupSet(@AuthenticationPrincipal AuthenticatedUser user, @RequestBody CreateGroupSetRequest groupSetInfo){
        try {
            userGroupRepository.createGroupSet(groupSetInfo);
        } catch (ApplicationException ex) {
            logger.error(ex.getMessage(), ex);

            return new RestResponse(getError(ex));
        }

        return new RestResponse();
    }

    /**
     * Deletes a user defined group by its id.
     *
     * @param key the group key.
     * @return the controller's response.
     */
    @RequestMapping(value = "/action/group/delete/{key}", method = RequestMethod.GET, produces = "application/json")
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public @ResponseBody RestResponse deleteGroup(@PathVariable UUID key){
        try{
            userGroupRepository.deleteGroup(key);
        } catch (ApplicationException ex) {
            logger.error(ex.getMessage(), ex);

            return new RestResponse(getError(ex));
        }
        return new RestResponse();
    }

    /**
     * It returns the list of groups in which the user is member.
     *
     * @param key
     * @return the user groups
     */
    @RequestMapping(value = "/action/group/list/member/{key}", method = RequestMethod.GET, produces = "application/json")
    @Secured({ RoleConstant.ROLE_UTILITY_ADMIN, RoleConstant.ROLE_SYSTEM_ADMIN })
    public @ResponseBody RestResponse getGroupsByMember(@PathVariable UUID key){
        try{
            return new GroupListInfoResponse(userGroupRepository.getGroupsByMember(key));
        } catch (ApplicationException ex) {
            logger.error(ex.getMessage(), ex);

            return new RestResponse(getError(ex));
        }
    }
}
