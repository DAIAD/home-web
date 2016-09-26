package eu.daiad.web.repository.application;

import java.util.List;
import java.util.UUID;

import eu.daiad.web.model.admin.AccountActivity;
import eu.daiad.web.model.admin.AccountWhiteListEntry;
import eu.daiad.web.model.admin.AccountWhiteListInfo;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.model.security.EnumRole;
import eu.daiad.web.model.security.PasswordResetToken;
import eu.daiad.web.model.user.Account;
import eu.daiad.web.model.user.UserInfo;
import eu.daiad.web.model.user.UserQuery;
import eu.daiad.web.model.user.UserQueryResult;

public interface IUserRepository {

    void initializeSecurityConfiguration();

    UUID createUser(Account user) throws ApplicationException;

    List<UserInfo> filterUserByPrefix(String prefix);

    void changePassword(String username, String password) throws ApplicationException;

    PasswordResetToken createPasswordResetToken(String username) throws ApplicationException;

    void resetPassword(UUID token, String pin, String password) throws ApplicationException;

    void grantRole(String username, EnumRole role) throws ApplicationException;

    void revokeRole(String username, EnumRole role) throws ApplicationException;

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

    UserInfo getUserInfoByKey(UUID user_id);

    UserQueryResult search(UserQuery query);
    
    eu.daiad.web.domain.application.Account getAccountByKey(UUID key);
    
    eu.daiad.web.domain.application.Account getAccountByUsername(String username);

}
