package eu.daiad.web.data;

import java.util.UUID;

import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.model.security.EnumRole;
import eu.daiad.web.model.user.Account;

public interface IUserRepository {

	void initializeSecurityConfiguration();

	UUID createUser(Account user) throws ApplicationException;

	void setPassword(String username, String password) throws ApplicationException;

	void setRole(String username, EnumRole role, boolean set) throws ApplicationException;

	public AuthenticatedUser getUserByName(String username) throws ApplicationException;

}
