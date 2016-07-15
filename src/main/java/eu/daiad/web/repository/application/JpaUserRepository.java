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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import eu.daiad.web.domain.application.AccountActivity;
import eu.daiad.web.domain.application.AccountProfile;
import eu.daiad.web.domain.application.AccountProfileHistoryEntry;
import eu.daiad.web.domain.application.AccountRole;
import eu.daiad.web.domain.application.AccountWhiteListEntry;
import eu.daiad.web.domain.application.Role;
import eu.daiad.web.domain.application.Survey;
import eu.daiad.web.domain.application.Utility;
import eu.daiad.web.model.EnumGender;
import eu.daiad.web.model.EnumValueDescription;
import eu.daiad.web.model.admin.AccountWhiteListInfo;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.error.SharedErrorCode;
import eu.daiad.web.model.error.UserErrorCode;
import eu.daiad.web.model.profile.EnumMobileMode;
import eu.daiad.web.model.profile.EnumUtilityMode;
import eu.daiad.web.model.profile.EnumWebMode;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.model.security.EnumRole;
import eu.daiad.web.model.user.Account;
import eu.daiad.web.model.user.UserInfo;
import eu.daiad.web.model.user.UserQuery;
import eu.daiad.web.model.user.UserQueryResult;
import eu.daiad.web.repository.BaseRepository;

@Repository
@Transactional("applicationTransactionManager")
public class JpaUserRepository extends BaseRepository implements IUserRepository {

    private static final Log logger = LogFactory.getLog(JpaUserRepository.class);

    @Value("${security.white-list}")
    private boolean enforceWhiteListCheck;

    @PersistenceContext(unitName = "default")
    EntityManager entityManager;

    private void initializeRoles() throws ApplicationException {
        try {
            for (EnumRole r : EnumRole.class.getEnumConstants()) {
                TypedQuery<Role> roleQuery = entityManager.createQuery("select r from role r where r.name = :name",
                                Role.class);
                roleQuery.setParameter("name", r.toString());

                List<Role> roles = roleQuery.getResultList();
                if (roles.size() == 0) {
                    Role role = new Role();

                    String description = EnumRole.class.getField(r.name()).getAnnotation(EnumValueDescription.class)
                                    .value();
                    role.setName(r.name());
                    role.setDescription(description);

                    this.entityManager.persist(role);
                }
            }
        } catch (Exception ex) {
            throw wrapApplicationException(ex, UserErrorCode.ROLE_INITIALIZATION);
        }
    }

    private void initializeAdministrators() throws ApplicationException {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        try {
            TypedQuery<eu.daiad.web.domain.application.Utility> utilityQuery = entityManager.createQuery(
                            "select u from utility u", eu.daiad.web.domain.application.Utility.class);

            for (eu.daiad.web.domain.application.Utility utility : utilityQuery.getResultList()) {
                TypedQuery<eu.daiad.web.domain.application.Account> userQuery = entityManager.createQuery(
                                "select a from account a where a.username = :username",
                                eu.daiad.web.domain.application.Account.class);
                userQuery.setParameter("username", utility.getDefaultAdministratorUsername());

                List<eu.daiad.web.domain.application.Account> users = userQuery.getResultList();

                if (users.size() == 0) {
                    String password = UUID.randomUUID().toString();

                    eu.daiad.web.domain.application.Account account = new eu.daiad.web.domain.application.Account();
                    account.setUsername(utility.getDefaultAdministratorUsername());
                    account.setPassword(encoder.encode(password));
                    account.setLocked(false);
                    account.setChangePasswordOnNextLogin(false);
                    account.setUtility(utility);
                    account.setLocale(Locale.ENGLISH.getLanguage());

                    TypedQuery<Role> roleQuery = entityManager.createQuery("select r from role r where r.name = :name",
                                    Role.class);
                    roleQuery.setParameter("name", EnumRole.ROLE_ADMIN.name());

                    Role role = roleQuery.getSingleResult();

                    AccountRole assignedRole = new AccountRole();
                    assignedRole.setRole(role);
                    assignedRole.setAssignedOn(account.getCreatedOn());
                    assignedRole.setAssignedBy(account);

                    account.getRoles().add(assignedRole);

                    this.entityManager.persist(account);
                    this.entityManager.flush();

                    AccountProfile profile = new AccountProfile();
                    profile.setMobileMode(EnumMobileMode.INACTIVE.getValue());
                    profile.setWebMode(EnumWebMode.INACTIVE.getValue());
                    profile.setUtilityMode(EnumUtilityMode.ACTIVE.getValue());
                    profile.setUpdatedOn(account.getCreatedOn());

                    profile.setAccount(account);
                    this.entityManager.persist(profile);

                    AccountProfileHistoryEntry entry = new AccountProfileHistoryEntry();
                    entry.setVersion(profile.getVersion());
                    entry.setUpdatedOn(account.getCreatedOn());
                    entry.setMobileMode(profile.getMobileMode());
                    entry.setWebMode(profile.getWebMode());
                    entry.setUtilityMode(profile.getUtilityMode());

                    entry.setProfile(profile);
                    this.entityManager.persist(entry);

                    logger.info(String
                                    .format("Default administrator has been crearted for utility [%s]. User name : %s. Password : %s",
                                                    utility.getName(), utility.getDefaultAdministratorUsername(),
                                                    password));
                }
            }
        } catch (Exception ex) {
            throw wrapApplicationException(ex, UserErrorCode.ADMIN_INITIALIZATION);
        }
    }

