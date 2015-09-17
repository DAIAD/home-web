package eu.daiad.web.data;

import java.util.ArrayList;
import java.util.Iterator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

import eu.daiad.web.model.Device;
import eu.daiad.web.model.DeviceRegistration;
import eu.daiad.web.model.Profile;
import eu.daiad.web.security.model.ApplicationUser;

@Repository()
@Scope("prototype")
public class ProfileRepository {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private DeviceRepository deviceRepository;

	public Profile getProfileByUsername(String username) throws Exception {
		ApplicationUser user = this.userRepository.getUserByName(username);

		ArrayList<Device> devices = this.deviceRepository.getUserDevices(user
				.getKey());
		
		Profile profile = new Profile();
		
		profile.setKey(user.getKey());
		profile.setFirstname(user.getFirstname());
		profile.setLastname(user.getLastname());
		profile.setTimezone(user.getTimezone());
		profile.setCountry(user.getCountry());
		profile.setPostalCode(user.getPostalCode());
		
		ArrayList<DeviceRegistration> registrations = new ArrayList<DeviceRegistration>();
		for(Iterator<Device> d = devices.iterator(); d.hasNext(); ) {
		    registrations.add(d.next().toDeviceRegistration());
		}
		profile.setDevices(registrations);
		
		return profile;
	}
}