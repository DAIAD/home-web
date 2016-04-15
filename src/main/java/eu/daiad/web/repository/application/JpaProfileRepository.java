package eu.daiad.web.repository.application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
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
import eu.daiad.web.domain.application.DeviceAmphiro;
import eu.daiad.web.domain.application.DeviceAmphiroConfiguration;
import eu.daiad.web.domain.application.DeviceAmphiroConfigurationDefault;
import eu.daiad.web.domain.application.Utility;
import eu.daiad.web.model.EnumApplication;
import eu.daiad.web.model.device.Device;
import eu.daiad.web.model.device.DeviceConfigurationCollection;
import eu.daiad.web.model.device.DeviceRegistration;
import eu.daiad.web.model.device.DeviceRegistrationQuery;
import eu.daiad.web.model.device.EnumDeviceType;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.error.DeviceErrorCode;
import eu.daiad.web.model.error.ProfileErrorCode;
import eu.daiad.web.model.error.SharedErrorCode;
import eu.daiad.web.model.profile.EnumMobileMode;
import eu.daiad.web.model.profile.Profile;
import eu.daiad.web.model.profile.ProfileDeactivateRequest;
import eu.daiad.web.model.profile.ProfileModeChange;
import eu.daiad.web.model.profile.ProfileModes;
import eu.daiad.web.model.profile.ProfileModesChanges;
import eu.daiad.web.model.profile.ProfileModesFilterOptions;
import eu.daiad.web.model.profile.ProfileModesRequest;
import eu.daiad.web.model.profile.ProfileModesSubmitChangesRequest;
import eu.daiad.web.model.security.AuthenticatedUser;

@Repository()
@Transactional("transactionManager")
public class JpaProfileRepository implements IProfileRepository {

