package eu.daiad.web.repository.application;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.daiad.web.domain.application.AccountEntity;
import eu.daiad.web.domain.application.AccountProfileEntity;
import eu.daiad.web.domain.application.AccountProfileHistoryEntity;
import eu.daiad.web.domain.application.DeviceAmphiroConfigurationEntity;
import eu.daiad.web.domain.application.DeviceAmphiroDefaultConfigurationEntity;
import eu.daiad.web.domain.application.DeviceAmphiroEntity;
import eu.daiad.web.domain.application.HouseholdEntity;
import eu.daiad.web.domain.application.HouseholdMemberEntity;
import eu.daiad.web.domain.application.UtilityEntity;
import eu.daiad.web.model.EnumApplication;
import eu.daiad.web.model.EnumGender;
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
import eu.daiad.web.model.profile.EnumUnit;
import eu.daiad.web.model.profile.EnumUtilityMode;
import eu.daiad.web.model.profile.EnumWebMode;
import eu.daiad.web.model.profile.Household;
import eu.daiad.web.model.profile.HouseholdMember;
import eu.daiad.web.model.profile.Profile;
import eu.daiad.web.model.profile.ProfileDeactivateRequest;
import eu.daiad.web.model.profile.ProfileHistoryEntry;
import eu.daiad.web.model.profile.ProfileModeChange;
import eu.daiad.web.model.profile.ProfileModes;
import eu.daiad.web.model.profile.ProfileModesChanges;
import eu.daiad.web.model.profile.ProfileModesFilterOptions;
import eu.daiad.web.model.profile.ProfileModesRequest;
import eu.daiad.web.model.profile.ProfileModesSubmitChangesRequest;
import eu.daiad.web.model.profile.UpdateHouseholdRequest;
import eu.daiad.web.model.profile.UpdateProfileRequest;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.model.security.EnumRole;
import eu.daiad.web.model.security.RoleConstant;
import eu.daiad.web.model.utility.UtilityInfo;
import eu.daiad.web.repository.BaseRepository;

@Repository()
@Transactional("applicationTransactionManager")
public class JpaProfileRepository extends BaseRepository implements IProfileRepository {

    @PersistenceContext(unitName = "default")
    EntityManager entityManager;

    @Autowired
    private IDeviceRepository deviceRepository;

    @Autowired
    Environment environment;

    @Override
    public Profile getProfileByUserKey(UUID userKey, EnumApplication application) throws ApplicationException
    {
        try {
            // Load account data
            TypedQuery<eu.daiad.web.domain.application.AccountEntity> userQuery = entityManager.createQuery(
                            "select a from account a where a.key = :userKey",
                            eu.daiad.web.domain.application.AccountEntity.class).setFirstResult(0).setMaxResults(1);
            userQuery.setParameter("userKey", userKey);

            // Load registered device data
            AccountEntity account = userQuery.getSingleResult();

            List<Device> devices = deviceRepository.getUserDevices(account.getKey(), new DeviceRegistrationQuery());

            // Initialize profile
            Profile profile = new Profile();
            profile.setVersion(account.getProfile().getVersion());

            profile.setKey(account.getKey());
            profile.setUsername(account.getUsername());
            profile.setEmail(account.getEmail());
            profile.setFirstname(account.getFirstname());
            profile.setLastname(account.getLastname());
            profile.setTimezone(account.getTimezone());
            profile.setCountry(account.getCountry());
            profile.setAddress(account.getAddress());
            profile.setGender(account.getGender());
            profile.setBirthdate(account.getBirthdate());
            profile.setPostalCode(account.getPostalCode());
            profile.setLocale(account.getLocale());
            profile.setApplication(application);
            profile.setPhoto(account.getPhoto());

            profile.setDailyMeterBudget(account.getProfile().getDailyMeterBudget());
            profile.setDailyAmphiroBudget(account.getProfile().getDailyAmphiroBudget());
            profile.setUnit(account.getProfile().getUnit());
            profile.setGarden(account.getProfile().getGarden());

            profile.setSendMailEnabled(account.getProfile().isSendMailEnabled());
            profile.setSendMessageEnabled(account.getProfile().isSendMessageEnabled());

            profile.setUtility(new UtilityInfo(account.getUtility()));

            // Initialize devices
            ArrayList<DeviceRegistration> registrations = new ArrayList<>();
            for (Iterator<Device> d = devices.iterator(); d.hasNext();) {
                registrations.add(d.next().toDeviceRegistration());
            }
            profile.setDevices(registrations);

            // Initialize application mode and configuration
            switch (application) {
                case HOME:
                    profile.setMode(account.getProfile().getWebMode());
                    profile.setConfiguration(account.getProfile().getWebConfiguration());
                    profile.setSocial(account.getProfile().isSocialEnabled());
                    break;
                case MOBILE:
                    profile.setMode(account.getProfile().getMobileMode());
                    profile.setConfiguration(account.getProfile().getMobileConfiguration());
                    profile.setSocial(account.getProfile().isSocialEnabled());
                    break;
                case UTILITY:
                    profile.setMode(account.getProfile().getUtilityMode());
                    profile.setConfiguration(account.getProfile().getUtilityConfiguration());
                    profile.setSocial(false);
                    break;
                default:
                    throw createApplicationException(ProfileErrorCode.PROFILE_NOT_SUPPORTED).set("application",
                                    application);
            }

            // Initialize household
            if (account.getHousehold() != null) {
                Household household = new Household(account.getHousehold());
                profile.setHousehold(household);
            }

            return profile;
        } catch (Exception ex) {
            throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
        }
    }

