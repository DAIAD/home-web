package eu.daiad.utility.controller;

import java.util.ArrayList;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import eu.daiad.common.model.AuthenticatedRequest;
import eu.daiad.common.model.error.ApplicationException;
import eu.daiad.common.model.error.SharedErrorCode;
import eu.daiad.common.model.security.AuthenticatedUser;
import eu.daiad.common.model.security.Credentials;
import eu.daiad.common.model.security.EnumRole;

/**
 * Base controller class providing helper methods for authenticating API requests.
 */
public class BaseRestController extends BaseController {

    /**
     * A class that can process a specific {@link org.springframework.security.core.Authentication} implementation.
     */
    @Autowired
    private AuthenticationProvider authenticationProvider;

    /**
     * A bean validator that can be used to validate received input.
     */
    @Autowired
    @Qualifier("defaultBeanValidator")
    private Validator validator;

    /**
     * Authenticates a user and optionally, if authentication is successful,
     * sets the {@link org.springframework.security.core.Authentication} in the
     * security context.
     *
     * If one or more roles are given, the authenticated user must have at least
     * one of them. If the user does not have any of the roles specified, an
     * exception is thrown.
     *
     * @param credentials the user credentials.
     * @param roles the optional requested roles.
     * @return the authenticated user.
     * @throws ApplicationException if authentication fails or user does not have the required roles.
     */
    protected AuthenticatedUser authenticate(Credentials credentials, EnumRole... roles) throws ApplicationException {
        try {
            if (credentials == null) {
                throw createApplicationException(SharedErrorCode.AUTHENTICATION_NO_CREDENTIALS);
            }

            UsernamePasswordAuthenticationToken authentication = (UsernamePasswordAuthenticationToken) authenticationProvider
                            .authenticate(new UsernamePasswordAuthenticationToken(credentials.getUsername(), credentials.getPassword()));

            if (authentication == null) {
                throw createApplicationException(SharedErrorCode.AUTHENTICATION_USERNAME).set("username", credentials.getUsername());
            }

            // Check roles
            if ((roles != null) && (roles.length > 0)) {
                boolean hasRole = false;

                for (EnumRole role : roles) {
                    if (authentication.getAuthorities().contains(new SimpleGrantedAuthority(role.toString()))) {
                        hasRole = true;
                        break;
                    }
                }

                if (!hasRole) {
                    throw createApplicationException(SharedErrorCode.AUTHORIZATION_MISSING_ROLE).set("role", StringUtils.join(roles, ","));
                }
            }

            // Store authenticated user into the security context
            SecurityContextHolder.getContext().setAuthentication(authentication);

            return (AuthenticatedUser) authentication.getPrincipal();
        } catch (BadCredentialsException ex) {
            throw wrapApplicationException(ex, SharedErrorCode.AUTHENTICATION_USERNAME).set("username", credentials.getUsername());
        }
    }

    /**
     * Validate a REST request using default bean validator.
     *
     * @return a list of validation errors
     */
    protected <R extends AuthenticatedRequest> ArrayList<eu.daiad.common.model.error.Error> validate(R request)
    {
        ArrayList<eu.daiad.common.model.error.Error> errors = new ArrayList<>();
        Set<ConstraintViolation<R>> constraintViolations = validator.validate(request);

        if (constraintViolations.isEmpty())
            return errors;

        // The request is invalid: turn constraint violations into error DTO objects
        for (ConstraintViolation<R> c: constraintViolations) {
            eu.daiad.common.model.error.Error error = new eu.daiad.common.model.error.Error(
                SharedErrorCode.INVALID_FIELD.name(),
                "The field `" + c.getPropertyPath() + "` is invalid: " + c.getMessage());
            errors.add(error);
        }
        return errors;
    }
}
