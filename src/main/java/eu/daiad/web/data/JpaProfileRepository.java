package eu.daiad.web.data;

import java.util.ArrayList;
import java.util.Iterator;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

import eu.daiad.web.model.ApplicationUser;
import eu.daiad.web.model.device.Device;
import eu.daiad.web.model.device.DeviceRegistration;
import eu.daiad.web.model.device.DeviceRegistrationQuery;
import eu.daiad.web.model.profile.Profile;

@Repository()
@Transactional()
@Scope("prototype")
public class JpaProfileRepository implements IProfileRepository {

	@Autowired
	private IUserRepository userRepository;

	@Autowired
	private IDeviceRepository deviceRepository;

	@Override
	public Profile getProfileByUsername(String username) throws Exception {
		ApplicationUser user = this.userRepository.getUserByName(username);

		ArrayList<Device> devices = this.deviceRepository.getUserDevices(
				user.getKey(), new DeviceRegistrationQuery());

		Profile profile = new Profile();

		profile.setKey(user.getKey());
		profile.setUsername(user.getUsername());
		profile.setFirstname(user.getFirstname());
		profile.setLastname(user.getLastname());
		profile.setTimezone(user.getTimezone());
		profile.setCountry(user.getCountry());

		ArrayList<DeviceRegistration> registrations = new ArrayList<DeviceRegistration>();
		for (Iterator<Device> d = devices.iterator(); d.hasNext();) {
			registrations.add(d.next().toDeviceRegistration());
		}
		profile.setDevices(registrations);

		return profile;
	}
}