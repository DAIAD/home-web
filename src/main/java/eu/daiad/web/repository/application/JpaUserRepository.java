package eu.daiad.web.repository.application;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.ParameterMode;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.StoredProcedureQuery;
import javax.persistence.TypedQuery;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import eu.daiad.web.domain.application.AccountActivityEntity;
import eu.daiad.web.domain.application.AccountEntity;
import eu.daiad.web.domain.application.AccountProfileEntity;
import eu.daiad.web.domain.application.AccountProfileHistoryEntity;
import eu.daiad.web.domain.application.AccountRoleEntity;
import eu.daiad.web.domain.application.AccountUtilityEntity;
import eu.daiad.web.domain.application.AccountWhiteListEntity;
import eu.daiad.web.domain.application.HouseholdEntity;
import eu.daiad.web.domain.application.HouseholdMemberEntity;
import eu.daiad.web.domain.application.PasswordResetTokenEntity;
import eu.daiad.web.domain.application.RoleEntity;
import eu.daiad.web.domain.application.SurveyEntity;
import eu.daiad.web.domain.application.UtilityEntity;
import eu.daiad.web.model.EnumApplication;
import eu.daiad.web.model.EnumGender;
import eu.daiad.web.model.EnumValueDescription;
import eu.daiad.web.model.admin.AccountActivity;
import eu.daiad.web.model.admin.AccountWhiteListEntry;
import eu.daiad.web.model.admin.AccountWhiteListInfo;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.error.SharedErrorCode;
import eu.daiad.web.model.error.UserErrorCode;
import eu.daiad.web.model.profile.EnumMobileMode;
import eu.daiad.web.model.profile.EnumUtilityMode;
import eu.daiad.web.model.profile.EnumWebMode;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.model.security.EnumRole;
import eu.daiad.web.model.security.PasswordResetToken;
import eu.daiad.web.model.user.Account;
import eu.daiad.web.model.user.UserInfo;
import eu.daiad.web.model.user.UserQuery;
import eu.daiad.web.model.user.UserQueryResult;
import eu.daiad.web.repository.BaseRepository;

/**
 * Provides methods for managing user accounts.
 */
@Repository
@Transactional("applicationTransactionManager")
public class JpaUserRepository extends BaseRepository implements IUserRepository {

    /**
     * Logger instance for writing events using the configured logging API.
     */
    private static final Log logger = LogFactory.getLog(JpaUserRepository.class);

    /**
     * Default utility name. If white list functionality is disabled, a new user
     * account is assigned to the default utility.
     */
    private static final String DEFAULT_UTILITY_NAME = "DAIAD";

    /**
     * Enables/Disables white list functionality.
     */
    @Value("${security.white-list}")
    private boolean enforceWhiteListCheck;

    /**
     * Password reset token interval in hours.
     */
    @Value("${daiad.password.reset.token.duration}")
    private int passwordResetTokenDuration;

    /**
     *  Java Persistence entity manager.
     */
    @PersistenceContext(unitName = "default")
    EntityManager entityManager;

    /**
     * Initializes roles.
     *
     * @throws ApplicationException if role initialization has failed.
     */
    private void initializeRoles() throws ApplicationException {
        try {
            for (EnumRole r : EnumRole.class.getEnumConstants()) {
                TypedQuery<RoleEntity> roleQuery = entityManager.createQuery("select r from role r where r.name = :name", RoleEntity.class);
                roleQuery.setParameter("name", r.toString());

                List<RoleEntity> roles = roleQuery.getResultList();
                if (roles.size() == 0) {
                    RoleEntity role = new RoleEntity();

                    String description = EnumRole.class.getField(r.name()).getAnnotation(EnumValueDescription.class).value();
                    role.setName(r.name());
                    role.setDescription(description);

                    entityManager.persist(role);
                    entityManager.flush();
                }
            }
        } catch (Exception ex) {
            throw wrapApplicationException(ex, UserErrorCode.ROLE_INITIALIZATION);
        }
    }

    /**
     * Initializes administration accounts for registered utilities.
     *
     * @throws ApplicationException if account creation has failed.
     */
    private void initializeAdministrators() throws ApplicationException {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        try {
            String utilityQueryString = "select u from utility u";

            TypedQuery<UtilityEntity> utilityQuery = entityManager.createQuery(utilityQueryString, UtilityEntity.class);

            for (UtilityEntity utility : utilityQuery.getResultList()) {
                String accountQueryString = "select a from account a where a.username = :username";

                TypedQuery<AccountEntity> userQuery = entityManager.createQuery(accountQueryString,  AccountEntity.class);
                userQuery.setParameter("username", utility.getDefaultAdministratorUsername());

                List<AccountEntity> users = userQuery.getResultList();

                if (users.isEmpty()) {
                    String password = UUID.randomUUID().toString();

                    // Create account
                    AccountEntity account = new AccountEntity();
                    account.setUsername(utility.getDefaultAdministratorUsername());
                    account.setPassword(encoder.encode(password));
                    account.setLocked(false);
                    account.setChangePasswordOnNextLogin(false);
                    account.setUtility(utility);
                    account.setLocale(Locale.ENGLISH.getLanguage());

                    String roleQueryString = "select r from role r where r.name = :name";

                    TypedQuery<RoleEntity> roleQuery = entityManager.createQuery(roleQueryString, RoleEntity.class);
                    roleQuery.setParameter("name", EnumRole.ROLE_UTILITY_ADMIN.name());

                    // Assign role
                    RoleEntity role = roleQuery.getSingleResult();

                    AccountRoleEntity assignedRole = new AccountRoleEntity();
                    assignedRole.setRole(role);
                    assignedRole.setAssignedOn(account.getCreatedOn());
                    assignedRole.setAssignedBy(account);

                    account.getRoles().add(assignedRole);

                    // Assign utilities
                    AccountUtilityEntity accountUtility = new AccountUtilityEntity();
                    accountUtility.setOwner(account);
                    accountUtility.setUtility(utility);
                    accountUtility.setAssignedOn(account.getCreatedOn());

                    account.getUtilities().add(accountUtility);

                    // Create account
                    entityManager.persist(account);
                    entityManager.flush();

                    // Create profile
                    AccountProfileEntity profile = new AccountProfileEntity();
                    profile.setMobileMode(EnumMobileMode.INACTIVE.getValue());
                    profile.setWebMode(EnumWebMode.INACTIVE.getValue());
                    profile.setUtilityMode(EnumUtilityMode.ACTIVE.getValue());
                    profile.setUpdatedOn(account.getCreatedOn());

                    profile.setAccount(account);
                    entityManager.persist(profile);

                    // Create profile history entry
                    AccountProfileHistoryEntity entry = new AccountProfileHistoryEntity();
                    entry.setVersion(profile.getVersion());
                    entry.setUpdatedOn(account.getCreatedOn());
                    entry.setMobileMode(profile.getMobileMode());
                    entry.setWebMode(profile.getWebMode());
                    entry.setUtilityMode(profile.getUtilityMode());

                    entry.setProfile(profile);
                    entityManager.persist(entry);

                    // Log account creation and random password
                    logger.info(String.format("Default administrator has been crearted for utility [%s]. User name : %s. Password : %s",
                                              utility.getName(),
                                              utility.getDefaultAdministratorUsername(),
                                              password));
                }
            }
        } catch (Exception ex) {
            throw wrapApplicationException(ex, UserErrorCode.ADMIN_INITIALIZATION);
        }
    }

