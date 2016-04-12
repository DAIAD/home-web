package eu.daiad.web.service;

import java.util.UUID;

import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.security.EnumRole;
import eu.daiad.web.model.user.UserRegistrationRequest;

public interface IUserService {

	abstract UUID createUser(UserRegistrationRequest request) throws ApplicationException;

	abstract void setPassword(String username, String password) throws ApplicationException;

	abstract void setRole(String username, EnumRole role, boolean set) throws ApplicationException;

}
