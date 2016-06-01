package eu.daiad.web.service;

import java.util.UUID;

import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.security.EnumRole;
import eu.daiad.web.model.user.UserRegistrationRequest;

public interface IUserService {

	/**
	 * Creates a new user.
	 * 
	 * @param request the user to create.
	 * @return the new user key.
	 * @throws ApplicationException if the user already exists.
	 */
	abstract UUID createUser(UserRegistrationRequest request) throws ApplicationException;

	/**
	 * Sets a user password.
	 * 
	 * @param username the user name .
	 * @param password the new password.
	 * @throws ApplicationException if the user does not exist.
	 */
	abstract void setPassword(String username, String password) throws ApplicationException;

	/**
	 * Grants or revokes a role to a user.
	 * 
	 * @param username the user name.
	 * @param role the role to grant or revoke.
	 * @param set true if the role is being granted; Otherwise false.
	 * @throws ApplicationException if the user or the role does not exist.
	 */
	abstract void setRole(String username, EnumRole role, boolean set) throws ApplicationException;

}