	@PersistenceContext(unitName="default")
	EntityManager entityManager;

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
	public List <ProfileModes> getProfileModes(ProfileModesRequest filters) throws ApplicationException {
		try {
						
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();

			AuthenticatedUser user = (AuthenticatedUser) auth.getPrincipal();

			if (!user.hasRole("ROLE_ADMIN")) {
					throw new ApplicationException(SharedErrorCode.AUTHORIZATION);
			}			
						
			TypedQuery<eu.daiad.web.domain.application.Account> userQuery = null;
		
			String queryFilterPart = "";
			if (filters.getNameFilter() != null && filters.getNameFilter().length() > 0){
				queryFilterPart = " AND (LOWER(a.firstname) LIKE :searchTerm "
										+ "OR LOWER(a.lastname) LIKE :searchTerm "
										+ "OR LOWER(a.username) LIKE :searchTerm "
										+ "ORDER BY a.lastname, a.username)";
			} 
			
			String queryString = "select a from account a join a.roles r "
										+ "where r.role.name = :userRole" 
										+ queryFilterPart 
										+ " ORDER BY a.lastname, a.username";
			userQuery = entityManager.createQuery(queryString, eu.daiad.web.domain.application.Account.class).setFirstResult(0);
			userQuery.setParameter("userRole", "ROLE_USER");
			
			if (filters.getNameFilter() != null && filters.getNameFilter().length() > 0){
				userQuery.setParameter("searchTerm", "%" + filters.getNameFilter().toLowerCase() + "%");
			}
			List <Account> accounts = userQuery.getResultList();
			List <ProfileModes> profileModesList = new ArrayList<ProfileModes>();
			
			// List all DeviceAmphiroConfigurationDefault's in a simple HashMap 
			List<DeviceAmphiroConfigurationDefault> defaultConfigurations = this.deviceRepository.getAmphiroDefaultConfigurations();
			HashMap <Integer, String> simplifiedDefaultConfs = new HashMap <Integer, String>();
			for (DeviceAmphiroConfigurationDefault defaultConfiguration : defaultConfigurations){
				simplifiedDefaultConfs.put(defaultConfiguration.getId(), defaultConfiguration.getTitle());
			}
			
			for(Account account : accounts){
				ProfileModes profileModes = new ProfileModes();

				// User Id
				profileModes.setId(account.getKey());
				
				// User name
				if (account.getFirstname() != null && account.getLastname() != null){
					profileModes.setName(account.getFirstname() + " " + account.getLastname());
				} else {
					profileModes.setName(account.getUsername());
				}
				
				// Utility name & Id
				Utility utility = account.getUtility();
				profileModes.setGroupId(utility.getId());
				profileModes.setGroupName(utility.getName());
				// Applying Utility filter
				if (filters.getGroupName()!= null && !filters.getGroupName().equals(profileModes.getGroupName())){
					continue;
				}

				// Active flag
				if(account.getProfile().getMobileMode() == EnumMobileMode.BLOCK.getValue()){
					profileModes.setActive(false);
				} else {
					profileModes.setActive(true);
				}

				// Amphiro b1 flag
				ArrayList<Device> devices = this.deviceRepository.getUserDevices(account.getKey(),
						new DeviceRegistrationQuery());
				List <UUID> deviceKeyList = new ArrayList <UUID> ();
				for (Device device : devices){
					if(device.getType() == EnumDeviceType.AMPHIRO){
						deviceKeyList.add(device.getKey());
					}
				}
				
				if (deviceKeyList.size() > 0){
					UUID[] deviceKeys = deviceKeyList.toArray(new UUID[0]);
					ArrayList<DeviceConfigurationCollection> deviceConfigurations = this.deviceRepository.getConfiguration(account.getKey(), deviceKeys);
					
					// Get only the conf of the first amphiro device, since all amphiro devices ought to have the same conf.
					if (deviceConfigurations.get(0).getConfigurations().get(0).getTitle()
							.equals(simplifiedDefaultConfs.get(DeviceAmphiroConfigurationDefault.CONFIG_ENABLED_METRIC)) 
							||
							deviceConfigurations.get(0).getConfigurations().get(0).getTitle()
							.equals(simplifiedDefaultConfs.get(DeviceAmphiroConfigurationDefault.CONFIG_ENABLED_IMPERIAL))){
						profileModes.setAmphiro(ProfileModes.AmphiroModeState.ON);
						
					} else {
						profileModes.setAmphiro(ProfileModes.AmphiroModeState.OFF);
					}
					
				} else {
					profileModes.setAmphiro(ProfileModes.AmphiroModeState.NOT_APPLICABLE);
				}
				// Applying Amphiro filter
				if (filters.getAmphiro() != null && !filters.getAmphiro().equals(profileModes.getAmphiro().toString())){
					continue;
				}
				
				// Mobile Flag
				if(account.getProfile().getMobileMode() == EnumMobileMode.ACTIVE.getValue()){
						profileModes.setMobile(ProfileModes.MobileModeState.ON);
				} else {
						profileModes.setMobile(ProfileModes.MobileModeState.OFF);
				}
				// Applying Mobile filter
				if (filters.getMobile() != null){
					if (!filters.getMobile().equals(profileModes.getMobile().toString()))
						continue;
				}
				
				// Social Flag
				profileModes.setSocial(ProfileModes.SocialModeState.OFF);
				
				// Applying Social filter
				if (filters.getSocial() != null){
					if(!filters.getSocial().equals(profileModes.getSocial().toString())){
						continue;
					}
				}
				profileModesList.add(profileModes);
			}
									
			return profileModesList;
		} catch (Exception ex) {
			throw ApplicationException.wrap(ex, SharedErrorCode.UNKNOWN);
		}
	}
	
