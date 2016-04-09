package eu.daiad.web.repository.application;

import java.util.List;
import java.util.UUID;

import eu.daiad.web.model.admin.AccountActivity;
import eu.daiad.web.model.admin.AccountWhiteListEntry;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.model.security.EnumRole;
import eu.daiad.web.model.user.Account;

public interface IUserRepository {

	void initializeSecurityConfiguration();

	UUID createUser(Account user) throws ApplicationException;

	void setPassword(String username, String password) throws ApplicationException;

	void setRole(String username, EnumRole role, boolean set) throws ApplicationException;

	AuthenticatedUser getUserByName(String username) throws ApplicationException;

	AuthenticatedUser getUserByUtilityAndKey(int utilityId, UUID key) throws ApplicationException;

	AccountWhiteListEntry getAccountWhiteListEntry(String username);

	void updateLoginStats(int id, boolean success);

	List<AccountActivity> getAccountActivity(int utilityId);

}