    @Override
    public List<ProfileModes> getProfileModes(ProfileModesRequest filters) throws ApplicationException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AuthenticatedUser user = null;

        if (auth.getPrincipal() instanceof AuthenticatedUser) {
            user = (AuthenticatedUser) auth.getPrincipal();
        } else {
            throw createApplicationException(SharedErrorCode.AUTHORIZATION_ANONYMOUS_SESSION);
        }

        if (!user.hasRole(EnumRole.ROLE_UTILITY_ADMIN, EnumRole.ROLE_SYSTEM_ADMIN)) {
            throw createApplicationException(SharedErrorCode.AUTHORIZATION);
        }

        TypedQuery<AccountEntity> userQuery = null;

        String queryFilterPart = "";
        if (!StringUtils.isBlank(filters.getNameFilter())) {
            queryFilterPart = " AND ( LOWER(a.firstname) LIKE :searchTerm OR "
                            + "       LOWER(a.lastname) LIKE :searchTerm OR "
                            + "       LOWER(a.username) LIKE :searchTerm ) ";
        }

        String queryString = "select    a " +
                             "from      account a join a.roles r " +
                             "where     a.utility.id in :utilities and r.role.name = :userRole " +
                             queryFilterPart +
                             "order by  a.lastname, a.username";

        userQuery = entityManager.createQuery(queryString, AccountEntity.class).setFirstResult(0);
        userQuery.setParameter("utilities", user.getUtilities());
        userQuery.setParameter("userRole", RoleConstant.ROLE_USER);

        if (!StringUtils.isBlank(filters.getNameFilter())) {
            userQuery.setParameter("searchTerm", "%" + filters.getNameFilter().toLowerCase() + "%");
        }

        List<AccountEntity> accounts = userQuery.getResultList();
        List<ProfileModes> profileModesList = new ArrayList<>();

        // List all DeviceAmphiroConfigurationDefault's in a simple HashMap
        List<DeviceAmphiroDefaultConfigurationEntity> defaultConfigurations = deviceRepository.getAmphiroDefaultConfigurations();

        HashMap<Integer, String> simplifiedDefaultConfs = new HashMap<>();
        for (DeviceAmphiroDefaultConfigurationEntity defaultConfiguration : defaultConfigurations) {
            simplifiedDefaultConfs.put(defaultConfiguration.getId(), defaultConfiguration.getTitle());
        }