    @Override
    @Transactional(transactionManager = "transactionManager", propagation = Propagation.REQUIRES_NEW)
    public void initializeSecurityConfiguration() {
        try {
            // Initialize all system roles
            initializeRoles();

            // Create an administrator for any registered utility
            initializeAdministrators();
        } catch (ApplicationException ex) {
            logger.error("Database initialization has failed.");
        }
    }

    private boolean isUsernameReserved(String username) {
        TypedQuery<eu.daiad.web.domain.application.Utility> userQuery = entityManager.createQuery(
                        "select u from utility u where u.defaultAdministratorUsername = :username",
                        eu.daiad.web.domain.application.Utility.class);

        userQuery.setParameter("username", username);

        return (userQuery.getResultList().size() != 0);
    }

    @Override
    public UUID createUser(Account user) throws ApplicationException {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        try {
            if (this.isUsernameReserved(user.getUsername())) {
                throw createApplicationException(UserErrorCode.USERNANE_RESERVED).set("username", user.getUsername());
            }
            if (this.getUserByName(user.getUsername()) != null) {
                throw createApplicationException(UserErrorCode.USERNANE_NOT_AVAILABLE).set("username",
                                user.getUsername());
            }

            AccountWhiteListEntry whiteListEntry = null;

            if (enforceWhiteListCheck) {
                TypedQuery<eu.daiad.web.domain.application.AccountWhiteListEntry> query = entityManager.createQuery(
                                "select a from account_white_list a where a.username = :username",
                                eu.daiad.web.domain.application.AccountWhiteListEntry.class).setFirstResult(0)
                                .setMaxResults(1);
                query.setParameter("username", user.getUsername());

                List<eu.daiad.web.domain.application.AccountWhiteListEntry> result = query.getResultList();
                if (result.size() == 0) {
                    throw createApplicationException(UserErrorCode.WHITELIST_MISMATCH).set("username",
                                    user.getUsername());
                } else {
                    whiteListEntry = result.get(0);
                }
            }

            Utility utility = null;

            if (whiteListEntry != null) {
                TypedQuery<eu.daiad.web.domain.application.Utility> query = entityManager.createQuery(
                                "select u from utility u where u.id = :id",
                                eu.daiad.web.domain.application.Utility.class);
                query.setParameter("id", whiteListEntry.getUtility().getId());

                utility = query.getSingleResult();
            } else {
                TypedQuery<eu.daiad.web.domain.application.Utility> query = entityManager.createQuery(
                                "select u from utility u where u.name = :name",
                                eu.daiad.web.domain.application.Utility.class);
                query.setParameter("name", "DAIAD");

                utility = query.getSingleResult();
            }

            eu.daiad.web.domain.application.Account account = new eu.daiad.web.domain.application.Account();
            account.setUsername(user.getUsername());
            account.setPassword(encoder.encode(user.getPassword()));

            account.setEmail(user.getUsername());

            if (whiteListEntry != null) {
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
            }

            account.setLocked(false);
            account.setChangePasswordOnNextLogin(false);

            account.setUtility(utility);

            Role role = null;
            TypedQuery<Role> roleQuery = entityManager.createQuery("select r from role r where r.name = :name",
                            Role.class);
            roleQuery.setParameter("name", EnumRole.ROLE_USER.toString());

            role = roleQuery.getSingleResult();

            AccountRole assignedRole = new AccountRole();
            assignedRole.setRole(role);
            assignedRole.setAssignedOn(account.getCreatedOn());

            account.getRoles().add(assignedRole);

            this.entityManager.persist(account);
            this.entityManager.flush();

            AccountProfile profile = new AccountProfile();
            if (whiteListEntry != null) {
                profile.setMobileMode(whiteListEntry.getDefaultMobileMode());
                profile.setWebMode(whiteListEntry.getDefaultWebMode());
            } else {
                profile.setMobileMode(EnumMobileMode.ACTIVE.getValue());
                profile.setWebMode(EnumWebMode.ACTIVE.getValue());
            }
            profile.setUtilityMode(EnumUtilityMode.INACTIVE.getValue());
            profile.setUpdatedOn(account.getCreatedOn());

            profile.setAccount(account);
            this.entityManager.persist(profile);

            AccountProfileHistoryEntry profileHistoryEntry = new AccountProfileHistoryEntry();
            profileHistoryEntry.setVersion(profile.getVersion());
            profileHistoryEntry.setUpdatedOn(account.getCreatedOn());
            profileHistoryEntry.setMobileMode(profile.getMobileMode());
            profileHistoryEntry.setWebMode(profile.getWebMode());
            profileHistoryEntry.setUtilityMode(profile.getUtilityMode());

            profileHistoryEntry.setProfile(profile);
            this.entityManager.persist(profileHistoryEntry);

            if (whiteListEntry != null) {
                whiteListEntry.setRegisteredOn(DateTime.now());
                whiteListEntry.setAccount(account);
            }

            this.entityManager.flush();

            return account.getKey();
        } catch (Exception ex) {
            throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
        }
    }

