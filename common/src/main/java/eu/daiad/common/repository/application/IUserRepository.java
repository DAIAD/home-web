package eu.daiad.common.repository.application;

import java.util.List;
import java.util.UUID;

import eu.daiad.common.domain.application.AccountEntity;
import eu.daiad.common.domain.application.SurveyEntity;
import eu.daiad.common.model.EnumApplication;
import eu.daiad.common.model.admin.AccountActivity;
import eu.daiad.common.model.admin.AccountWhiteListEntry;
import eu.daiad.common.model.admin.AccountWhiteListInfo;
import eu.daiad.common.model.error.ApplicationException;
import eu.daiad.common.model.security.AuthenticatedUser;
import eu.daiad.common.model.security.EnumRole;
import eu.daiad.common.model.security.PasswordResetToken;
import eu.daiad.common.model.user.Account;
import eu.daiad.common.model.user.UserInfo;
import eu.daiad.common.model.user.UserQuery;
import eu.daiad.common.model.user.UserQueryResult;

public interface IUserRepository {

    void initializeSecurityConfiguration();

    UUID createUser(Account user) throws ApplicationException;

    List<UserInfo> filterUserByPrefix(String prefix);

    void changePassword(String username, String password) throws ApplicationException;

    PasswordResetToken createPasswordResetToken(EnumApplication application, String username) throws ApplicationException;

    PasswordResetToken getPasswordResetTokenById(UUID token);

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

    List<UUID> getUserKeysForGroup(UUID groupKey);

    List<UUID> getUserKeysForUtility();

    List<UUID> getUserKeysForUtility(UUID utilityKey);

    List<UUID> getUserKeysForUtility(int utilityId);

    UserInfo getUserInfoByKey(UUID key);

    UserQueryResult search(UserQuery query);

    AccountEntity getAccountByKey(UUID key);

    AccountEntity getAccountByUsername(String username);

    AccountEntity findOne(int id);

    List<SurveyEntity> getSurveyDataByUtilityId(int utilityId);

    SurveyEntity getSurveyByKey(UUID userKey);

    AccountEntity getUserByMeterSerial(String serial);

}