        for (AccountEntity account : accounts) {
            ProfileModes profileModes = new ProfileModes();

            profileModes.setId(account.getKey());
            profileModes.setName(account.getFullname());
            profileModes.setEmail(account.getUsername());

            UtilityEntity utility = account.getUtility();
            profileModes.setGroupId(utility.getKey());
            profileModes.setGroupName(utility.getName());

            // Applying Utility filter
            if (filters.getGroupName() != null && !filters.getGroupName().equals(profileModes.getGroupName())) {
                continue;
            }

            // Active flag
            if (account.getProfile().getMobileMode() == EnumMobileMode.BLOCK.getValue()) {
                profileModes.setActive(false);
            } else {
                profileModes.setActive(true);
            }

            // Amphiro b1 flag
            List<Device> devices = deviceRepository.getUserDevices(account.getKey(), new DeviceRegistrationQuery());
            List<UUID> deviceKeyList = new ArrayList<>();
            for (Device device : devices) {
                if (device.getType() == EnumDeviceType.AMPHIRO) {
                    deviceKeyList.add(device.getKey());
                }
            }

            if (deviceKeyList.size() > 0) {
                UUID[] deviceKeys = deviceKeyList.toArray(new UUID[0]);
                List<DeviceConfigurationCollection> deviceConfigurations = deviceRepository.getConfiguration(account.getKey(), deviceKeys);

                // Get only the configuration of the first amphiro device, since all
                // amphiro devices ought to have the same configuration.
                if (deviceConfigurations.get(0).getConfigurations().get(0).getTitle()
                                .equals(simplifiedDefaultConfs
                                                .get(DeviceAmphiroDefaultConfigurationEntity.CONFIG_ENABLED_METRIC))
                                || deviceConfigurations
                                                .get(0)
                                                .getConfigurations()
                                                .get(0)
                                                .getTitle()
                                                .equals(simplifiedDefaultConfs
                                                                .get(DeviceAmphiroDefaultConfigurationEntity.CONFIG_ENABLED_IMPERIAL))) {
                    profileModes.setAmphiro(ProfileModes.AmphiroModeState.ON);
                } else {
                    profileModes.setAmphiro(ProfileModes.AmphiroModeState.OFF);
                }

            } else {
                profileModes.setAmphiro(ProfileModes.AmphiroModeState.NOT_APPLICABLE);
            }

            // Applying Amphiro filter
            if (filters.getAmphiro() != null && !filters.getAmphiro().equals(profileModes.getAmphiro().toString())) {
                continue;
            }

            // Mobile Flag
            if (account.getProfile().getMobileMode() == EnumMobileMode.ACTIVE.getValue()) {
                profileModes.setMobile(ProfileModes.MobileModeState.ON);
            } else {
                profileModes.setMobile(ProfileModes.MobileModeState.OFF);
            }

            // Applying Mobile filter
            if (filters.getMobile() != null) {
                if (!filters.getMobile().equals(profileModes.getMobile().toString()))
                    continue;
            }

            // Web Flag
            if (account.getProfile().getWebMode() == EnumWebMode.ACTIVE.getValue()) {
                profileModes.setWeb(ProfileModes.WebModeState.ON);
            } else {
                profileModes.setWeb(ProfileModes.WebModeState.OFF);
            }

            // Social Flag
            if(account.getProfile().isSocialEnabled()) {
                profileModes.setSocial(ProfileModes.SocialModeState.ON);
            } else {
                profileModes.setSocial(ProfileModes.SocialModeState.OFF);
            }

            // Applying Social filter
            if ((filters.getSocial() != null) && (!filters.getSocial().equals(profileModes.getSocial().toString()))) {
                continue;
            }

            profileModesList.add(profileModes);
        }

