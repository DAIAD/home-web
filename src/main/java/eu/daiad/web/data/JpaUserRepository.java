package eu.daiad.web.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;

import eu.daiad.web.domain.AccountProfile;
import eu.daiad.web.domain.AccountRole;
import eu.daiad.web.domain.AccountWhiteListEntry;
import eu.daiad.web.domain.Role;
import eu.daiad.web.domain.Utility;
import eu.daiad.web.model.EnumValueDescription;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.error.SharedErrorCode;
import eu.daiad.web.model.error.UserErrorCode;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.model.security.EnumRole;
import eu.daiad.web.model.user.Account;

@Primary
@Repository
@Transactional()
@Scope("prototype")
public class JpaUserRepository implements IUserRepository {

	private static final Log logger = LogFactory.getLog(JpaUserRepository.class);

	@Value("${security.white-list}")
	private boolean enforceWhiteListCheck;

	@Autowired
	private ApplicationContext ctx;

	@Autowired
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
			throw ApplicationException.wrap(ex, UserErrorCode.ROLE_INITIALIZATION);
		}
	}

	private void initializeAdministrators() throws ApplicationException {
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

		try {
			TypedQuery<eu.daiad.web.domain.Utility> utilityQuery = entityManager.createQuery("select u from utility u",
							eu.daiad.web.domain.Utility.class);

			for (eu.daiad.web.domain.Utility utility : utilityQuery.getResultList()) {
				TypedQuery<eu.daiad.web.domain.Account> userQuery = entityManager.createQuery(
								"select a from account a where a.username = :username",
								eu.daiad.web.domain.Account.class);
				userQuery.setParameter("username", utility.getDefaultAdministratorUsername());

				List<eu.daiad.web.domain.Account> users = userQuery.getResultList();

				if (users.size() == 0) {
					String password = UUID.randomUUID().toString();

					eu.daiad.web.domain.Account account = new eu.daiad.web.domain.Account();
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
					assignedRole.setAssignedOn(new DateTime());
					assignedRole.setAssignedBy(account);

					account.getRoles().add(assignedRole);

					this.entityManager.persist(account);

					logger.info(String
									.format("Default administrator has been crearted for utility [%s]. User name : %s. Password : %s",
													utility.getName(), utility.getDefaultAdministratorUsername(),
													password));
				}
			}
		} catch (Exception ex) {
			throw ApplicationException.wrap(ex, UserErrorCode.ADMIN_INITIALIZATION);
		}
	}

	@Override
	@Transactional
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

	private boolean isUsernameReserved(String username) {
		TypedQuery<eu.daiad.web.domain.Utility> userQuery = entityManager.createQuery(
						"select u from utility u where u.defaultAdministratorUsername = :username",
						eu.daiad.web.domain.Utility.class);

		userQuery.setParameter("username", username);

		return (userQuery.getResultList().size() != 0);
	}

	@Override
	public UUID createUser(Account user) throws ApplicationException {
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

		try {
			if (this.isUsernameReserved(user.getUsername())) {
				throw new ApplicationException(UserErrorCode.USERNANE_RESERVED).set("username", user.getUsername());
			}
			if (this.getUserByName(user.getUsername()) != null) {
				throw new ApplicationException(UserErrorCode.USERNANE_NOT_AVAILABLE)
								.set("username", user.getUsername());
			}

			AccountWhiteListEntry entry = null;

			if (enforceWhiteListCheck) {
				TypedQuery<eu.daiad.web.domain.AccountWhiteListEntry> query = entityManager
								.createQuery("select a from account_white_list a where a.username = :username",
												eu.daiad.web.domain.AccountWhiteListEntry.class).setFirstResult(0)
								.setMaxResults(1);
				query.setParameter("username", user.getUsername());

				List<eu.daiad.web.domain.AccountWhiteListEntry> result = query.getResultList();
				if (result.size() == 0) {
					throw new ApplicationException(UserErrorCode.WHITELIST_MISMATCH)
									.set("username", user.getUsername());
				} else {
					entry = result.get(0);
				}
			}

			Utility utility = null;

			if (entry != null) {
				TypedQuery<eu.daiad.web.domain.Utility> query = entityManager.createQuery(
								"select u from utility u where u.id = :id", eu.daiad.web.domain.Utility.class);
				query.setParameter("id", entry.getUtility().getId());

				utility = query.getSingleResult();
			} else {
				TypedQuery<eu.daiad.web.domain.Utility> query = entityManager.createQuery(
								"select u from utility u where u.name = :name", eu.daiad.web.domain.Utility.class);
				query.setParameter("name", "DAIAD");

				utility = query.getSingleResult();
			}

			eu.daiad.web.domain.Account account = new eu.daiad.web.domain.Account();
			account.setUsername(user.getUsername());
			account.setPassword(encoder.encode(user.getPassword()));

			account.setEmail(user.getUsername());

			account.setFirstname(user.getFirstname());
			account.setLastname(user.getLastname());
			account.setBirthdate(user.getBirthdate());
			account.setGender(user.getGender());

			account.setLocale(user.getLocale());
			account.setCountry(user.getCountry());
			account.setTimezone(user.getTimezone());
			account.setPostalCode(user.getPostalCode());

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
			assignedRole.setAssignedOn(new DateTime());

			account.getRoles().add(assignedRole);

			this.entityManager.persist(account);
			this.entityManager.flush();
			
			AccountProfile profile = new AccountProfile();
			profile.setMobileEnabled(true);
			profile.setWebEnabled(true);
			profile.setUtilityEnabled(false);
			
			profile.setAccount(account);

			this.entityManager.persist(profile);

			if (entry != null) {
				entry.setRegisteredOn(DateTime.now());
				entry.setAccount(account);
			}

			return account.getKey();
		} catch (Exception ex) {
			throw ApplicationException.wrap(ex, SharedErrorCode.UNKNOWN);
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

			TypedQuery<eu.daiad.web.domain.Account> query = entityManager
							.createQuery("select a from account a where a.username = :username",
											eu.daiad.web.domain.Account.class).setFirstResult(0).setMaxResults(1);
			query.setParameter("username", username);

			List<eu.daiad.web.domain.Account> result = query.getResultList();
			if (result.size() != 0) {
				eu.daiad.web.domain.Account account = result.get(0);

				List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
				for (AccountRole r : account.getRoles()) {
					authorities.add(new SimpleGrantedAuthority(r.getRole().getName()));
				}
				user = new AuthenticatedUser(account.getKey(), account.getUsername(), account.getPassword(),
								authorities);

				user.setBirthdate(account.getBirthdate());
				user.setCountry(account.getCountry());
				user.setFirstname(account.getFirstname());
				user.setLastname(account.getLastname());
				user.setGender(account.getGender());
				user.setPostalCode(account.getPostalCode());
				user.setTimezone(account.getTimezone());
			}

			return user;
		} catch (Exception ex) {
			throw ApplicationException.wrap(ex, SharedErrorCode.UNKNOWN);
		}
	}

}