    /**
     * Initializes application security configuration.
     */
    @Override
    public void initializeSecurityConfiguration() {
        try {
            // Initialize all system roles
            initializeRoles();

            // Create an administrator for any registered utility
            initializeAdministrators();
        } catch (ApplicationException ex) {
            logger.error("Database initialization has failed.", ex);
        }
    }

    /**
     * Checks if the user name is reserved.
     *
     * @param username the user name to check.
     * @return if the user name is reserved.
     */
    private boolean isUsernameReserved(String username) {
        String userQueryString = "select u from utility u where u.defaultAdministratorUsername = :username";

        TypedQuery<UtilityEntity> userQuery = entityManager.createQuery(userQueryString, UtilityEntity.class);

        userQuery.setParameter("username", username);

        return (!userQuery.getResultList().isEmpty());
    }

    /**
     * Creates a new account.
     *
     * @param user the account data
     * @throws ApplicationException if account creation has failed.
     */
    @Override
    public UUID createUser(Account user) throws ApplicationException {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        try {
            // Check if user name is available
            if (isUsernameReserved(user.getUsername())) {
                throw createApplicationException(UserErrorCode.USERNANE_RESERVED).set("username", user.getUsername());
            }
            if (getUserByName(user.getUsername()) != null) {
                throw createApplicationException(UserErrorCode.USERNANE_NOT_AVAILABLE).set("username", user.getUsername());
            }

            // Get matching white list entry if the white least feature is supported
            AccountWhiteListEntity whiteListEntry = null;

            if (enforceWhiteListCheck) {
                String whiteListQueryString = "select a from account_white_list a where a.username = :username";

                TypedQuery<AccountWhiteListEntity> query = entityManager.createQuery(whiteListQueryString, AccountWhiteListEntity.class)
                                                                        .setFirstResult(0)
                                                                        .setMaxResults(1);
                query.setParameter("username", user.getUsername());

                List<AccountWhiteListEntity> result = query.getResultList();
                if (result.isEmpty()) {
                    throw createApplicationException(UserErrorCode.WHITELIST_MISMATCH).set("username", user.getUsername());
                } else {
                    whiteListEntry = result.get(0);
                }
            }

            // Decide utility to which the user is assigned to
            UtilityEntity utility = null;

            if (whiteListEntry != null) {
                String utilityQueryString = "select u from utility u where u.id = :id";

                TypedQuery<UtilityEntity> query = entityManager.createQuery(utilityQueryString, UtilityEntity.class);
                query.setParameter("id", whiteListEntry.getUtility().getId());

                utility = query.getSingleResult();
            } else {
                String utilityQueryString = "select u from utility u where u.name = :name";

                TypedQuery<UtilityEntity> query = entityManager.createQuery(utilityQueryString, UtilityEntity.class);
                query.setParameter("name", DEFAULT_UTILITY_NAME);

                utility = query.getSingleResult();
            }

            // Create and initialize user
            AccountEntity account = new AccountEntity();
            account.setUsername(user.getUsername());
            account.setPassword(encoder.encode(user.getPassword()));

            account.setEmail(user.getUsername());

            if (whiteListEntry != null) {
                // Set user properties from the white list
                account.setFirstname(whiteListEntry.getFirstname());
                account.setLastname(whiteListEntry.getLastname());
                account.setBirthdate(whiteListEntry.getBirthdate());
                account.setGender(whiteListEntry.getGender());

                account.setLocale(whiteListEntry.getLocale());
                account.setCountry(whiteListEntry.getCountry());
                account.setCity(whiteListEntry.getCity());
                account.setAddress(whiteListEntry.getAddress());
                account.setTimezone(whiteListEntry.getTimezone());
                account.setPostalCode(whiteListEntry.getPostalCode());

                if ((whiteListEntry.getLocation() != null) && (whiteListEntry.getLocation() instanceof Point)) {
                    account.setLocation(whiteListEntry.getLocation());
                } else if ((whiteListEntry.getMeterLocation() != null)
                                && (whiteListEntry.getMeterLocation() instanceof Point)) {
                    account.setLocation(whiteListEntry.getMeterLocation());
                }
            } else {
                // Set user properties
                account.setFirstname(user.getFirstname());
                account.setLastname(user.getLastname());
                account.setBirthdate(user.getBirthdate());
                account.setGender(user.getGender());

                account.setLocale(user.getLocale());
                account.setCountry(user.getCountry());
                account.setCity(user.getCity());
                account.setAddress(user.getAddress());
                account.setTimezone(user.getTimezone());
                account.setPostalCode(user.getPostalCode());

                if ((user.getLocation() != null) && (user.getLocation() instanceof Point)) {
                    account.setLocation(user.getLocation());
                }

                account.setPhoto(user.getPhoto());
            }

            account.setLocked(false);
            account.setChangePasswordOnNextLogin(false);
            account.setAllowPasswordReset(true);

            account.setUtility(utility);

            // Assign default ROLE_USER role to the new user
            RoleEntity role = null;
            TypedQuery<RoleEntity> roleQuery = entityManager.createQuery("select r from role r where r.name = :name",
                            RoleEntity.class);
            roleQuery.setParameter("name", EnumRole.ROLE_USER.toString());

            role = roleQuery.getSingleResult();

            AccountRoleEntity assignedRole = new AccountRoleEntity();
            assignedRole.setRole(role);
            assignedRole.setAssignedOn(account.getCreatedOn());

            account.getRoles().add(assignedRole);

            // Assign utilities
            AccountUtilityEntity accountUtility = new AccountUtilityEntity();
            accountUtility.setOwner(account);
            accountUtility.setUtility(utility);
            accountUtility.setAssignedOn(account.getCreatedOn());

            account.getUtilities().add(accountUtility);

            // Create user
            entityManager.persist(account);
            entityManager.flush();

            // Initialize user profile
            AccountProfileEntity profile = new AccountProfileEntity();

            if (utility.getDefaultMobileMode() == EnumMobileMode.UNDEFINED) {
                profile.setMobileMode(EnumMobileMode.INACTIVE.getValue());
            } else {
                profile.setMobileMode(utility.getDefaultMobileMode().getValue());
            }

            if (utility.getDefaultWebMode() == EnumWebMode.UNDEFINED) {
                profile.setWebMode(EnumWebMode.INACTIVE.getValue());
            } else {
                profile.setWebMode(utility.getDefaultWebMode().getValue());
            }

            profile.setSocialEnabled(utility.isDefaultSocialEnabled());

            // Always disable utility mode
            profile.setUtilityMode(EnumUtilityMode.INACTIVE.getValue());

            profile.setUpdatedOn(account.getCreatedOn());

            profile.setAccount(account);
            entityManager.persist(profile);

            // Create historical record for the first profile update
            AccountProfileHistoryEntity profileHistoryEntry = new AccountProfileHistoryEntity();
            profileHistoryEntry.setVersion(profile.getVersion());
            profileHistoryEntry.setUpdatedOn(account.getCreatedOn());
            profileHistoryEntry.setMobileMode(profile.getMobileMode());
            profileHistoryEntry.setWebMode(profile.getWebMode());
            profileHistoryEntry.setUtilityMode(profile.getUtilityMode());
            profileHistoryEntry.setSocialEnabled(profile.isSocialEnabled());

            profileHistoryEntry.setProfile(profile);
            entityManager.persist(profileHistoryEntry);

            // Update white list
            if (whiteListEntry != null) {
                whiteListEntry.setRegisteredOn(DateTime.now());
                whiteListEntry.setAccount(account);
            }

            // Optionally load data from survey table
            SurveyEntity survey = null;

            TypedQuery<SurveyEntity> surveyQuery = entityManager.createQuery(
                            "select s from survey s where s.username = :username", SurveyEntity.class).setFirstResult(0)
                            .setMaxResults(1);
            surveyQuery.setParameter("username", account.getUsername());

            List<SurveyEntity> surveys = surveyQuery.getResultList();
            if (!surveys.isEmpty()) {
                survey = surveys.get(0);
            }

            // Initialize household
            HouseholdEntity household = new HouseholdEntity();
            household.setAccount(account);
            household.setCreatedOn(account.getCreatedOn());
            household.setUpdatedOn(account.getCreatedOn());
            entityManager.persist(household);

            HouseholdMemberEntity householdMember = new HouseholdMemberEntity();

            householdMember.setIndex(0);
            householdMember.setCreatedOn(account.getCreatedOn());
            householdMember.setUpdatedOn(account.getCreatedOn());
            householdMember.setGender(account.getGender());
            householdMember.setName(account.getFirstname());
            householdMember.setPhoto(account.getPhoto());
            if (account.getBirthdate() != null) {
                householdMember.setAge(new Period(account.getBirthdate(), DateTime.now()).getYears());
            } else if ((survey != null) && (survey.getAge() != null)) {
                householdMember.setAge(survey.getAge());
            }
            householdMember.setHousehold(household);

            entityManager.persist(householdMember);
            entityManager.flush();

            return account.getKey();
        } catch (Exception ex) {
            throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
        }
    }