    @Override
    public void setPassword(String username, String password) throws ApplicationException {
        // TODO Auto-generated method stub

    }

    @Override
    public void setRole(String username, EnumRole role, boolean set) throws ApplicationException {
        // TODO Auto-generated method stub

    }

    @Override
    public AuthenticatedUser getUserByName(String username) throws ApplicationException {
        try {
            AuthenticatedUser user = null;

            TypedQuery<eu.daiad.web.domain.application.Account> query = entityManager.createQuery(
                            "select a from account a where a.username = :username",
                            eu.daiad.web.domain.application.Account.class).setFirstResult(0).setMaxResults(1);
            query.setParameter("username", username);

            List<eu.daiad.web.domain.application.Account> result = query.getResultList();
            if (result.size() != 0) {
                eu.daiad.web.domain.application.Account account = result.get(0);

                List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
                for (AccountRole r : account.getRoles()) {
                    authorities.add(new SimpleGrantedAuthority(r.getRole().getName()));
                }
                user = new AuthenticatedUser(account.getId(), account.getKey(), account.getUsername(), account
                                .getPassword(), account.getUtility().getId(), account.getUtility().getKey(), account
                                .isLocked(), authorities);

                user.setCreatedOn(account.getCreatedOn());
                user.setBirthdate(account.getBirthdate());
                user.setCountry(account.getCountry());
                user.setLocale(account.getLocale());
                user.setFirstname(account.getFirstname());
                user.setLastname(account.getLastname());
                user.setGender(account.getGender());
                user.setPostalCode(account.getPostalCode());
                user.setTimezone(account.getTimezone());

                user.setWebMode(EnumWebMode.fromInteger(account.getProfile().getWebMode()));
                user.setMobileMode(EnumMobileMode.fromInteger(account.getProfile().getMobileMode()));
                user.setUtilityMode(EnumUtilityMode.fromInteger(account.getProfile().getUtilityMode()));
            }

            return user;
        } catch (Exception ex) {
            throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
        }
    }