        return profileModesList;
    }

    @Override
    public void setProfileModes(ProfileModesSubmitChangesRequest modeChangesObject) throws ApplicationException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AuthenticatedUser user = null;

        if (auth.getPrincipal() instanceof AuthenticatedUser) {
            user = (AuthenticatedUser) auth.getPrincipal();
        } else {
            throw createApplicationException(SharedErrorCode.AUTHORIZATION_ANONYMOUS_SESSION);
        }

        if (!user.hasRole(EnumRole.ROLE_UTILITY_ADMIN, EnumRole.ROLE_SYSTEM_ADMIN)) {
            throw createApplicationException(SharedErrorCode.AUTHORIZATION);
        }

        List<DeviceAmphiroDefaultConfigurationEntity> configurations = deviceRepository
                        .getAmphiroDefaultConfigurations();

        // Organizing amphiro default configurations in a handy HashMap
        HashMap<String, DeviceAmphiroDefaultConfigurationEntity> defaultconfigurations = new HashMap<>();
        for (DeviceAmphiroDefaultConfigurationEntity defconf : configurations) {
            if (defconf.getTitle().equals("Enabled Configuration (Metric Units)")) {
                defaultconfigurations.put("ON_Metric", defconf);
            } else if (defconf.getTitle().equals("Enabled Configuration (Imperial Units)")) {
                defaultconfigurations.put("ON_Imperial", defconf);
            } else {
                defaultconfigurations.put("OFF", defconf);
            }
        }

        for (ProfileModesChanges modeChanges : modeChangesObject.getModeChanges()) {

            String accountQueryString = "select a from account a where a.key = :key";
            TypedQuery<AccountEntity> accountQuery = entityManager.createQuery(accountQueryString, AccountEntity.class)
                                                                  .setFirstResult(0).setMaxResults(1);
            accountQuery.setParameter("key", modeChanges.getId());

            AccountEntity account = accountQuery.getSingleResult();

            for (ProfileModeChange modeChange : modeChanges.getChanges()) {
                switch (modeChange.getMode()) {
                    case "amphiro":
                        // Selecting the suitable default configuration
                        DeviceAmphiroDefaultConfigurationEntity newDefaultConfiguration = null;
                        if (modeChange.getValue().equals("OFF")) {
                            newDefaultConfiguration = defaultconfigurations.get("OFF");
                        } else if (modeChange.getValue().equals("ON")) {
                            if (account.getUtility().getName().equals("St Albans")) {
                                newDefaultConfiguration = defaultconfigurations.get("ON_Imperial");
                            } else if (account.getUtility().getName().equals("Alicante")) {
                                newDefaultConfiguration = defaultconfigurations.get("ON_Metric");
                            } else {
                                if (account.getLocale().equals("en")) {
                                    newDefaultConfiguration = defaultconfigurations.get("ON_Imperial");
                                } else {
                                    newDefaultConfiguration = defaultconfigurations.get("ON_Metric");
                                }
                            }

                            // Set also mobile mode to OFF, if the user
                            // hasn't set explicitly mobile mode
                            boolean mobileModeChangeExists = false;
                            for (ProfileModeChange change : modeChanges.getChanges()) {
                                if (change.getMode().equals("mobile")) {
                                    mobileModeChangeExists = true;
                                }
                            }
                            if (!mobileModeChangeExists) {
                                setMobileMode(account, EnumMobileMode.INACTIVE.getValue());
                            }
                        } else {
                            newDefaultConfiguration = defaultconfigurations.get("OFF");
                        }

                        // Get the current (active and inactive)
                        // configurations and set them as inactive
                        Set<eu.daiad.web.domain.application.DeviceEntity> devices = account.getDevices();
                        for (eu.daiad.web.domain.application.DeviceEntity device : devices) {

                            if (device.getType() == EnumDeviceType.AMPHIRO) {
                                DeviceAmphiroConfigurationEntity newConfiguration = new DeviceAmphiroConfigurationEntity();

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

                                DeviceAmphiroEntity deviceAmphiro = (DeviceAmphiroEntity) device;
                                for (DeviceAmphiroConfigurationEntity currentConfiguration : deviceAmphiro
                                                .getConfigurations()) {
                                    currentConfiguration.setActive(false);
                                }
                                deviceAmphiro.getConfigurations().add(newConfiguration);
                            }
                        }

                        break;
                    case "mobile":
                        int newMobileMode = EnumMobileMode.UNDEFINED.getValue();
                        if (modeChange.getValue().equals("ON")) {
                            newMobileMode = EnumMobileMode.ACTIVE.getValue();
                        } else if (modeChange.getValue().equals("OFF")) {
                            newMobileMode = EnumMobileMode.INACTIVE.getValue();
                        }
                        setMobileMode(account, newMobileMode);

                        break;
                    case "web":
                        int newWebMode = EnumWebMode.UNDEFINED.getValue();
                        if (modeChange.getValue().equals("ON")) {
                            newWebMode = EnumWebMode.ACTIVE.getValue();
                        } else if (modeChange.getValue().equals("OFF")) {
                            newWebMode = EnumWebMode.INACTIVE.getValue();
                        }
                        setWebMode(account, newWebMode);

                        break;
                    case "social":
                        boolean isSocialEnabled = false;
                        if (modeChange.getValue().equals("ON")) {
                            isSocialEnabled = true;
                        } else if (modeChange.getValue().equals("OFF")) {
                            isSocialEnabled = false;
                        }
                        setSocial(account, isSocialEnabled);

                        break;
                }

                entityManager.persist(account);
            }
        }
    }

    @Override
    public void deactivateProfile(ProfileDeactivateRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AuthenticatedUser user = null;

        if (auth.getPrincipal() instanceof AuthenticatedUser) {
            user = (AuthenticatedUser) auth.getPrincipal();
        } else {
            throw createApplicationException(SharedErrorCode.AUTHORIZATION_ANONYMOUS_SESSION);
        }

        if (!user.hasRole(EnumRole.ROLE_UTILITY_ADMIN, EnumRole.ROLE_SYSTEM_ADMIN)) {
            throw createApplicationException(SharedErrorCode.AUTHORIZATION);
        }

        String accountQueryString = "select a from account a where a.key = :key";
        TypedQuery<AccountEntity> accountQuery = entityManager.createQuery(accountQueryString, AccountEntity.class)
                                                              .setFirstResult(0)
                                                              .setMaxResults(1);

        accountQuery.setParameter("key", request.getUserkey());

        AccountEntity account = accountQuery.getSingleResult();
        UUID newVersion = UUID.randomUUID();

        account.getProfile().setVersion(newVersion);

        account.getProfile().setMobileMode(EnumMobileMode.BLOCK.getValue());
        account.getProfile().setWebMode(EnumWebMode.INACTIVE.getValue());
        account.getProfile().setUtilityMode(EnumUtilityMode.INACTIVE.getValue());
        account.getProfile().setSocialEnabled(false);

        DateTime now = new DateTime();
        account.getProfile().setUpdatedOn(now);

        AccountProfileHistoryEntity historyEntry = new AccountProfileHistoryEntity();
        historyEntry.setProfile(account.getProfile());
        historyEntry.setUpdatedOn(now);
        historyEntry.setVersion(newVersion);
        historyEntry.setMobileMode(EnumMobileMode.BLOCK.getValue());
        historyEntry.setWebMode(EnumWebMode.INACTIVE.getValue());
        historyEntry.setUtilityMode(EnumUtilityMode.INACTIVE.getValue());
        historyEntry.setSocialEnabled(false);

        // Deactivate with amphiro devices
        List<DeviceAmphiroDefaultConfigurationEntity> configurations = deviceRepository.getAmphiroDefaultConfigurations();

        DeviceAmphiroDefaultConfigurationEntity offConfiguration = null;
        for (DeviceAmphiroDefaultConfigurationEntity defconf : configurations) {
            if (defconf.getTitle().equals("Off Configuration")) {
                offConfiguration = defconf;
            }
        }

        if (offConfiguration == null) {
            throw createApplicationException(DeviceErrorCode.OFF_AMPHIRO_CONFIGURATION_NOT_FOUND);
        }

        // Get the current (active and inactive) configurations and set them
        // as inactive
        Set<eu.daiad.web.domain.application.DeviceEntity> devices = account.getDevices();
        for (eu.daiad.web.domain.application.DeviceEntity device : devices) {

            if (device.getType() == EnumDeviceType.AMPHIRO) {

                DeviceAmphiroConfigurationEntity newConfiguration = new DeviceAmphiroConfigurationEntity();

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

                DeviceAmphiroEntity deviceAmphiro = (DeviceAmphiroEntity) device;
                for (DeviceAmphiroConfigurationEntity currentConfiguration : deviceAmphiro.getConfigurations()) {
                    currentConfiguration.setActive(false);
                }
                deviceAmphiro.getConfigurations().add(newConfiguration);
            }
        }

        entityManager.persist(account);
        entityManager.persist(historyEntry);
    }

    @Override
    public ProfileModesFilterOptions getFilterOptions() throws ApplicationException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AuthenticatedUser user = null;

        if (auth.getPrincipal() instanceof AuthenticatedUser) {
            user = (AuthenticatedUser) auth.getPrincipal();
        } else {
            throw createApplicationException(SharedErrorCode.AUTHORIZATION_ANONYMOUS_SESSION);
        }

        if (!user.hasRole(EnumRole.ROLE_UTILITY_ADMIN, EnumRole.ROLE_SYSTEM_ADMIN)) {
            throw createApplicationException(ProfileErrorCode.PROFILE_NOT_SUPPORTED);
        }

        ProfileModesFilterOptions profileModesFilterOptions = new ProfileModesFilterOptions();

        // Get distinct utility names for simple user accounts
        String utilityQuery = "select   distinct (a.utility.name) " +
                              "from     account a inner join a.roles r " +
                              "where    a.utility.id in :utilities and r.role.name = :role";

        TypedQuery<String> utilityNameQuery = entityManager.createQuery(utilityQuery, String.class).setFirstResult(0);
        utilityNameQuery.setParameter("utilities", user.getUtilities());
        utilityNameQuery.setParameter("role", RoleConstant.ROLE_USER);

        List<String> utilityOptions = utilityNameQuery.getResultList();
        profileModesFilterOptions.setGroupName(utilityOptions);

        // Get distinct amphiro names for simple user accounts
        profileModesFilterOptions.setAmphiro(new ArrayList<String>());
        TypedQuery<String> amphiroQuery = entityManager.createQuery(
                        "SELECT DISTINCT(dc.title) FROM device_amphiro_config dc", String.class).setFirstResult(0);

        List<String> deviceConfigOptions = amphiroQuery.getResultList();

        for (String dc : deviceConfigOptions) {
            if (dc.equals("Default Configuration")) {
                profileModesFilterOptions.getAmphiro().add("OFF");
            } else if (dc.startsWith("Enabled Configuration")) {
                profileModesFilterOptions.getAmphiro().add("ON");
            }
            if (profileModesFilterOptions.getAmphiro().size() > 1) {
                break;
            }
        }
        // check if there is at least a user without registered amphiro
        // devices
        // TODO Refactor query
        TypedQuery<AccountEntity> userQuery = entityManager.createQuery(
                        "SELECT a FROM account a JOIN a.roles r WHERE a.utility.id in :utilities and r.role.name = :role",
                        AccountEntity.class).setFirstResult(0);
        userQuery.setParameter("utilities", user.getUtilities());
        userQuery.setParameter("role", RoleConstant.ROLE_USER);

        List<AccountEntity> userAccounts = userQuery.getResultList();
        for (AccountEntity a : userAccounts) {
            int amphiroCount = 0;
            for (eu.daiad.web.domain.application.DeviceEntity d : a.getDevices()) {
                if (d.getType() == EnumDeviceType.AMPHIRO) {
                    amphiroCount++;
                }
            }
            if (amphiroCount == 0) {
                profileModesFilterOptions.getAmphiro().add("NOT_APPLICABLE");
                break;
            }
        }

        // Get distinct mobile names for simple user accounts
        profileModesFilterOptions.setMobile(new ArrayList<String>());
        profileModesFilterOptions.getMobile().add("ON");
        profileModesFilterOptions.getMobile().add("OFF");

        // Get distinct web names for simple user accounts
        profileModesFilterOptions.setWeb(new ArrayList<String>());
        profileModesFilterOptions.getWeb().add("ON");
        profileModesFilterOptions.getWeb().add("OFF");

        // Get distinct social names for simple user accounts
        // TODO: Currently social is not supported
        profileModesFilterOptions.setSocial(new ArrayList<String>());
        profileModesFilterOptions.getSocial().add("ON");
        profileModesFilterOptions.getSocial().add("OFF");

        return profileModesFilterOptions;
    }

    @Override
    public void saveProfile(UpdateProfileRequest updates) throws ApplicationException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AuthenticatedUser user = null;

        if (auth.getPrincipal() instanceof AuthenticatedUser) {
            user = (AuthenticatedUser) auth.getPrincipal();
        } else {
            throw createApplicationException(SharedErrorCode.AUTHORIZATION_ANONYMOUS_SESSION);
        }

        String accountQueryString = "select a from account a where a.key = :key";
        TypedQuery<AccountEntity> query = entityManager.createQuery(accountQueryString, AccountEntity.class).setFirstResult(0).setMaxResults(1);
        query.setParameter("key", user.getKey());

        // Update account and profile
        AccountEntity account = query.getSingleResult();

        if (!StringUtils.isBlank(updates.getConfiguration())) {
            switch (updates.getApplication()) {
                case HOME:
                    account.getProfile().setWebConfiguration(updates.getConfiguration());
                    break;
                case MOBILE:
                    account.getProfile().setMobileConfiguration(updates.getConfiguration());
                    break;
                case UTILITY:
                    account.getProfile().setUtilityConfiguration(updates.getConfiguration());
                    break;
                default:
                    throw createApplicationException(ProfileErrorCode.PROFILE_NOT_SUPPORTED).set("application", updates.getApplication());
            }
        }

        if (updates.getDailyMeterBudget() != null) {
            account.getProfile().setDailyMeterBudget(updates.getDailyMeterBudget());
        }
        if (updates.getDailyAmphiroBudget() != null) {
            account.getProfile().setDailyAmphiroBudget(updates.getDailyAmphiroBudget());
        }
        if ((updates.getUnit() != null) && (updates.getUnit() != EnumUnit.UNDEFINED)) {
            account.getProfile().setUnit(updates.getUnit());
        }
        if (updates.getGarden() != null) {
            account.getProfile().setGarden(updates.getGarden());
        }

        if (!StringUtils.isBlank(updates.getLastname())) {
            account.setLastname(updates.getLastname());
        }
        if (!StringUtils.isBlank(updates.getFirstname())) {
            account.setFirstname(updates.getFirstname());
        }
        if (!StringUtils.isBlank(updates.getLocale())) {
            String locale = updates.getLocale();
            if (locale.length() > 2) {
                locale = locale.substring(0, 2);
            }
            account.setLocale(locale);
        }
        if (!StringUtils.isBlank(updates.getTimezone())) {
            account.setTimezone(updates.getTimezone());
        }

        if (!StringUtils.isBlank(updates.getAddress())) {
            account.setAddress(updates.getAddress());
        }
        if (!StringUtils.isBlank(updates.getCountry())) {
            account.setCountry(updates.getCountry());
        }
        if (!StringUtils.isBlank(updates.getPostalCode())) {
            account.setPostalCode(updates.getPostalCode());
        }

        if((updates.getPhoto() == null) || (updates.getPhoto().length == 0)) {
            if(updates.isResetPhoto()) {
                account.setPhoto(null);
            }
        } else {
            account.setPhoto(updates.getPhoto());
        }
        if (updates.getBirthdate() == null) {
            account.setBirthdate(updates.getBirthdate());
        }
        if ((updates.getGender() != null) && (updates.getGender() != EnumGender.UNDEFINED)) {
            account.setGender(updates.getGender());
        }

        /*
        // Update default household member
        HouseholdMemberEntity member = account.getHousehold().getDefaultMember();
        if (member != null) {
            member.setUpdatedOn(DateTime.now());
            member.setGender(account.getGender());
            member.setPhoto(account.getPhoto());
            if(StringUtils.isBlank(member.getName())) {
                member.setName(account.getFirstname());
            }
            if ((member.getAge() == null) && (account.getBirthdate() != null)) {
                member.setAge(new Period(account.getBirthdate(), DateTime.now()).getYears());
            }
        }
        */

    }

    @Override
    public void notifyProfile(EnumApplication application, UUID version, DateTime updatedOn) {
        try {
            boolean found = false;

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            AuthenticatedUser user = null;

            if (auth.getPrincipal() instanceof AuthenticatedUser) {
                user = (AuthenticatedUser) auth.getPrincipal();
            } else {
                throw createApplicationException(SharedErrorCode.AUTHORIZATION_ANONYMOUS_SESSION);
            }

            TypedQuery<AccountEntity> query = entityManager.createQuery("select a from account a where a.key = :key",
                            AccountEntity.class).setFirstResult(0).setMaxResults(1);
            query.setParameter("key", user.getKey());

            AccountEntity account = query.getSingleResult();

            switch (application) {
                case HOME:
                    throw createApplicationException(ProfileErrorCode.PROFILE_NOT_SUPPORTED).set("application",
                                    application);
                case MOBILE:
                    for (AccountProfileHistoryEntity h : account.getProfile().getHistory()) {
                        if (h.getVersion().equals(version)) {
                            found = true;

                            h.setEnabledOn(updatedOn);
                            h.setAcknowledgedOn(new DateTime());
                            break;
                        }
                    }
                    break;
                default:
                    throw createApplicationException(ProfileErrorCode.PROFILE_NOT_SUPPORTED).set("application",
                                    application);
            }
            if (!found) {
                throw createApplicationException(ProfileErrorCode.PROFILE_VERSION_NOT_FOUND).set("version", version);
            }
        } catch (Exception ex) {
            throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
        }
    }

    @Override
    public void saveHousehold(UpdateHouseholdRequest updates) {
        // Get account
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AuthenticatedUser user = null;

        if (auth.getPrincipal() instanceof AuthenticatedUser) {
            user = (AuthenticatedUser) auth.getPrincipal();
        } else {
            throw createApplicationException(SharedErrorCode.AUTHORIZATION_ANONYMOUS_SESSION);
        }

        TypedQuery<AccountEntity> query = entityManager.createQuery("select a from account a where a.key = :key",
                        AccountEntity.class).setFirstResult(0).setMaxResults(1);
        query.setParameter("key", user.getKey());

        AccountEntity account = query.getSingleResult();

        // Initialize household
        HouseholdEntity householdEntity = account.getHousehold();
        DateTime updatedOn = DateTime.now();

        if (householdEntity == null) {
            householdEntity = new HouseholdEntity();
            householdEntity.setAccount(account);
            householdEntity.setCreatedOn(updatedOn);
            householdEntity.setUpdatedOn(updatedOn);
            entityManager.persist(householdEntity);
        } else {
            householdEntity.setUpdatedOn(updatedOn);
        }

        // Sort members
        List<HouseholdMember> members = updates.getMembers();

        Collections.sort(members, new Comparator<HouseholdMember>() {

            @Override
            public int compare(HouseholdMember first, HouseholdMember second) {
                if (first.getIndex() == second.getIndex()) {
                    throw createApplicationException(ProfileErrorCode.HOUSEHOLD_MEMBER_DUPLICATE_INDEX).set("index", first.getIndex());
                } else if (first.getIndex() < second.getIndex()) {
                    return -1;
                } else {
                    return 1;
                }
            }
        });

        for (HouseholdMember member : members) {
            HouseholdMemberEntity householdMemberEntity = account.getHousehold().getMember(member.getIndex());

            if (householdMemberEntity == null) {
                householdMemberEntity = new HouseholdMemberEntity();

                householdMemberEntity.setIndex(member.getIndex());
                householdMemberEntity.setCreatedOn(updatedOn);
                householdMemberEntity.setUpdatedOn(updatedOn);

                householdMemberEntity.setHousehold(householdEntity);

                entityManager.persist(householdMemberEntity);
            } else {
                householdMemberEntity.setUpdatedOn(updatedOn);
            }

            /*
            if (member.getIndex() == 0) {
                account.setPhoto(member.getPhoto());
                account.setGender(member.getGender());
            }
            */

            if (member.getIndex() != 0) {
                householdMemberEntity.setActive(member.isActive());
            }
            householdMemberEntity.setName(member.getName());
            householdMemberEntity.setGender(member.getGender());
            householdMemberEntity.setAge(member.getAge());
            householdMemberEntity.setPhoto(member.getPhoto());
        }

        for (HouseholdMemberEntity householdMemberEntity : account.getHousehold().getMembers()) {
            if (updates.getMember(householdMemberEntity.getIndex()) == null) {
                if (householdMemberEntity.getIndex() != 0) {
                    householdMemberEntity.setActive(false);
                }
            }
        }
    }

    private void setMobileMode(AccountEntity account, int mode) {
        UUID newVersion = UUID.randomUUID();

        account.getProfile().setVersion(newVersion);

        account.getProfile().setMobileMode(mode);
        DateTime now = new DateTime();
        account.getProfile().setUpdatedOn(now);

        AccountProfileHistoryEntity historyEntry = new AccountProfileHistoryEntity();
        historyEntry.setProfile(account.getProfile());
        historyEntry.setUpdatedOn(now);
        historyEntry.setVersion(newVersion);
        historyEntry.setMobileMode(mode);
        historyEntry.setUtilityMode(account.getProfile().getUtilityMode());
        historyEntry.setWebMode(account.getProfile().getWebMode());
        historyEntry.setSocialEnabled(account.getProfile().isSocialEnabled());

        entityManager.persist(historyEntry);
    }

    private void setWebMode(AccountEntity account, int mode) {
        UUID newVersion = UUID.randomUUID();

        account.getProfile().setVersion(newVersion);

        account.getProfile().setWebMode(mode);
        DateTime now = new DateTime();
        account.getProfile().setUpdatedOn(now);

        AccountProfileHistoryEntity historyEntry = new AccountProfileHistoryEntity();
        historyEntry.setProfile(account.getProfile());
        historyEntry.setUpdatedOn(now);
        historyEntry.setVersion(newVersion);
        historyEntry.setMobileMode(account.getProfile().getMobileMode());
        historyEntry.setUtilityMode(account.getProfile().getUtilityMode());
        historyEntry.setWebMode(mode);
        historyEntry.setSocialEnabled(account.getProfile().isSocialEnabled());

        entityManager.persist(historyEntry);
    }

    private void setSocial(AccountEntity account, boolean enabled) {
        UUID newVersion = UUID.randomUUID();

        account.getProfile().setVersion(newVersion);

        account.getProfile().setSocialEnabled(enabled);
        DateTime now = new DateTime();
        account.getProfile().setUpdatedOn(now);

        AccountProfileHistoryEntity historyEntry = new AccountProfileHistoryEntity();
        historyEntry.setProfile(account.getProfile());
        historyEntry.setUpdatedOn(now);
        historyEntry.setVersion(newVersion);
        historyEntry.setMobileMode(account.getProfile().getMobileMode());
        historyEntry.setUtilityMode(account.getProfile().getUtilityMode());
        historyEntry.setWebMode(account.getProfile().getWebMode());
        historyEntry.setSocialEnabled(enabled);

        entityManager.persist(historyEntry);
    }

    @Override
    public List<ProfileHistoryEntry> getProfileHistoryByUserKey(UUID userKey) {
        String sqlString = "select a from account a where a.key = :userKey";

        TypedQuery<AccountEntity> query = entityManager.createQuery(sqlString, AccountEntity.class)
                                                       .setFirstResult(0)
                                                       .setMaxResults(1);
        query.setParameter("userKey", userKey);

        AccountEntity account = query.getSingleResult();

        List<ProfileHistoryEntry> entries = new ArrayList<>();

        for (AccountProfileHistoryEntity h : account.getProfile().getHistory()) {
            ProfileHistoryEntry entry = new ProfileHistoryEntry();

            entry.setAcknowledgedOn(h.getAcknowledgedOn());
            entry.setEnabledOn(h.getEnabledOn());
            entry.setId(h.getId());
            entry.setMobileMode(EnumMobileMode.fromInteger(h.getMobileMode()));
            entry.setWebMode(EnumWebMode.fromInteger(h.getWebMode()));
            entry.setUtilityMode(EnumUtilityMode.fromInteger(h.getUtilityMode()));
            entry.setSocialEnabled(h.isSocialEnabled());
            entry.setUpdatedOn(h.getUpdatedOn());
            entry.setVersion(h.getVersion());

            entries.add(entry);
        }

        Collections.sort(entries, new Comparator<ProfileHistoryEntry>() {

            @Override
            public int compare(ProfileHistoryEntry e1, ProfileHistoryEntry e2) {
                if (e1.getUpdatedOn().getMillis() == e2.getUpdatedOn().getMillis()) {
                    throw new RuntimeException("History entry updated timestamp must be unique.");
                } else if (e1.getUpdatedOn().getMillis() < e2.getUpdatedOn().getMillis()) {
                    return -1;
                } else {
                    return 1;
                }
            }
        });

        return entries;
    }

    /**
     * Update the DAIAD@home mobile application version for the user with the given key.
     *
     * @param userKey the user key.
     * @param version the application version.
     */
    @Override
    public void updateMobileVersion(UUID userKey, String version) {
        String sqlString = "select p from account_profile p where p.account.key = :userKey";

        TypedQuery<AccountProfileEntity> query = entityManager.createQuery(sqlString, AccountProfileEntity.class)
                                                              .setFirstResult(0)
                                                              .setMaxResults(1);
        query.setParameter("userKey", userKey);

        AccountProfileEntity profile = query.getSingleResult();

        if(StringUtils.isBlank(version)) {
            profile.setMobileApplicationVersion("unknown");
        } else {
            profile.setMobileApplicationVersion(version);
        }
    }

}
