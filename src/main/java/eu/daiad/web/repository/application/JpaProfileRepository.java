package eu.daiad.web.repository.application;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.daiad.web.domain.application.Account;
import eu.daiad.web.domain.application.AccountProfileHistoryEntry;
import eu.daiad.web.model.EnumApplication;
import eu.daiad.web.model.device.Device;
import eu.daiad.web.model.device.DeviceRegistration;
import eu.daiad.web.model.device.DeviceRegistrationQuery;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.error.ProfileErrorCode;
import eu.daiad.web.model.error.SharedErrorCode;
import eu.daiad.web.model.profile.Profile;
import eu.daiad.web.model.security.AuthenticatedUser;

@Repository()
@Transactional()
public class JpaProfileRepository implements IProfileRepository {

	@PersistenceContext(unitName="default")
	EntityManager entityManager;

	@Autowired
	private IUserRepository userRepository;

	@Autowired
	private IDeviceRepository deviceRepository;

	@Override
	public Profile getProfileByUsername(EnumApplication application) throws ApplicationException {
		try {
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();

			AuthenticatedUser user = (AuthenticatedUser) auth.getPrincipal();

			switch (application) {
				case HOME:
				case MOBILE:
					if (!user.hasRole("ROLE_USER")) {
						throw new ApplicationException(ProfileErrorCode.PROFILE_NOT_SUPPORTED).set("application",
										application);
					}
					break;
				case UTILITY:
					if (!user.hasRole("ROLE_ADMIN")) {
						throw new ApplicationException(ProfileErrorCode.PROFILE_NOT_SUPPORTED).set("application",
										application);
					}
					break;
				default:
					throw new ApplicationException(ProfileErrorCode.PROFILE_NOT_SUPPORTED).set("application",
									application);
			}

			TypedQuery<eu.daiad.web.domain.application.Account> userQuery = entityManager
							.createQuery("select a from account a where a.username = :username",
											eu.daiad.web.domain.application.Account.class).setFirstResult(0)
							.setMaxResults(1);
			userQuery.setParameter("username", user.getUsername());

			Account account = userQuery.getSingleResult();

			ArrayList<Device> devices = this.deviceRepository.getUserDevices(account.getKey(),
							new DeviceRegistrationQuery());

			Profile profile = new Profile();

			profile.setVersion(account.getProfile().getVersion());
			profile.setKey(account.getKey());
			profile.setUsername(account.getUsername());
			profile.setFirstname(account.getFirstname());
			profile.setLastname(account.getLastname());
			profile.setTimezone(account.getTimezone());
			profile.setCountry(account.getCountry());

			ArrayList<DeviceRegistration> registrations = new ArrayList<DeviceRegistration>();
			for (Iterator<Device> d = devices.iterator(); d.hasNext();) {
				registrations.add(d.next().toDeviceRegistration());
			}
			profile.setDevices(registrations);

			switch (application) {
				case HOME:
					profile.setMode(account.getProfile().getWebMode());
					profile.setConfiguration(account.getProfile().getWebConfiguration());
					break;
				case MOBILE:
					profile.setMode(account.getProfile().getMobileMode());
					profile.setConfiguration(account.getProfile().getMobileConfiguration());
					break;
				case UTILITY:
					profile.setMode(account.getProfile().getUtilityMode());
					profile.setConfiguration(account.getProfile().getUtilityConfiguration());
					break;
				default:
					throw new ApplicationException(ProfileErrorCode.PROFILE_NOT_SUPPORTED).set("application",
									application);
			}

			return profile;
		} catch (Exception ex) {
			throw ApplicationException.wrap(ex, SharedErrorCode.UNKNOWN);
		}
	}

	@Override
	public void setProfileConfiguration(EnumApplication application, String value) throws ApplicationException {
		try {
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();

			TypedQuery<Account> query = entityManager
							.createQuery("select a from account a where a.key = :key", Account.class).setFirstResult(0)
							.setMaxResults(1);
			query.setParameter("key", ((AuthenticatedUser) auth.getPrincipal()).getKey());

			Account account = query.getSingleResult();

			switch (application) {
				case HOME:
					account.getProfile().setWebConfiguration(value);
					break;
				case MOBILE:
					account.getProfile().setMobileConfiguration(value);
					break;
				default:
					throw new ApplicationException(ProfileErrorCode.PROFILE_NOT_SUPPORTED).set("application",
									application);
			}
		} catch (Exception ex) {
			throw ApplicationException.wrap(ex, SharedErrorCode.UNKNOWN);
		}
	}

	@Override
	public void notifyProfile(EnumApplication application, UUID version, DateTime updatedOn) {
		try {
			boolean found = false;

			Authentication auth = SecurityContextHolder.getContext().getAuthentication();

			TypedQuery<Account> query = entityManager
							.createQuery("select a from account a where a.key = :key", Account.class).setFirstResult(0)
							.setMaxResults(1);
			query.setParameter("key", ((AuthenticatedUser) auth.getPrincipal()).getKey());

			Account account = query.getSingleResult();

			switch (application) {
				case HOME:
					throw new ApplicationException(ProfileErrorCode.PROFILE_NOT_SUPPORTED).set("application",
									application);
				case MOBILE:
					for (AccountProfileHistoryEntry h : account.getProfile().getHistory()) {
						if (h.getVersion().equals(version)) {
							found = true;

							h.setEnabledOn(updatedOn);
							h.setAcknowledgedOn(new DateTime());
							break;
						}
					}
					break;
				default:
					throw new ApplicationException(ProfileErrorCode.PROFILE_NOT_SUPPORTED).set("application",
									application);
			}
			if (!found) {
				throw new ApplicationException(ProfileErrorCode.PROFILE_VERSION_NOT_FOUND).set("version", version);
			}
		} catch (Exception ex) {
			throw ApplicationException.wrap(ex, SharedErrorCode.UNKNOWN);
		}
	}

}