    /**
     * Changes user password.
     *
     * @param username the user name.
     * @param password the new password.
     */
    @Override
    public void changePassword(String username, String password) throws ApplicationException {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String userQueryString = "select a from account a where a.username = :username";

        TypedQuery<AccountEntity> userQuery = entityManager.createQuery(userQueryString, AccountEntity.class);
        userQuery.setParameter("username", username);

        AccountEntity user = userQuery.getSingleResult();

        user.setPassword(encoder.encode(password));

        entityManager.flush();

        // Log password changes
        logger.warn(String.format("Password for user [%s] has been updated", username));
    }

    /**
     * Create password reset token.
     *
     * @param application the application which requested the password reset token.
     * @param username the name of the user who requested the password reset token.
     */
    @Override
    public PasswordResetToken createPasswordResetToken(EnumApplication application, String username) throws ApplicationException {
        // Find user
        String userQueryString = "select a from account a where a.username = :username";

        TypedQuery<AccountEntity> accountQuery = entityManager.createQuery(userQueryString, AccountEntity.class)
                                                              .setFirstResult(0)
                                                              .setMaxResults(1);
        accountQuery.setParameter("username", username);

        List<AccountEntity> accounts = accountQuery.getResultList();
        if (accounts.isEmpty()) {
            throw createApplicationException(UserErrorCode.USERNANE_NOT_FOUND).set("username", username);
        }

        AccountEntity account = accounts.get(0);

        // Reset any existing tokens that are still valid
        String tokenQueryString = "select t from password_reset_token t where t.valid = true and t.account.id = :accountId";

        TypedQuery<PasswordResetTokenEntity> tokenQuery = entityManager.createQuery(tokenQueryString, PasswordResetTokenEntity.class);
        tokenQuery.setParameter("accountId", account.getId());

        for(PasswordResetTokenEntity oldToken : tokenQuery.getResultList()){
            oldToken.setValid(false);
        }

        // Create new token
        PasswordResetTokenEntity token = new PasswordResetTokenEntity();

        token.setAccount(account);
        token.setValid(true);
        token.setPin(RandomStringUtils.randomNumeric(4));
        token.setApplication(application);

        entityManager.persist(token);

        String locale = account.getLocale();
        if(StringUtils.isBlank(locale)) {
            locale = account.getUtility().getLocale();
        }
        if(StringUtils.isBlank(locale)) {
            locale = "en";
        }

        return new PasswordResetToken(token.getToken(), token.getPin(), locale);
    }

