package eu.daiad.web.data;

import java.util.UUID;

import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.model.security.EnumRole;
import eu.daiad.web.model.user.Account;

public interface IUserRepository {

	void createDefaultUser();

	UUID createUser(Account user) throws Exception;

	void setPassword(String username, String password) throws Exception;

	void setRole(String username, EnumRole role, boolean set) throws Exception;

	public AuthenticatedUser getUserByName(String username) throws Exception;

}
