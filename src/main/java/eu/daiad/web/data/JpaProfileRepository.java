package eu.daiad.web.data;

import java.util.ArrayList;
import java.util.Iterator;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Repository;

import eu.daiad.web.domain.Account;
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
@Scope("prototype")
public class JpaProfileRepository implements IProfileRepository {

	@Autowired
	EntityManager entityManager;

	@Autowired
	private IUserRepository userRepository;

	@Autowired
	private IDeviceRepository deviceRepository;

	@Override
	public Profile getProfileByUsername(EnumApplication application, String username) throws ApplicationException {
		try {
			TypedQuery<eu.daiad.web.domain.Account> userQuery = entityManager
							.createQuery("select a from account a where a.username = :username",
											eu.daiad.web.domain.Account.class).setFirstResult(0).setMaxResults(1);
			userQuery.setParameter("username", username);

			Account account = userQuery.getSingleResult();

			ArrayList<Device> devices = this.deviceRepository.getUserDevices(account.getKey(),
							new DeviceRegistrationQuery());

			Profile profile = new Profile();

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
				profile.setEnabled(account.getProfile().isWebEnabled());
				profile.setConfiguration(account.getProfile().getWebConfiguration());
				break;
			case MOBILE:
				profile.setEnabled(account.getProfile().isMobileEnabled());
				profile.setConfiguration(account.getProfile().getMobileConfiguration());
				break;
			default:
				profile.setEnabled(false);
				profile.setConfiguration(null);
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
				throw new ApplicationException(ProfileErrorCode.PROFILE_NOT_SUPPORTED).set("application", application);
			}
		} catch (Exception ex) {
			throw ApplicationException.wrap(ex, SharedErrorCode.UNKNOWN);
		}
	}

}