package eu.daiad.web.repository.application;

import java.util.List;
import java.util.UUID;

import eu.daiad.web.domain.application.GroupSegment;
import eu.daiad.web.model.admin.AccountActivity;
import eu.daiad.web.model.admin.AccountWhiteListEntry;
import eu.daiad.web.model.admin.AccountWhiteListInfo;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.query.EnumClusterType;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.model.security.EnumRole;
import eu.daiad.web.model.user.Account;

public interface IUserRepository {

	void initializeSecurityConfiguration();

	UUID createUser(Account user) throws ApplicationException;

	void setPassword(String username, String password) throws ApplicationException;

	void setRole(String username, EnumRole role, boolean set) throws ApplicationException;

	AuthenticatedUser getUserByName(String username) throws ApplicationException;

	AuthenticatedUser getUserByKey(UUID key) throws ApplicationException;

	AuthenticatedUser getUserByUtilityAndKey(int utilityId, UUID key) throws ApplicationException;

	AccountWhiteListEntry getAccountWhiteListEntry(String username);

	void insertAccountWhiteListEntry(AccountWhiteListInfo userInfo);

	void updateLoginStats(int id, boolean success);

	List<AccountActivity> getAccountActivity();

	List<AccountActivity> getAccountActivity(int utilityId);

	List<UUID> getUserKeysForGroup(UUID groupKey);

	List<UUID> getUserKeysForUtility();

	List<UUID> getUserKeysForUtility(UUID utilityKey);

	List<UUID> getUserKeysForUtility(int utilityId);

	List<GroupSegment> getClusterGroupByKey(UUID key);

	List<GroupSegment> getClusterGroupByName(String name);

	List<GroupSegment> getClusterGroupByType(EnumClusterType type);

}