	@Override
	public void setProfileModes(ProfileModesSubmitChangesRequest modeChangesObject) throws ApplicationException {
		try {
			
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			AuthenticatedUser user = (AuthenticatedUser) auth.getPrincipal();

			if (!user.hasRole("ROLE_ADMIN")) {
					throw new ApplicationException(SharedErrorCode.AUTHORIZATION);
			}
		
			List <DeviceAmphiroConfigurationDefault> configurations = this.deviceRepository.getAmphiroDefaultConfigurations();
			
			// Organizing amphiro default configurations in a handy HashMap
			HashMap <String, DeviceAmphiroConfigurationDefault> defaultconfigurations = new HashMap <String, DeviceAmphiroConfigurationDefault>();
			for (DeviceAmphiroConfigurationDefault defconf : configurations){
				if(defconf.getTitle().equals("Enabled Configuration (Metric Units)")){
					defaultconfigurations.put("ON_Metric", defconf);
				} else if (defconf.getTitle().equals("Enabled Configuration (Imperial Units)")){
					defaultconfigurations.put("ON_Imperial", defconf);
				} else {
					defaultconfigurations.put("OFF", defconf);
				}
			}
			
			for (ProfileModesChanges modeChanges : modeChangesObject.getModeChanges()){
				
				TypedQuery<eu.daiad.web.domain.application.Account> accountQuery = entityManager
						.createQuery("select a from account a where a.key = :key",
										eu.daiad.web.domain.application.Account.class).setFirstResult(0)
						.setMaxResults(1);
				accountQuery.setParameter("key", modeChanges.getId());
				
				Account account = accountQuery.getSingleResult();
				 
				for (ProfileModeChange modeChange : modeChanges.getChanges()){
					switch (modeChange.getMode()){
					case "amphiro":						
						
						// Selecting the suitable default configuration
						DeviceAmphiroConfigurationDefault newDefaultConfiguration = null;
						if(modeChange.getValue().equals("OFF")){
							newDefaultConfiguration = defaultconfigurations.get("OFF");
						} else if (modeChange.getValue().equals("ON")){
							if(account.getUtility().getName().equals("St Albans")){
								newDefaultConfiguration = defaultconfigurations.get("ON_Imperial");
							} else if (account.getUtility().getName().equals("Alicante")){
								newDefaultConfiguration = defaultconfigurations.get("ON_Metric");
							} else {
								if (account.getLocale().equals("en")){
									newDefaultConfiguration = defaultconfigurations.get("ON_Imperial");
								} else {
									newDefaultConfiguration = defaultconfigurations.get("ON_Metric");
								}
							}
						} else {
							newDefaultConfiguration = defaultconfigurations.get("OFF");
						}
						
						DeviceAmphiroConfiguration newConfiguration = new DeviceAmphiroConfiguration();
						
						newConfiguration.setActive(true);
						newConfiguration.setCreatedOn(new DateTime());
						newConfiguration.setTitle(newDefaultConfiguration.getTitle());
						newConfiguration.setBlock(newDefaultConfiguration.getBlock());
						newConfiguration.setValue1(newDefaultConfiguration.getValue1());
						newConfiguration.setValue2(newDefaultConfiguration.getValue2());
						newConfiguration.setValue3(newDefaultConfiguration.getValue3());
						newConfiguration.setValue4(newDefaultConfiguration.getValue4());
						newConfiguration.setValue5(newDefaultConfiguration.getValue5());
						newConfiguration.setValue6(newDefaultConfiguration.getValue6());
						newConfiguration.setValue7(newDefaultConfiguration.getValue7());
						newConfiguration.setValue8(newDefaultConfiguration.getValue8());
						newConfiguration.setValue9(newDefaultConfiguration.getValue9());
						newConfiguration.setValue10(newDefaultConfiguration.getValue10());
						newConfiguration.setValue11(newDefaultConfiguration.getValue11());
						newConfiguration.setValue12(newDefaultConfiguration.getValue12());
						newConfiguration.setNumberOfFrames(newDefaultConfiguration.getNumberOfFrames());
						newConfiguration.setFrameDuration(newDefaultConfiguration.getFrameDuration());
						
						//Get the current (active and inactive) configurations and set them as inactive
						Set<eu.daiad.web.domain.application.Device> devices = account.getDevices();
						for (eu.daiad.web.domain.application.Device device : devices){
							DeviceAmphiro deviceAmphiro = (DeviceAmphiro) device;
							for (DeviceAmphiroConfiguration currentConfiguration : deviceAmphiro.getConfigurations()){
								currentConfiguration.setActive(false);
							}
							
							deviceAmphiro.getConfigurations().add(newConfiguration);
						}
						
						break;
					case "mobile":
						UUID newVersion = UUID.randomUUID();
						
						account.getProfile().setVersion(newVersion);
						
						int newMode = EnumMobileMode.UNDEFINED.getValue();
						if (modeChange.getValue().equals("ON")){
							newMode = EnumMobileMode.ACTIVE.getValue();
						} else if (modeChange.getValue().equals("OFF")){
							newMode = EnumMobileMode.INACTIVE.getValue();
						}
						account.getProfile().setMobileMode(newMode);
						DateTime now = new DateTime();
						account.getProfile().setUpdatedOn(now);
						
						AccountProfileHistoryEntry historyEntry = new AccountProfileHistoryEntry();
						historyEntry.setProfile(account.getProfile());
						historyEntry.setUpdatedOn(now);
						historyEntry.setVersion(newVersion);
						historyEntry.setMobileMode(newMode);
						historyEntry.setUtilityMode(account.getProfile().getUtilityMode());
						historyEntry.setWebMode(account.getProfile().getWebMode());
						
						this.entityManager.persist(account);
						this.entityManager.persist(historyEntry);
						
						break;
					case "social":
						break;
					}
				}
			}			
			
		} catch (Exception ex) {
			throw ApplicationException.wrap(ex, SharedErrorCode.UNKNOWN);
		}
	}
	