    /**
     * Find a password reset token by id.
     *
     * @param token the token id to search for.
     */
    @Override
    public PasswordResetToken getPasswordResetTokenById(UUID token) {
        String tokenQueryString = "select t from password_reset_token t where t.token = :token";

        TypedQuery<PasswordResetTokenEntity> tokenQuery = entityManager.createQuery(tokenQueryString, PasswordResetTokenEntity.class);
        tokenQuery.setParameter("token", token);

        List<PasswordResetTokenEntity> tokens = tokenQuery.getResultList();

        if (tokens.isEmpty()) {
            return null;
        }

        PasswordResetTokenEntity passwordResetTokenEntity = tokens.get(0);

        if(passwordResetTokenEntity.isExpired(passwordResetTokenDuration)) {
            return null;
        }

        if(passwordResetTokenEntity.isReedemed()) {
            return null;
        }

        if(!passwordResetTokenEntity.isValid()) {
            return null;
        }

        String locale = passwordResetTokenEntity.getAccount().getLocale();
        if(StringUtils.isBlank(locale)) {
            locale = passwordResetTokenEntity.getAccount().getUtility().getLocale();
        }
        if(StringUtils.isBlank(locale)) {
            locale = "en";
        }

        return new PasswordResetToken(passwordResetTokenEntity.getToken(), passwordResetTokenEntity.getPin(), locale);
    }

    /**
     * Resets user password.
     *
     * @param token a valid password reset token.
     * @param pin a 4-digit number used for validating the user email address.
     * @param password the new password.
     */
    @Override
    public void resetPassword(UUID token, String pin, String password) throws ApplicationException {
        // Find token
        String tokenQueryString = "select t from password_reset_token t where t.token = :token";

        TypedQuery<PasswordResetTokenEntity> tokenQuery = entityManager.createQuery(tokenQueryString, PasswordResetTokenEntity.class);
        tokenQuery.setParameter("token", token);

        List<PasswordResetTokenEntity> tokens = tokenQuery.getResultList();

        if (tokens.isEmpty()) {
            throw createApplicationException(UserErrorCode.PASSWORD_RESET_TOKEN_NOT_FOUND);
        }

        PasswordResetTokenEntity passwordResetTokenEntity = tokens.get(0);

        // Validate token
        if (passwordResetTokenEntity.isReedemed()) {
            throw createApplicationException(UserErrorCode.PASSWORD_RESET_TOKEN_ALREADY_REEDEMED);
        }

        if (passwordResetTokenEntity.isExpired(passwordResetTokenDuration)) {
            throw createApplicationException(UserErrorCode.PASSWORD_RESET_TOKEN_EXPIRED);
        }

        if ((passwordResetTokenEntity.getApplication() == EnumApplication.MOBILE)
                        && (!passwordResetTokenEntity.getPin().equals(pin))) {
            throw createApplicationException(UserErrorCode.PASSWORD_RESET_PIN_MISMATCH);
        }

        // Get account
        AccountEntity account = passwordResetTokenEntity.getAccount();
        if (account == null) {
            throw createApplicationException(UserErrorCode.PASSWORD_RESET_TOKEN_USER_NOT_FOUND);
        }

        // Set password
        changePassword(account.getUsername(), password);

        // Update token
        passwordResetTokenEntity.setRedeemedOn(DateTime.now());
        passwordResetTokenEntity.setValid(false);

        logger.warn(String.format("Password for user [%s] has been reset.", account.getUsername()));
    }

    /**
     * Grants a role to a user.
     *
     * @param username the name of the user.
     * @param role the role to assign.
     */
    @Override
    public void grantRole(String username, EnumRole role) throws ApplicationException {
        throw createApplicationException(SharedErrorCode.NOT_IMPLEMENTED);
    }

    /**
     * Revokes a role from a user.
     *
     * @param username the name of the user.
     * @param role the role to assign.
     */
    @Override
    public void revokeRole(String username, EnumRole role) throws ApplicationException {
        throw createApplicationException(SharedErrorCode.NOT_IMPLEMENTED);
    }

    /**
     * Find a user by name.
     *
     * @param username the user name to search for.
     */
    @Override
    public AuthenticatedUser getUserByName(String username) throws ApplicationException {
        try {
            String accountQueryString = "select a from account a where a.username = :username";

            TypedQuery<AccountEntity> query = entityManager.createQuery(accountQueryString, AccountEntity.class)
                                                           .setFirstResult(0)
                                                           .setMaxResults(1);
            query.setParameter("username", username);

            List<AccountEntity> result = query.getResultList();
            if (!result.isEmpty()) {
                return accountEntityToUser(result.get(0));
            }

            return null;
        } catch (Exception ex) {
            throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
        }
    }