    @Override
    public AuthenticatedUser getUserByKey(UUID key) throws ApplicationException {
        try {
            AuthenticatedUser user = null;

            TypedQuery<eu.daiad.web.domain.application.Account> query = entityManager
                            .createQuery("select a from account a where a.key = :key",
                                            eu.daiad.web.domain.application.Account.class).setFirstResult(0)
                            .setMaxResults(1);
            query.setParameter("key", key);

            List<eu.daiad.web.domain.application.Account> result = query.getResultList();
            if (result.size() != 0) {
                eu.daiad.web.domain.application.Account account = result.get(0);

                List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
                for (AccountRole r : account.getRoles()) {
                    authorities.add(new SimpleGrantedAuthority(r.getRole().getName()));
                }

                user = new AuthenticatedUser(account.getId(), account.getKey(), account.getUsername(), account
                                .getPassword(), account.getUtility().getId(), account.getUtility().getKey(), account
                                .isLocked(), authorities);

                user.setCreatedOn(account.getCreatedOn());
                user.setBirthdate(account.getBirthdate());
                user.setCountry(account.getCountry());
                user.setLocale(account.getLocale());
                user.setFirstname(account.getFirstname());
                user.setLastname(account.getLastname());
                user.setGender(account.getGender());
                user.setPostalCode(account.getPostalCode());
                user.setTimezone(account.getTimezone());

                user.setWebMode(EnumWebMode.fromInteger(account.getProfile().getWebMode()));
                user.setMobileMode(EnumMobileMode.fromInteger(account.getProfile().getMobileMode()));
                user.setUtilityMode(EnumUtilityMode.fromInteger(account.getProfile().getUtilityMode()));
            }

            return user;
        } catch (Exception ex) {
            throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
        }
    }

    @Override
    public AuthenticatedUser getUserByUtilityAndKey(int utilityId, UUID key) throws ApplicationException {
        try {
            AuthenticatedUser user = null;

            TypedQuery<eu.daiad.web.domain.application.Account> query = entityManager.createQuery(
                            "select a from account a where a.key = :key and a.utility.id = :utility_id",
                            eu.daiad.web.domain.application.Account.class).setFirstResult(0).setMaxResults(1);
            query.setParameter("utility_id", utilityId);
            query.setParameter("key", key);

            List<eu.daiad.web.domain.application.Account> result = query.getResultList();
            if (result.size() != 0) {
                eu.daiad.web.domain.application.Account account = result.get(0);

                List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
                for (AccountRole r : account.getRoles()) {
                    authorities.add(new SimpleGrantedAuthority(r.getRole().getName()));
                }
                user = new AuthenticatedUser(account.getId(), account.getKey(), account.getUsername(), account
                                .getPassword(), account.getUtility().getId(), account.getUtility().getKey(), account
                                .isLocked(), authorities);

                user.setCreatedOn(account.getCreatedOn());
                user.setBirthdate(account.getBirthdate());
                user.setCountry(account.getCountry());
                user.setLocale(account.getLocale());
                user.setFirstname(account.getFirstname());
                user.setLastname(account.getLastname());
                user.setGender(account.getGender());
                user.setPostalCode(account.getPostalCode());
                user.setTimezone(account.getTimezone());

                user.setWebMode(EnumWebMode.fromInteger(account.getProfile().getWebMode()));
                user.setMobileMode(EnumMobileMode.fromInteger(account.getProfile().getMobileMode()));
                user.setUtilityMode(EnumUtilityMode.fromInteger(account.getProfile().getUtilityMode()));
            }

            return user;
        } catch (Exception ex) {
            throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
        }
    }

