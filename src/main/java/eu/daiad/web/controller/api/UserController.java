package eu.daiad.web.controller.api;

import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.beanvalidation.OptionalValidatorFactoryBean;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.daiad.web.controller.BaseRestController;
import eu.daiad.web.model.RestResponse;
import eu.daiad.web.model.error.SharedErrorCode;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.model.security.EnumRole;
import eu.daiad.web.model.security.PasswordChangeRequest;
import eu.daiad.web.model.security.PasswordResetTokenCreateRequest;
import eu.daiad.web.model.security.PasswordResetTokenCreateResponse;
import eu.daiad.web.model.security.PasswordResetTokenRedeemRequest;
import eu.daiad.web.model.security.RoleUpdateRequest;
import eu.daiad.web.model.user.Account;
import eu.daiad.web.model.user.UserRegistrationRequest;
import eu.daiad.web.model.user.UserRegistrationResponse;
import eu.daiad.web.service.IUserService;

/**
 * Provides methods for user management
 */
@RestController("RestUserController")
public class UserController extends BaseRestController {

    /**
     * Logger instance for writing events using the configured logging API.
     */
    private static final Log logger = LogFactory.getLog(UserController.class);

    /**
     * Instance of @{link IUserService} that implements user management operations.
     */
    @Autowired
    private IUserService userService;

    /**
     * Instance of @{link org.springframework.validation.Validator} for performing user input validation manually.
     */
    @Autowired
    private org.springframework.validation.Validator validator;

    /**
     * True if white list checks must be applied; Otherwise False.
     */
    @Value("${security.white-list}")
    private boolean enforceWhiteListCheck;

    /**
     * Creates a new user.
     *
     * @param request the request.
     * @param results the binding results.
     * @return the controller's response.
     */
    @RequestMapping(value = "/api/v1/user/register", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public RestResponse register(@RequestBody UserRegistrationRequest request, BindingResult results) {
        RestResponse response = new RestResponse();

        try {
            if (enforceWhiteListCheck) {
                ((OptionalValidatorFactoryBean) validator).validate(request, results,
                                Account.AccountSimpleValidation.class);
            } else {
                ((OptionalValidatorFactoryBean) validator).validate(request.getAccount(), results,
                                Account.AccountDefaultValidation.class);
            }

            if (results.hasErrors()) {
                for (FieldError e : results.getFieldErrors()) {
                    response.add(this.getError(e));
                }

                return response;
            }

            UUID userKey = userService.createUser(request);

            UserRegistrationResponse registerResponse = new UserRegistrationResponse();
            registerResponse.setUserKey(userKey.toString());

            return registerResponse;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            response.add(this.getError(ex));
        }

        return response;
    }

    /**
     * Changes a user's password.
     *
     * @param data the request.
     * @return the controller's response.
     */
    @RequestMapping(value = "/api/v1/user/password/change", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public RestResponse changePassword(@RequestBody PasswordChangeRequest data) {
        RestResponse response = new RestResponse();

        try {
            AuthenticatedUser user = authenticate(data.getCredentials());

            if (user.hasRole(EnumRole.ROLE_SYSTEM_ADMIN, EnumRole.ROLE_UTILITY_ADMIN)) {
                if(StringUtils.isBlank(data.getUsername())) {
                    userService.changePassword(user.getUsername(), data.getPassword());
                } else {
                    userService.changePassword(data.getUsername(), data.getPassword());
                }
            } else if(user.hasRole(EnumRole.ROLE_USER)){
                if(StringUtils.isBlank(data.getUsername())) {
                    userService.changePassword(user.getUsername(), data.getPassword());
                } else {
                    throw createApplicationException(SharedErrorCode.AUTHORIZATION);
                }
            } else {
                throw createApplicationException(SharedErrorCode.AUTHORIZATION);
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            response.add(this.getError(ex));
        }

        return response;
    }

    /**
     * Requests a token for resetting a user's password.
     *
     * @param request the name of the user.
     * @return the controller's response.
     */
    @RequestMapping(value = "/api/v1/user/password/reset/token/create", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public RestResponse resetPasswordCreateToken(@RequestBody PasswordResetTokenCreateRequest request) {
        try {
            UUID token = userService.resetPasswordCreateToken(request.getUsername(), request.getApplication());

            return new PasswordResetTokenCreateResponse(token);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            // Do not send detailed error information
            return new RestResponse(getErrorUnknown());
        }
    }

    /**
     * Resets a user's password given a valid token and password.
     *
     * @param request the token and new password values.
     * @return the controller's response.
     */
    @RequestMapping(value = "/api/v1/user/password/reset/token/redeem", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public RestResponse resetPasswordRedeemToken(@RequestBody PasswordResetTokenRedeemRequest request) {
        RestResponse response = new RestResponse();

        try {
            userService.resetPasswordRedeemToken(request.getToken(), request.getPin(), request.getPassword());
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            response.add(this.getError(ex));
        }

        return response;
    }

    /**
     * Grants a role to a user.
     *
     * @param request the request.
     * @return the controller's response.
     */
    @RequestMapping(value = "/api/v1/user/role", method = RequestMethod.PUT, consumes = "application/json", produces = "application/json")
    public RestResponse addRole(@RequestBody RoleUpdateRequest request) {
        RestResponse response = new RestResponse();

        try {
            authenticate(request.getCredentials(), EnumRole.ROLE_SYSTEM_ADMIN, EnumRole.ROLE_UTILITY_ADMIN);

            userService.grantRole(request.getUsername(), request.getRole());
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            response.add(this.getError(ex));
        }

        return response;
    }

    /**
     * Revokes a role to a user.
     *
     * @param request the request.
     * @return the controller's response.
     */
    @RequestMapping(value = "/api/v1/user/role", method = RequestMethod.DELETE, consumes = "application/json", produces = "application/json")
    public RestResponse revokeRole(@RequestBody RoleUpdateRequest request) {
        RestResponse response = new RestResponse();

        try {
            authenticate(request.getCredentials(), EnumRole.ROLE_SYSTEM_ADMIN, EnumRole.ROLE_UTILITY_ADMIN);

            userService.revokeRole(request.getUsername(), request.getRole());
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            response.add(this.getError(ex));
        }

        return response;
    }
}