    /**
     * Find a user by key.
     *
     * @param key the user key to search for.
     */
    @Override
    public AuthenticatedUser getUserByKey(UUID key) throws ApplicationException {
        try {
            String accountQueryString = "select a from account a where a.key = :key";

            TypedQuery<AccountEntity> query = entityManager.createQuery(accountQueryString, AccountEntity.class)
                                                           .setFirstResult(0)
                                                           .setMaxResults(1);
            query.setParameter("key", key);

            List<AccountEntity> result = query.getResultList();
            if (!result.isEmpty()) {
                return accountEntityToUser(result.get(0));
            }
            return null;
        } catch (Exception ex) {
            throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
        }
    }

    /**
     * Converts an instance of {@link AccountEntity} to an instance of {@link AuthenticatedUser}.
     *
     * @param entity an instance of {@link AccountEntity}.
     * @return a new instance of {@link AuthenticatedUser}.
     */
    private AuthenticatedUser accountEntityToUser(AccountEntity entity) {
        List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        for (AccountRoleEntity r : entity.getRoles()) {
            authorities.add(new SimpleGrantedAuthority(r.getRole().getName()));
        }

        AuthenticatedUser user = new AuthenticatedUser(entity.getId(),
                                                       entity.getKey(),
                                                       entity.getUsername(),
                                                       entity.getPassword(),
                                                       entity.getUtility().getId(),
                                                       entity.getUtility().getKey(),
                                                       entity.isLocked(),
                                                       authorities);

        user.setCreatedOn(entity.getCreatedOn());
        user.setBirthdate(entity.getBirthdate());
        user.setCountry(entity.getCountry());
        user.setLocale(entity.getLocale());
        user.setFirstname(entity.getFirstname());
        user.setLastname(entity.getLastname());
        user.setGender(entity.getGender());
        user.setPostalCode(entity.getPostalCode());
        user.setTimezone(entity.getTimezone());
        user.setAllowPasswordReset(entity.isAllowPasswordReset());

        user.setWebMode(EnumWebMode.fromInteger(entity.getProfile().getWebMode()));
        user.setMobileMode(EnumMobileMode.fromInteger(entity.getProfile().getMobileMode()));
        user.setUtilityMode(EnumUtilityMode.fromInteger(entity.getProfile().getUtilityMode()));

        for(AccountUtilityEntity accountUtility : entity.getUtilities()) {
            user.getUtilities().add(accountUtility.getUtility().getId());
        }

        return user;
    }

    /**
     * Find a user by key for the given utility.
     *
     * @param utilityId the utility id.
     * @param key the user id.
     * @return the user.
     */
    @Override
    public AuthenticatedUser getUserByUtilityAndKey(int utilityId, UUID key) {
        String accountQueryString = "select a from account a where a.key = :key and a.utility.id = :utility_id";

        TypedQuery<AccountEntity> query = entityManager.createQuery(accountQueryString, AccountEntity.class)
                                                       .setFirstResult(0)
                                                       .setMaxResults(1);
        query.setParameter("utility_id", utilityId);
        query.setParameter("key", key);

        List<AccountEntity> result = query.getResultList();

        if (!result.isEmpty()) {
            return accountEntityToUser(result.get(0));

        }

        return null;
    }

    /**
     * Find a white list entry by user name.
     *
     * @param username the name of the user.
     */
    @Override
    public AccountWhiteListEntry getAccountWhiteListEntry(String username) {
        String entryQueryString = "select a from account_white_list a where a.username = :username";

        TypedQuery<AccountWhiteListEntity> entityQuery = entityManager.createQuery(entryQueryString, AccountWhiteListEntity.class)
                                                                      .setFirstResult(0)
                                                                      .setMaxResults(1);
        entityQuery.setParameter("username", username);

        List<AccountWhiteListEntity> entries = entityQuery.getResultList();

        if (entries.size() == 1) {
            AccountWhiteListEntity entry = entries.get(0);

            AccountWhiteListEntry result = new AccountWhiteListEntry();

            if (entry.getAccount() != null) {
                result.setAccountId(entry.getAccount().getId());
            }
            result.setAddress(entry.getAddress());
            result.setBirthdate(entry.getBirthdate());
            result.setCity(entry.getCity());
            result.setCountry(entry.getCountry());
            result.setDefaultMobileMode(entry.getDefaultMobileMode());
            result.setDefaultWebMode(entry.getDefaultWebMode());
            result.setFirstname(entry.getFirstname());
            result.setGender(entry.getGender());
            result.setId(entry.getId());
            result.setLastname(entry.getLastname());
            result.setLocale(entry.getLocale());
            result.setMeterLocation(entry.getMeterLocation());
            result.setMeterSerial(entry.getMeterSerial());
            result.setPostalCode(entry.getPostalCode());
            result.setRegisteredOn(entry.getRegisteredOn());
            result.setTimezone(entry.getTimezone());
            result.setUsername(entry.getUsername());
            result.setUtilityId(entry.getUtility().getId());

            return result;
        }

        return null;
    }

    /**
     * Updates login statistics for an account.
     *
     * @param id the account id.
     * @param success if the login operation was successful.
     */
    @Override
    public void updateLoginStats(int id, boolean success) {
        try {
            StoredProcedureQuery procedure = entityManager.createStoredProcedureQuery("sp_account_update_stats");

            procedure.registerStoredProcedureParameter(0, Integer.class, ParameterMode.IN);
            procedure.registerStoredProcedureParameter(1, Boolean.class, ParameterMode.IN);
            procedure.registerStoredProcedureParameter(2, Date.class, ParameterMode.IN);

            procedure.setParameter(0, id);
            procedure.setParameter(1, success);
            procedure.setParameter(2, new Date());

            procedure.execute();

        } catch (Exception ex) {
            logger.error(String.format("Failed to update login stats for user [%d]", id), ex);
        }
    }