    @Override
    public eu.daiad.web.model.admin.AccountWhiteListEntry getAccountWhiteListEntry(String username) {
        TypedQuery<AccountWhiteListEntry> entityQuery = entityManager.createQuery(
                        "select a from account_white_list a where a.username = :username", AccountWhiteListEntry.class)
                        .setFirstResult(0).setMaxResults(1);
        entityQuery.setParameter("username", username);

        List<AccountWhiteListEntry> entries = entityQuery.getResultList();

        if (entries.size() == 1) {
            AccountWhiteListEntry entry = entries.get(0);

            eu.daiad.web.model.admin.AccountWhiteListEntry result = new eu.daiad.web.model.admin.AccountWhiteListEntry();

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

    @Override
    public List<eu.daiad.web.model.admin.AccountActivity> getAccountActivity() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AuthenticatedUser user = null;

        if (auth.getPrincipal() instanceof AuthenticatedUser) {
            user = (AuthenticatedUser) auth.getPrincipal();
        }

        if (user != null) {
            return this.getAccountActivity(user.getUtilityId());
        }

        return new ArrayList<eu.daiad.web.model.admin.AccountActivity>();
    }

    @Override
    public List<eu.daiad.web.model.admin.AccountActivity> getAccountActivity(int utilityId) {
        try {
            TypedQuery<AccountActivity> query = entityManager
                            .createQuery("select a from trial_account_activity a where a.utilityId = :utility_id order by a.username",
                                            AccountActivity.class);

            query.setParameter("utility_id", utilityId);

            ArrayList<eu.daiad.web.model.admin.AccountActivity> results = new ArrayList<eu.daiad.web.model.admin.AccountActivity>();

            for (AccountActivity a : query.getResultList()) {
                eu.daiad.web.model.admin.AccountActivity account = new eu.daiad.web.model.admin.AccountActivity();

                account.setId(a.getId());
                account.setKey(a.getKey());
                account.setUtilityId(a.getUtilityId());
                account.setAccountId(a.getAccountId());
                account.setAccountRegisteredOn(a.getAccountRegisteredOn() != null ? a.getAccountRegisteredOn()
                                .getMillis() : null);
                account.setUtilityName(a.getUtilityName());
                account.setUsername(a.getUsername());
                account.setFirstName(a.getFirstName());
                account.setLastName(a.getLastName());

                account.setNumberOfAmphiroDevices(a.getNumberOfAmphiroDevices());
                account.setNumberOfMeters(a.getNumberOfMeters());

                account.setLastDataUploadFailure((a.getLastDataUploadFailure() != null) ? a.getLastDataUploadFailure()
                                .getMillis() : null);
                account.setLastDataUploadSuccess(a.getLastDataUploadSuccess() != null ? a.getLastDataUploadSuccess()
                                .getMillis() : null);
                account.setLastLoginFailure(a.getLastLoginFailure() != null ? a.getLastLoginFailure().getMillis()
                                : null);
                account.setLastLoginSuccess(a.getLastLoginSuccess() != null ? a.getLastLoginSuccess().getMillis()
                                : null);

                account.setLeastAmphiroRegistration(a.getLeastAmphiroRegistration() != null ? a
                                .getLeastAmphiroRegistration().getMillis() : null);
                account.setLeastMeterRegistration(a.getLeastMeterRegistration() != null ? a.getLeastMeterRegistration()
                                .getMillis() : null);

                account.setTransmissionCount(a.getTransmissionCount());
                account.setTransmissionIntervalMax(a.getTransmissionIntervalMax());
                account.setTransmissionIntervalSum(a.getTransmissionIntervalSum());

                results.add(account);
            }

            return results;
        } catch (Exception ex) {
            logger.error(String.format("Failed to load account activity for utility [%d]", utilityId), ex);
        }

        return null;
    }

    @Override
    public List<UserInfo> filterUserByPrefix(String prefix) {
        List<UserInfo> accounts = new ArrayList<UserInfo>();

        TypedQuery<eu.daiad.web.domain.application.Account> accountQuery = entityManager.createQuery(
                        "select a from account a where (lower(a.firstname) like lower(:prefix) or lower(a.lastname) like lower(:prefix)) "
                                        + "and (a.utility.id = :utility_id)",
                        eu.daiad.web.domain.application.Account.class).setMaxResults(100);

        accountQuery.setParameter("prefix", prefix + '%');
        accountQuery.setParameter("utility_id", getCurrentUtilityId());

        for (eu.daiad.web.domain.application.Account account : accountQuery.getResultList()) {
            accounts.add(new UserInfo(account));
        }

        return accounts;
    }

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

        TypedQuery<eu.daiad.web.domain.application.Account> entityQuery = entityManager.createQuery(command,
                        eu.daiad.web.domain.application.Account.class);

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
        for (eu.daiad.web.domain.application.Account a : result.getAccounts()) {
            a.getDevices().size();
        }

        return result;

    }