	@Override
	public void deactivateProfile (ProfileDeactivateRequest userDeactId){
		try {
			
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();

			AuthenticatedUser user = (AuthenticatedUser) auth.getPrincipal();

			if (!user.hasRole("ROLE_ADMIN")) {
					throw new ApplicationException(SharedErrorCode.AUTHORIZATION);
			}
			
			TypedQuery<eu.daiad.web.domain.application.Account> accountQuery = entityManager
					.createQuery("select a from account a where a.key = :key",
									eu.daiad.web.domain.application.Account.class).setFirstResult(0)
					.setMaxResults(1);
			accountQuery.setParameter("key", userDeactId.getUserDeactId());
			
			Account account = accountQuery.getSingleResult();
			UUID newVersion = UUID.randomUUID();			
			
			account.getProfile().setVersion(newVersion);
			
			int newMode = EnumMobileMode.BLOCK.getValue();
			account.getProfile().setMobileMode(newMode);
			
			DateTime now = new DateTime();
			account.getProfile().setUpdatedOn(now);
			
			AccountProfileHistoryEntry historyEntry = new AccountProfileHistoryEntry();
			historyEntry.setProfile(account.getProfile());
			historyEntry.setUpdatedOn(now);
			historyEntry.setVersion(newVersion);
			historyEntry.setMobileMode(newMode);
			historyEntry.setUtilityMode(account.getProfile().getUtilityMode());
			historyEntry.setWebMode(account.getProfile().getWebMode());
			
			// Deactivate with amphiro devices
			List <DeviceAmphiroConfigurationDefault> configurations = this.deviceRepository.getAmphiroDefaultConfigurations();
			
			DeviceAmphiroConfigurationDefault offConfiguration = null;
			for (DeviceAmphiroConfigurationDefault defconf : configurations){
				if(defconf.getTitle().equals("Off Configuration")) {
					offConfiguration = defconf;
				}
			}
			
			if (offConfiguration == null){
				throw new ApplicationException(DeviceErrorCode.OFF_AMPHIRO_CONFIGURATION_NOT_FOUND);
			}
			
			// Selecting the suitable default configuration
			
			DeviceAmphiroConfiguration newConfiguration = new DeviceAmphiroConfiguration();
			
			newConfiguration.setActive(true);
			newConfiguration.setCreatedOn(new DateTime());
			newConfiguration.setTitle(offConfiguration.getTitle());
			newConfiguration.setBlock(offConfiguration.getBlock());
			newConfiguration.setValue1(offConfiguration.getValue1());
			newConfiguration.setValue2(offConfiguration.getValue2());
			newConfiguration.setValue3(offConfiguration.getValue3());
			newConfiguration.setValue4(offConfiguration.getValue4());
			newConfiguration.setValue5(offConfiguration.getValue5());
			newConfiguration.setValue6(offConfiguration.getValue6());
			newConfiguration.setValue7(offConfiguration.getValue7());
			newConfiguration.setValue8(offConfiguration.getValue8());
			newConfiguration.setValue9(offConfiguration.getValue9());
			newConfiguration.setValue10(offConfiguration.getValue10());
			newConfiguration.setValue11(offConfiguration.getValue11());
			newConfiguration.setValue12(offConfiguration.getValue12());
			newConfiguration.setNumberOfFrames(offConfiguration.getNumberOfFrames());
			newConfiguration.setFrameDuration(offConfiguration.getFrameDuration());
			
			//Get the current (active and inactive) configurations and set them as inactive
			Set<eu.daiad.web.domain.application.Device> devices = account.getDevices();
			for (eu.daiad.web.domain.application.Device device : devices){
				DeviceAmphiro deviceAmphiro = (DeviceAmphiro) device;
				for (DeviceAmphiroConfiguration currentConfiguration : deviceAmphiro.getConfigurations()){
					currentConfiguration.setActive(false);
				}
				
				deviceAmphiro.getConfigurations().add(newConfiguration);
			}
						
			this.entityManager.persist(account);
			this.entityManager.persist(historyEntry);
			
		} catch (Exception ex) {
			throw ApplicationException.wrap(ex, SharedErrorCode.UNKNOWN);
		}
	}
	