    /**
     * Get account activity.
     *
     * @return a list with all the users.
     */
    @Override
    public List<AccountActivity> getAccountActivity() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AuthenticatedUser user = null;

        if (auth.getPrincipal() instanceof AuthenticatedUser) {
            user = (AuthenticatedUser) auth.getPrincipal();
        }

        if (user != null) {
            return this.getAccountActivity(user.getUtilities());
        }

        return new ArrayList<AccountActivity>();
    }

    /**
     * Get account activity for the users of the given utilities.
     *
     * @param utilities the ids of the utilities.
     * @return a list of the users for the given utility.
     */
    public List<AccountActivity> getAccountActivity(List<Integer> utilities) {
        String activityQueryString = "select a from trial_account_activity a where a.utilityId in :utilities order by a.username";

        TypedQuery<AccountActivityEntity> query = entityManager.createQuery(activityQueryString, AccountActivityEntity.class);

        query.setParameter("utilities", utilities);

        ArrayList<AccountActivity> results = new ArrayList<AccountActivity>();

        for (AccountActivityEntity a : query.getResultList()) {
            AccountActivity account = new AccountActivity();

            account.setId(a.getId());
            account.setKey(a.getKey());
            account.setUtilityId(a.getUtilityId());
            account.setAccountId(a.getAccountId());
            account.setAccountRegisteredOn(a.getAccountRegisteredOn() != null ? a.getAccountRegisteredOn().getMillis() : null);
            account.setUtilityName(a.getUtilityName());
            account.setUsername(a.getUsername());
            account.setFirstName(a.getFirstName());
            account.setLastName(a.getLastName());

            account.setNumberOfAmphiroDevices(a.getNumberOfAmphiroDevices());
            account.setNumberOfMeters(a.getNumberOfMeters());

            account.setLastDataUploadFailure((a.getLastDataUploadFailure() != null) ? a.getLastDataUploadFailure().getMillis() : null);
            account.setLastDataUploadSuccess(a.getLastDataUploadSuccess() != null ? a.getLastDataUploadSuccess().getMillis() : null);
            account.setLastLoginFailure(a.getLastLoginFailure() != null ? a.getLastLoginFailure().getMillis() : null);
            account.setLastLoginSuccess(a.getLastLoginSuccess() != null ? a.getLastLoginSuccess().getMillis() : null);
            account.setLeastAmphiroRegistration(a.getLeastAmphiroRegistration() != null ? a.getLeastAmphiroRegistration().getMillis() : null);
            account.setLeastMeterRegistration(a.getLeastMeterRegistration() != null ? a.getLeastMeterRegistration().getMillis() : null);

            account.setTransmissionCount(a.getTransmissionCount());
            account.setTransmissionIntervalMax(a.getTransmissionIntervalMax());
            account.setTransmissionIntervalSum(a.getTransmissionIntervalSum());

            results.add(account);
        }

        return results;
    }

    /**
     * Searches users by user name prefix.
     *
     * @param prefix the prefix used for filtering users.
     * @return a list of users.
     */
    @Override
    public List<UserInfo> filterUserByPrefix(String prefix) {
        List<UserInfo> accounts = new ArrayList<UserInfo>();

        String accountQueryString = "select a from account a " +
                                    "where (lower(a.firstname) like lower(:prefix) or lower(a.lastname) like lower(:prefix)) and " +
                                    "      (a.utility.id = :utility_id)";

        TypedQuery<AccountEntity> accountQuery = entityManager.createQuery(accountQueryString, AccountEntity.class)
                                                              .setMaxResults(100);

        accountQuery.setParameter("prefix", prefix + '%');
        accountQuery.setParameter("utility_id", getCurrentUtilityId());

        for (AccountEntity account : accountQuery.getResultList()) {
            accounts.add(new UserInfo(account));
        }

        return accounts;
    }

    /**
     * Search for users.
     *
     * @param query query for filtering users.
     * @return a list of accounts.
     */
    @Override
    public UserQueryResult search(UserQuery query) {
        // Prepare response
        UserQueryResult result = new UserQueryResult();

        Geometry geometry = query.getGeometry();
        if ((geometry != null) && (geometry.getSRID() == 0)) {
            geometry.setSRID(4326);
        }

        // Load data
        String command = "";

        // Resolve filters
        List<String> filters = new ArrayList<String>();

        filters.add("(a.utility.id = :utility_id)");

        if (!StringUtils.isBlank(query.getSerial())) {
            filters.add("(a.id in (select m.account.id from device_meter m where m.serial like :serial))");

            if (!StringUtils.isBlank(query.getText())) {
                filters.add("(a.lastname like :text or a.username like :text)");
            }
        } else if (!StringUtils.isBlank(query.getText())) {
            filters.add("(a.lastname like :text or a.username like :text)");
        }
        if (geometry != null) {
            filters.add("(contains(:geometry, a.location) = true)");
        }

        command = "select count(a.id) from account a ";

        // Count total number of records
        Integer totalUsers;

        if (!filters.isEmpty()) {
            command += "where " + StringUtils.join(filters, " and ");
        }

        TypedQuery<Number> countQuery = entityManager.createQuery(command, Number.class);

        if (!StringUtils.isBlank(query.getText())) {
            countQuery.setParameter("text", query.getText() + "%");
        }
        if (!StringUtils.isBlank(query.getSerial())) {
            countQuery.setParameter("serial", query.getSerial() + "%");
        }
        if (geometry != null) {
            countQuery.setParameter("geometry", geometry);
        }

        countQuery.setParameter("utility_id", getCurrentUtilityId());

        totalUsers = ((Number) countQuery.getSingleResult()).intValue();

        result.setTotal(totalUsers);

        // Load data
        command = "select a from account a ";

        if (!filters.isEmpty()) {
            command += " where " + StringUtils.join(filters, " and ");
        }

        if (!StringUtils.isBlank(query.getSerial())) {
            command += " order by a.lastname, a.firstname";
        } else {
            command += " order by a.lastname, a.firstname";
        }

        TypedQuery<AccountEntity> entityQuery = entityManager.createQuery(command,
                        AccountEntity.class);

        if (!StringUtils.isBlank(query.getText())) {
            entityQuery.setParameter("text", query.getText() + "%");
        }
        if (!StringUtils.isBlank(query.getSerial())) {
            entityQuery.setParameter("serial", query.getSerial() + "%");
        }
        if (geometry != null) {
            entityQuery.setParameter("geometry", geometry);
        }

        entityQuery.setParameter("utility_id", getCurrentUtilityId());

        entityQuery.setFirstResult(query.getIndex() * query.getSize());
        entityQuery.setMaxResults(query.getSize());

        result.setAccounts(entityQuery.getResultList());

        // Force device loading
        for (AccountEntity a : result.getAccounts()) {
            a.getDevices().size();
        }

        return result;

    }

    /**
     * Create a new account white list entry.
     *
     * @param userInfo the new account white list entry.
     */
    @Override
    public void insertAccountWhiteListEntry(AccountWhiteListInfo userInfo) {
        String whiteListQueryString = "select a from account_white_list a where a.username = :username";

        TypedQuery<AccountWhiteListEntity> whitelistQuery = entityManager.createQuery(whiteListQueryString, AccountWhiteListEntity.class)
                                                                         .setFirstResult(0)
                                                                         .setMaxResults(1);

        whitelistQuery.setParameter("username", userInfo.getEmail());
        List<AccountWhiteListEntity> whitelistEntries = whitelistQuery.getResultList();

        if (!whitelistEntries.isEmpty()) {
            throw createApplicationException(UserErrorCode.USERNAME_EXISTS_IN_WHITELIST).set("username", userInfo.getEmail());
        }

        AccountWhiteListEntity newEntry = new AccountWhiteListEntity(userInfo.getEmail());
        newEntry.setFirstname(userInfo.getFirstName());
        newEntry.setLastname(userInfo.getLastName());
        newEntry.setGender(EnumGender.fromString(userInfo.getGender()));

        // Get Utility
        String utilityQueryString = "select u from utility u where u.id = :id";

        TypedQuery<UtilityEntity> utilityQuery = entityManager.createQuery(utilityQueryString, UtilityEntity.class)
                                                              .setFirstResult(0)
                                                              .setMaxResults(1);

        utilityQuery.setParameter("id", userInfo.getUtilityId());
        List<UtilityEntity> utilityEntry = utilityQuery.getResultList();

        if (utilityEntry.isEmpty()) {
            throw createApplicationException(UserErrorCode.UTILITY_DOES_NOT_EXIST).set("id", userInfo.getUtilityId());
        }
        newEntry.setUtility(utilityEntry.get(0));
        newEntry.setCountry(utilityEntry.get(0).getCountry());
        newEntry.setCity(utilityEntry.get(0).getCity());
        newEntry.setTimezone(utilityEntry.get(0).getTimezone());
        newEntry.setLocale(utilityEntry.get(0).getLocale());

        newEntry.setAddress(userInfo.getAddress());
        newEntry.setPostalCode(userInfo.getPostalCode());
        newEntry.setDefaultMobileMode(EnumMobileMode.LEARNING.getValue());
        newEntry.setDefaultWebMode(EnumWebMode.INACTIVE.getValue());

        entityManager.persist(newEntry);
    }

    /**
     * Get the unique user keys for a given user group key.
     *
     * @param groupKey the user group key.
     * @return a list of user keys.
     */
    @Override
    public List<UUID> getUserKeysForGroup(UUID groupKey) {
        ArrayList<UUID> result = new ArrayList<UUID>();
        try {
            Query query = entityManager.createNativeQuery("select CAST(a.key as char varying) from \"group\" g "
                            + "inner join group_member gm on g.id = gm.group_id "
                            + "inner join account a on gm.account_id = a.id where g.key = CAST(? as uuid)");
            query.setParameter(1, groupKey.toString());

            List<?> keys = query.getResultList();
            for (Object key : keys) {
                result.add(UUID.fromString((String) key));
            }
        } catch (Exception ex) {
            logger.error(String.format("Failed to load user keys for group [%s].", groupKey), ex);
        }

        return result;
    }

    /**
     * Get the unique user keys for a given utility key.
     *
     * @param utilityKey the utility key.
     * @return a list of user keys.
     */
    @Override
    public List<UUID> getUserKeysForUtility(UUID utilityKey) {
        ArrayList<UUID> result = new ArrayList<UUID>();
        try {
            Query query = entityManager.createNativeQuery("select CAST(a.key as char varying) from utility u "
                            + "inner join account a on u.id = a.utility_id where u.key = CAST(? as uuid)");
            query.setParameter(1, utilityKey.toString());

            List<?> keys = query.getResultList();
            for (Object key : keys) {
                result.add(UUID.fromString((String) key));
            }
        } catch (Exception ex) {
            logger.error(String.format("Failed to load user keys for utility [%s]", utilityKey), ex);
        }

        return result;
    }

    /**
     * Get the unique user keys for a given utility id.
     *
     * @param utilityId the utility id.
     * @return a list of user keys.
     */
    @Override
    public List<UUID> getUserKeysForUtility(int utilityId) {
        ArrayList<UUID> result = new ArrayList<UUID>();

        Query query = entityManager.createNativeQuery("select CAST(a.key as char varying) from utility u "
                        + "inner join account a on u.id = a.utility_id where u.id = :utilityId");
        query.setParameter("utilityId", utilityId);

        List<?> keys = query.getResultList();
        for (Object key : keys) {
            result.add(UUID.fromString((String) key));
        }

        return result;
    }

    /**
     * Get the unique user keys for the default utility of the currently authenticated user.
     *
     * @return a list of user keys.
     */
    @Override
    public List<UUID> getUserKeysForUtility() {
        ArrayList<UUID> result = new ArrayList<UUID>();

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            AuthenticatedUser user = null;

            if (auth.getPrincipal() instanceof AuthenticatedUser) {
                user = (AuthenticatedUser) auth.getPrincipal();
            }

            if (user != null) {
                Query query = entityManager.createNativeQuery("select CAST(a.key as char varying) from account a where a.utility_id = :utility_id");
                query.setParameter("utility_id", user.getUtilityId());

                List<?> keys = query.getResultList();

                // Force device loading
                for (Object key : keys) {
                    result.add(UUID.fromString((String) key));
                }
            }
        } catch (Exception ex) {
            logger.error("Failed to load user keys for utility.", ex);
        }

        return result;
    }

    /**
     * Get user information for the given user key.
     *
     * @param key the user key.
     * @return the user information.
     */
    @Override
    public UserInfo getUserInfoByKey(UUID key) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            AuthenticatedUser user = (AuthenticatedUser) auth.getPrincipal();

            if (!user.hasRole(EnumRole.ROLE_UTILITY_ADMIN, EnumRole.ROLE_SYSTEM_ADMIN)) {
                throw createApplicationException(SharedErrorCode.AUTHORIZATION);
            }

            TypedQuery<AccountEntity> userQuery = entityManager.createQuery(
                            "SELECT a FROM account a WHERE a.key = :key",
                            AccountEntity.class).setFirstResult(0).setMaxResults(1);
            userQuery.setParameter("key", key);

            AccountEntity account = userQuery.getSingleResult();

            UserInfo userInfo = new UserInfo(account);

            for (AccountProfileHistoryEntity history : account.getProfile().getHistory()) {
                if (history.getVersion().equals(account.getProfile().getVersion())) {
                    UserInfo.ModeInfo modeInfo = new UserInfo.ModeInfo();

                    modeInfo.setUtilityMode(account.getProfile().getUtilityMode());
                    modeInfo.setHomeMode(account.getProfile().getWebMode());
                    modeInfo.setMobileMode(account.getProfile().getMobileMode());

                    modeInfo.setUpdatedOn(account.getProfile().getUpdatedOn().getMillis());
                    if (history.getEnabledOn() != null) {
                        modeInfo.setEnabledOn(history.getEnabledOn().getMillis());
                    }
                    if (history.getAcknowledgedOn() != null) {
                        modeInfo.setAcknowledgedOn(history.getAcknowledgedOn().getMillis());
                    }

                    userInfo.setMode(modeInfo);
                    break;
                }
            }

            TypedQuery<SurveyEntity> surveyQuery = entityManager.createQuery(
                            "SELECT s FROM survey s WHERE s.username = :username",
                            SurveyEntity.class).setFirstResult(0).setMaxResults(1);
            surveyQuery.setParameter("username", account.getUsername());

            List<SurveyEntity> surveys = surveyQuery.getResultList();

            if (!surveys.isEmpty()) {
                SurveyEntity survey = surveys.get(0);

                userInfo.setTabletOs(survey.getTabletOs());
                userInfo.setSmartPhoneOs(survey.getSmartPhoneOs());
            }

            return userInfo;
        } catch (NoResultException ex) {
            throw wrapApplicationException(ex, UserErrorCode.USER_KEY_NOT_FOUND).set("key", key.toString());
        } catch (Exception ex) {
            throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
        }
    }

    /**
     * Get an account by key.
     *
     * @param key the account key.
     * @return an {@link AccountEntity} entity.
     */
    @Override
    public AccountEntity getAccountByKey(UUID key) {
        String accountQueryString = "select a from account a where a.key = :key";

        TypedQuery<AccountEntity> query = entityManager.createQuery(accountQueryString, AccountEntity.class)
                                                       .setFirstResult(0)
                                                       .setMaxResults(1);
        query.setParameter("key", key);
        
        return query.getSingleResult();
    }

    /**
     * Get an account by user name.
     *
     * @param username the user name.
     * @return an {@link AccountEntity} entity.
     */
    @Override
    public AccountEntity getAccountByUsername(String username) {
        String accountQueryString = "select a from account a where a.username = :username";

        TypedQuery<AccountEntity> query = entityManager.createQuery(accountQueryString, AccountEntity.class)
                                                       .setFirstResult(0)
                                                       .setMaxResults(1);
        query.setParameter("username", username);
        
        return query.getSingleResult();
    }

    /**
     * Gets survey data for the given utility id.
     *
     * @param utilityId the utility id.
     * @return a list of {@link SurveyEntity} objects.
     */
    @Override
    public List<SurveyEntity> getSurveyDataByUtilityId(int utilityId) {
        String queryString = "SELECT s FROM survey s WHERE s.utility.id = :utilityId";

        TypedQuery<SurveyEntity> surveyQuery = entityManager.createQuery(queryString, SurveyEntity.class);

        surveyQuery.setParameter("utilityId", utilityId);

        return surveyQuery.getResultList();
    }

    /**
     * Get survey data for a single user.
     *
     * @param userKey the user key.
     * @return the user survey data.
     */
    @Override
    public SurveyEntity getSurveyByKey(UUID userKey) {
        String nativeSurveyQueryString = "select s.* from survey s inner join account a on a.username = s.username where a.key = :key";

        Query surveyQuery = entityManager.createNativeQuery(nativeSurveyQueryString, SurveyEntity.class)
                                         .setFirstResult(0)
                                         .setMaxResults(1);
        surveyQuery.setParameter("key", userKey);

        List<?> surveys = surveyQuery.getResultList();
        if (!surveys.isEmpty()) {
            return (SurveyEntity) surveys.get(0);
        }

        return null;
    }

}