    @Override
    public void insertAccountWhiteListEntry(AccountWhiteListInfo userInfo) {
        try {

            TypedQuery<eu.daiad.web.domain.application.AccountWhiteListEntry> whitelistQuery = entityManager
                            .createQuery("select a from account_white_list a where a.username = :username",
                                            eu.daiad.web.domain.application.AccountWhiteListEntry.class)
                            .setFirstResult(0).setMaxResults(1);
            whitelistQuery.setParameter("username", userInfo.getEmail());
            List<AccountWhiteListEntry> whitelistEntries = whitelistQuery.getResultList();

            if (!whitelistEntries.isEmpty()) {
                throw createApplicationException(UserErrorCode.USERNAME_EXISTS_IN_WHITELIST).set("username",
                                userInfo.getEmail());
            }

            AccountWhiteListEntry newEntry = new AccountWhiteListEntry(userInfo.getEmail());
            newEntry.setFirstname(userInfo.getFirstName());
            newEntry.setLastname(userInfo.getLastName());
            newEntry.setGender(EnumGender.fromString(userInfo.getGender()));

            // Get Utility
            TypedQuery<eu.daiad.web.domain.application.Utility> utilityQuery = entityManager.createQuery(
                            "select u from utility u where u.id = :id", eu.daiad.web.domain.application.Utility.class)
                            .setFirstResult(0).setMaxResults(1);
            utilityQuery.setParameter("id", userInfo.getUtilityId());
            List<Utility> utilityEntry = utilityQuery.getResultList();

            if (utilityEntry.isEmpty()) {
                throw createApplicationException(UserErrorCode.UTILITY_DOES_NOT_EXIST).set("id",
                                userInfo.getUtilityId());
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

            this.entityManager.persist(newEntry);

        } catch (Exception ex) {
            throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
        }
    }

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

    @Override
    public List<UUID> getUserKeysForUtility(int utilityId) {
        ArrayList<UUID> result = new ArrayList<UUID>();
        try {
            Query query = entityManager.createNativeQuery("select CAST(a.key as char varying) from utility u "
                            + "inner join account a on u.id = a.utility_id where u.id = :utilityId");
            query.setParameter("utilityId", utilityId);

            List<?> keys = query.getResultList();
            for (Object key : keys) {
                result.add(UUID.fromString((String) key));
            }
        } catch (Exception ex) {
            logger.error(String.format("Failed to load user keys for utility [%d]", utilityId), ex);
        }

        return result;
    }

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
                Query query = entityManager
                                .createNativeQuery("select CAST(a.key as char varying) from account a where a.utility_id = :utility_id");
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

    @Override
    public UserInfo getUserInfoByKey(UUID user_id) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            AuthenticatedUser user = (AuthenticatedUser) auth.getPrincipal();

            if (!user.hasRole("ROLE_ADMIN") && !user.hasRole("ROLE_SUPERUSER")) {
                throw createApplicationException(SharedErrorCode.AUTHORIZATION);
            }

            TypedQuery<eu.daiad.web.domain.application.Account> userQuery = entityManager.createQuery(
                            "SELECT a FROM account a WHERE a.key = :user_id",
                            eu.daiad.web.domain.application.Account.class).setFirstResult(0).setMaxResults(1);
            userQuery.setParameter("user_id", user_id);

            eu.daiad.web.domain.application.Account account = userQuery.getSingleResult();

            UserInfo userInfo = new UserInfo(account);

            for(AccountProfileHistoryEntry history : account.getProfile().getHistory()) {
                if(history.getVersion().equals(account.getProfile().getVersion())) {
                    UserInfo.ModeInfo modeInfo = new UserInfo.ModeInfo();
                    
                    modeInfo.setUtilityMode(account.getProfile().getUtilityMode());
                    modeInfo.setHomeMode(account.getProfile().getWebMode());
                    modeInfo.setMobileMode(account.getProfile().getMobileMode());
                    
                    modeInfo.setUpdatedOn(account.getProfile().getUpdatedOn().getMillis());
                    if(history.getEnabledOn() != null) {
                        modeInfo.setEnabledOn(history.getEnabledOn().getMillis());
                    }
                    if(history.getAcknowledgedOn() != null) {
                        modeInfo.setAcknowledgedOn(history.getAcknowledgedOn().getMillis());
                    }
                    
                    userInfo.setMode(modeInfo);
                    break;
                }
            }
            
            TypedQuery<eu.daiad.web.domain.application.Survey> surveyQuery = entityManager.createQuery(
                            "SELECT s FROM survey s WHERE s.username = :username",
                            eu.daiad.web.domain.application.Survey.class).setFirstResult(0).setMaxResults(1);
            surveyQuery.setParameter("username", account.getUsername());

            List<eu.daiad.web.domain.application.Survey> surveys = surveyQuery.getResultList();

            if (!surveys.isEmpty()) {
                Survey survey = surveys.get(0);

                userInfo.setTabletOs(survey.getTabletOs());
                userInfo.setSmartPhoneOs(survey.getSmartPhoneOs());
            }

            return userInfo;
        } catch (NoResultException ex) {
            throw wrapApplicationException(ex, UserErrorCode.USERID_NOT_FOUND).set("accountId", user_id);
        } catch (Exception ex) {
            throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
        }
    }

}