	@Override
	public ProfileModesFilterOptions getFilterOptions()  throws ApplicationException {
		try {
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			AuthenticatedUser user = (AuthenticatedUser) auth.getPrincipal();
			if (!user.hasRole("ROLE_ADMIN")) {
					throw new ApplicationException(ProfileErrorCode.PROFILE_NOT_SUPPORTED);
			}
			
			ProfileModesFilterOptions profileModesFilterOptions = new ProfileModesFilterOptions();
			
			
			// Get distinct utility names for simple user accounts
			TypedQuery<String> utilityNameQuery = entityManager.createQuery(
					"SELECT DISTINCT (u.name) FROM utility u INNER JOIN u.accounts a "
										+ "INNER JOIN a.roles r WHERE r.role.name = :userRole",
					String.class).setFirstResult(0);
			utilityNameQuery.setParameter("userRole", "ROLE_USER");
			
			List <String> utilityOptions = utilityNameQuery.getResultList();
			profileModesFilterOptions.setGroupName(utilityOptions);
			
			
			// Get distinct amphiro names for simple user accounts
			profileModesFilterOptions.setAmphiro(new ArrayList <String>());
			TypedQuery<String> amphiroQuery = entityManager.createQuery(
					"SELECT DISTINCT(dc.title) FROM device_amphiro_config dc",
					String.class).setFirstResult(0);
			
			List <String> deviceConfigOptions = amphiroQuery.getResultList();
			
			for (String dc : deviceConfigOptions){
				if (dc.equals("Default Configuration")){
					profileModesFilterOptions.getAmphiro().add("OFF");
				} else if (dc.startsWith("Enabled Configuration")){
					profileModesFilterOptions.getAmphiro().add("ON");
				}
				if (profileModesFilterOptions.getAmphiro().size() > 1){
					break;
				}
			}
			// check if there is at least a user without registered amphiro devices
			// TODO Refactor query
			TypedQuery<eu.daiad.web.domain.application.Account> userQuery = entityManager.createQuery(
					"SELECT a FROM account a JOIN a.roles r WHERE r.role.name = :userRole",
					eu.daiad.web.domain.application.Account.class).setFirstResult(0);
			userQuery.setParameter("userRole", "ROLE_USER");
			
			List <Account> userAccounts = userQuery.getResultList();
			for (Account a : userAccounts){
				int amphiroCount = 0;
				for (eu.daiad.web.domain.application.Device d : a.getDevices()){
					if (d.getType() == EnumDeviceType.AMPHIRO){
						amphiroCount++;
					}
				}
				if (amphiroCount == 0){
					profileModesFilterOptions.getAmphiro().add("NOT_APPLICABLE");
					break;
				}
			}
			
			// Get distinct mobile names for simple user accounts
			profileModesFilterOptions.setMobile(new ArrayList <String>());
			profileModesFilterOptions.getMobile().add("ON");
			profileModesFilterOptions.getMobile().add("OFF");
			
			
			// Get distinct social names for simple user accounts
			//TODO: Currently social is not supported
			profileModesFilterOptions.setSocial(new ArrayList <String>());
			profileModesFilterOptions.getSocial().add("ON");
			profileModesFilterOptions.getSocial().add("OFF");
						
			return profileModesFilterOptions;
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