package eu.daiad.web.data;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.Resource;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;

import eu.daiad.web.domain.AccountRole;
import eu.daiad.web.domain.Role;
import eu.daiad.web.domain.Utility;
import eu.daiad.web.model.ApplicationUser;
import eu.daiad.web.model.EnumRole;
import eu.daiad.web.model.user.Account;

@Primary
@Repository
@Transactional
@Scope("prototype")
public class JpaUserRepository implements IUserRepository {

	private static final Log logger = LogFactory
			.getLog(JpaUserRepository.class);

	@Autowired
	private ApplicationContext ctx;

	@Autowired
	EntityManager entityManager;

	private final String defaultAdminUsername = "Administrator";

	private final EnumRole[] defaultAdminRoles = { EnumRole.ROLE_USER,
			EnumRole.ROLE_ADMIN };

	@Override
	@Transactional
	public void createDefaultUser() {
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		String password = UUID.randomUUID().toString();

		try {
			TypedQuery<eu.daiad.web.domain.Account> query = entityManager
					.createQuery(
							"select a from account a where a.username = :username",
							eu.daiad.web.domain.Account.class)
					.setFirstResult(0).setMaxResults(1);
			query.setParameter("username", this.defaultAdminUsername);

			List<eu.daiad.web.domain.Account> result = query.getResultList();
			if (result.size() == 0) {
				eu.daiad.web.domain.Utility utility = new eu.daiad.web.domain.Utility();
				utility.setName("DAIAD");
				utility.setDescription("Default DAIAD Utility");

				Resource resource = ctx
						.getResource("classpath:public/assets/images/daiad-transparent.png");
				InputStream stream = resource.getInputStream();
				utility.setLogo(IOUtils.toByteArray(stream));
				stream.close();

				eu.daiad.web.domain.Account account = new eu.daiad.web.domain.Account();
				account.setUsername(this.defaultAdminUsername);
				account.setPassword(encoder.encode(password));
				account.setLocked(false);
				account.setChangePasswordOnNextLogin(false);
				account.setUtility(utility);

				for (EnumRole r : this.defaultAdminRoles) {
					Role userRole = null;

					TypedQuery<Role> roleQuery = entityManager.createQuery(
							"select r from role r where r.name = :name",
							Role.class);
					roleQuery.setParameter("name", r.toString());

					List<Role> roles = roleQuery.getResultList();
					if (roles.size() == 0) {
						userRole = new Role();
						userRole.setName(r.toString());
						switch (r) {
						case ROLE_NONE:
							break;
						case ROLE_USER:
							userRole.setDescription("Allows access to DAIAD@Home");
							break;
						case ROLE_ADMIN:
							userRole.setDescription("Allows access to DAIAD@Utility");
							break;
						}
						;
					} else {
						userRole = roles.get(0);
					}

					AccountRole assignedRole = new AccountRole();
					assignedRole.setRole(userRole);
					assignedRole.setAssignedOn(new DateTime());
					assignedRole.setAssignedBy(account);

					account.getRoles().add(assignedRole);
				}
				this.entityManager.persist(account);

				logger.warn(String
						.format("Default administrator user has been crearted. User name : %s. Password : %s",
								this.defaultAdminUsername, password));
			}
		} catch (Exception ex) {
			logger.error(ex);

			throw new RuntimeException(ex);
		}
	}

	@Override
	public UUID createUser(Account user) throws Exception {
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

		try {
			if (user.getUsername().equals(this.defaultAdminUsername)) {
				throw new Exception("Username is reserved.");
			}
			if (this.getUserByName(user.getUsername()) != null) {
				throw new Exception("User already exists.");
			}

			TypedQuery<eu.daiad.web.domain.Utility> query = entityManager
					.createQuery(
							"select u from utility u where u.name = :name",
							eu.daiad.web.domain.Utility.class);
			query.setParameter("name", "DAIAD");

			Utility utility = query.getSingleResult();

			eu.daiad.web.domain.Account account = new eu.daiad.web.domain.Account();
			account.setUsername(user.getUsername());
			account.setPassword(encoder.encode(user.getPassword()));

			account.setFirstname(user.getFirstname());
			account.setLastname(user.getLastname());
			account.setBirthdate(user.getBirthdate());
			account.setGender(user.getGender());

			account.setCountry(user.getCountry());
			account.setTimezone(user.getTimezone());
			account.setPostalCode(user.getPostalCode());

			account.setLocked(false);
			account.setChangePasswordOnNextLogin(false);

			account.setUtility(utility);

			Role role = null;
			TypedQuery<Role> roleQuery = entityManager.createQuery(
					"select r from role r where r.name = :name", Role.class);
			roleQuery.setParameter("name", EnumRole.ROLE_USER.toString());

			role = roleQuery.getSingleResult();

			AccountRole assignedRole = new AccountRole();
			assignedRole.setRole(role);
			assignedRole.setAssignedOn(new DateTime());

			account.getRoles().add(assignedRole);

			this.entityManager.persist(account);

			return account.getKey();
		} catch (Exception ex) {
			logger.error(ex);

			throw new RuntimeException(ex);
		}
	}

	@Override
	public void setPassword(String username, String password) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void setRole(String username, EnumRole role, boolean set)
			throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public ApplicationUser getUserByName(String username) throws Exception {
		ApplicationUser user = null;

		TypedQuery<eu.daiad.web.domain.Account> query = entityManager
				.createQuery(
						"select a from account a where a.username = :username",
						eu.daiad.web.domain.Account.class).setFirstResult(0)
				.setMaxResults(1);
		query.setParameter("username", username);

		List<eu.daiad.web.domain.Account> result = query.getResultList();
		if (result.size() != 0) {
			eu.daiad.web.domain.Account account = result.get(0);

			List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
			for (AccountRole r : account.getRoles()) {
				authorities.add(new SimpleGrantedAuthority(r.getRole()
						.getName()));
			}
			user = new ApplicationUser(account.getKey(), account.getUsername(),
					account.getPassword(), authorities);

			user.setBirthdate(account.getBirthdate());
			user.setCountry(account.getCountry());
			user.setFirstname(account.getFirstname());
			user.setLastname(account.getLastname());
			user.setGender(account.getGender());
			user.setPostalCode(account.getPostalCode());
			user.setTimezone(account.getTimezone());
		}

		return user;
	}

}
