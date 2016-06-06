package eu.daiad.web.repository.application;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.vividsolutions.jts.geom.Geometry;

import eu.daiad.web.domain.application.AccountWhiteListEntry;
import eu.daiad.web.domain.application.DeviceAmphiro;
import eu.daiad.web.domain.application.DeviceAmphiroConfiguration;
import eu.daiad.web.domain.application.DeviceAmphiroConfigurationDefault;
import eu.daiad.web.domain.application.DeviceMeter;
import eu.daiad.web.domain.application.DeviceProperty;
import eu.daiad.web.model.KeyValuePair;
import eu.daiad.web.model.device.AmphiroDevice;
import eu.daiad.web.model.device.Device;
import eu.daiad.web.model.device.DeviceConfigurationCollection;
import eu.daiad.web.model.device.DeviceRegistrationQuery;
import eu.daiad.web.model.device.EnumDeviceType;
import eu.daiad.web.model.device.WaterMeterDevice;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.error.DeviceErrorCode;
import eu.daiad.web.model.error.SharedErrorCode;
import eu.daiad.web.model.error.UserErrorCode;
import eu.daiad.web.model.security.AuthenticatedUser;
import eu.daiad.web.repository.BaseRepository;

@Repository()
@Transactional("applicationTransactionManager")
public class JpaDeviceRepository extends BaseRepository implements IDeviceRepository {

    @Value("${security.white-list}")
    private boolean enforceWhiteListCheck;

    @PersistenceContext(unitName = "default")
    EntityManager entityManager;

    @Override
    public UUID createAmphiroDevice(UUID userKey, String name, String macAddress, String aesKey,
                    ArrayList<KeyValuePair> properties) throws ApplicationException {
        UUID deviceKey = null;

        try {
            TypedQuery<eu.daiad.web.domain.application.Account> query = entityManager
                            .createQuery("select a from account a where a.key = :key",
                                            eu.daiad.web.domain.application.Account.class).setFirstResult(0)
                            .setMaxResults(1);
            query.setParameter("key", userKey);

            eu.daiad.web.domain.application.Account account = query.getSingleResult();

            eu.daiad.web.domain.application.DeviceAmphiro amphiro = new eu.daiad.web.domain.application.DeviceAmphiro();
            amphiro.setName(name);
            amphiro.setRegisteredOn(new DateTime());
            amphiro.setMacAddress(macAddress);
            amphiro.setAesKey(aesKey);

            if (properties != null) {
                for (KeyValuePair p : properties) {
                    amphiro.getProperties().add(new DeviceProperty(p.getKey(), p.getValue()));
                }
            }

            TypedQuery<DeviceAmphiroConfigurationDefault> configQuery = entityManager.createQuery(
                            "select c from device_amphiro_config_default c where c.id = :id",
                            DeviceAmphiroConfigurationDefault.class).setFirstResult(0).setMaxResults(1);
            configQuery.setParameter("id", DeviceAmphiroConfigurationDefault.CONFIG_DEFAULT);

            DeviceAmphiroConfigurationDefault defaultConfiguration = configQuery.getSingleResult();

            DeviceAmphiroConfiguration configuration = new DeviceAmphiroConfiguration();
            configuration.setActive(true);
            configuration.setBlock(defaultConfiguration.getBlock());
            configuration.setCreatedOn(new DateTime());
            configuration.setDevice(amphiro);
            configuration.setFrameDuration(defaultConfiguration.getFrameDuration());
            configuration.setNumberOfFrames(defaultConfiguration.getNumberOfFrames());
            configuration.setTitle(defaultConfiguration.getTitle());
            configuration.setValue1(defaultConfiguration.getValue1());
            configuration.setValue2(defaultConfiguration.getValue2());
            configuration.setValue3(defaultConfiguration.getValue3());
            configuration.setValue4(defaultConfiguration.getValue4());
            configuration.setValue5(defaultConfiguration.getValue5());
            configuration.setValue6(defaultConfiguration.getValue6());
            configuration.setValue7(defaultConfiguration.getValue7());
            configuration.setValue8(defaultConfiguration.getValue8());
            configuration.setValue9(defaultConfiguration.getValue9());
            configuration.setValue10(defaultConfiguration.getValue10());
            configuration.setValue11(defaultConfiguration.getValue11());
            configuration.setValue12(defaultConfiguration.getValue12());

            amphiro.getConfigurations().add(configuration);

            account.getDevices().add(amphiro);

            this.entityManager.persist(account);

            deviceKey = amphiro.getKey();
        } catch (Exception ex) {
            throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
        }

        return deviceKey;
    }

    public List<DeviceAmphiroConfigurationDefault> getAmphiroDefaultConfigurations() throws ApplicationException {
        List<DeviceAmphiroConfigurationDefault> configurations = null;

        try {
            TypedQuery<DeviceAmphiroConfigurationDefault> configQuery = entityManager.createQuery(
                            "select c from device_amphiro_config_default c", DeviceAmphiroConfigurationDefault.class)
                            .setFirstResult(0);

            configurations = configQuery.getResultList();

        } catch (Exception ex) {
            throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
        }

        return configurations;
    }

    @Override
    public UUID createMeterDevice(String username, String serial, ArrayList<KeyValuePair> properties, Geometry location)
                    throws ApplicationException {
        UUID deviceKey = null;

        // Get user
        TypedQuery<eu.daiad.web.domain.application.Account> accountQuery = entityManager.createQuery(
                        "select a from account a where a.username = :username",
                        eu.daiad.web.domain.application.Account.class).setFirstResult(0).setMaxResults(1);

        accountQuery.setParameter("username", username);

        List<eu.daiad.web.domain.application.Account> accounts = accountQuery.getResultList();

        // If user exists add meter
        if (accounts.size() == 1) {
            eu.daiad.web.domain.application.Account account = accounts.get(0);

            // Check if a meter already exists
            DeviceRegistrationQuery meterQuery = new DeviceRegistrationQuery();
            meterQuery.setType(EnumDeviceType.METER);

            ArrayList<Device> meters = getUserDevices(account.getKey(), meterQuery);

            if (!meters.isEmpty()) {
                throw createApplicationException(DeviceErrorCode.ALREADY_EXISTS).set("id", serial);
            }

            // Update account location if not already set
            if (account.getLocation() == null) {
                account.setLocation(location);
            }

            // Create new device
            eu.daiad.web.domain.application.DeviceMeter meter = new eu.daiad.web.domain.application.DeviceMeter();
            meter.setSerial(serial);
            meter.setLocation(location);
            meter.setRegisteredOn(new DateTime());

            if (properties != null) {
                for (KeyValuePair p : properties) {
                    meter.getProperties().add(new DeviceProperty(p.getKey(), p.getValue()));
                }
            }

            account.getDevices().add(meter);

            this.entityManager.persist(account);

            deviceKey = meter.getKey();
        } else if (enforceWhiteListCheck) {
            // If the user does not exist, update white list
            TypedQuery<AccountWhiteListEntry> entryQuery = entityManager.createQuery(
                            "select a from account_white_list a where a.username = :username",
                            AccountWhiteListEntry.class).setFirstResult(0).setMaxResults(1);

            entryQuery.setParameter("username", username);

            List<AccountWhiteListEntry> entries = entryQuery.getResultList();

            if (entries.size() == 1) {
                AccountWhiteListEntry entry = entries.get(0);

                entry.setMeterSerial(serial);
                entry.setMeterLocation(location);

                if (entry.getLocation() == null) {
                    entry.setLocation(location);
                }
                this.entityManager.persist(entry);
            } else {
                throw createApplicationException(UserErrorCode.USERNANE_NOT_FOUND).set("username", username);
            }
        } else {
            throw createApplicationException(UserErrorCode.USERNANE_NOT_FOUND).set("username", username);
        }

        return deviceKey;
    }

    @Override
    public void updateMeterLocation(String username, String serial, Geometry location) throws ApplicationException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AuthenticatedUser user = null;

        if (auth.getPrincipal() instanceof AuthenticatedUser) {
            user = (AuthenticatedUser) auth.getPrincipal();
        } else {
            throw createApplicationException(SharedErrorCode.AUTHORIZATION_ANONYMOUS_SESSION);
        }

        TypedQuery<eu.daiad.web.domain.application.Account> accountQuery = entityManager.createQuery(
                        "select a from account a where a.username = :username and a.utility.id = :utility_id",
                        eu.daiad.web.domain.application.Account.class).setFirstResult(0).setMaxResults(1);

        accountQuery.setParameter("username", username);
        accountQuery.setParameter("utility_id", user.getUtilityId());

        List<eu.daiad.web.domain.application.Account> accounts = accountQuery.getResultList();

        if (accounts.size() == 1) {
            // Update account location if not already set
            if (accounts.get(0).getLocation() == null) {
                accounts.get(0).setLocation(location);
            }

            // Update device location
            TypedQuery<eu.daiad.web.domain.application.DeviceMeter> query = entityManager.createQuery(
                            "select d from device_meter d where d.serial = :serial and d.account.key = :userKey",
                            eu.daiad.web.domain.application.DeviceMeter.class).setFirstResult(0).setMaxResults(1);
            query.setParameter("serial", serial);
            query.setParameter("userKey", accounts.get(0).getKey());

            List<eu.daiad.web.domain.application.DeviceMeter> result = query.getResultList();

            if (result.size() == 1) {
                result.get(0).setLocation(location);

                this.entityManager.flush();
            } else {
                throw createApplicationException(DeviceErrorCode.METER_NOT_FOUND).set("serial", serial);
            }
        } else {
            throw createApplicationException(UserErrorCode.USERNANE_NOT_FOUND).set("username", username);
        }

    }

    @Override
    public Device getUserDeviceByKey(UUID userKey, UUID deviceKey) throws ApplicationException {
        try {
            TypedQuery<eu.daiad.web.domain.application.Device> query = entityManager.createQuery(
                            "select d from device d where d.key = :device_key and d.account.key = :user_key",
                            eu.daiad.web.domain.application.Device.class).setFirstResult(0).setMaxResults(1);
            query.setParameter("user_key", userKey);
            query.setParameter("device_key", deviceKey);

            List<eu.daiad.web.domain.application.Device> result = query.getResultList();

            if (result.size() == 1) {
                eu.daiad.web.domain.application.Device entity = result.get(0);

                switch (entity.getType()) {
                    case AMPHIRO:
                        eu.daiad.web.domain.application.DeviceAmphiro amphiroEntity = (eu.daiad.web.domain.application.DeviceAmphiro) entity;

                        AmphiroDevice amphiro = new AmphiroDevice(amphiroEntity.getAccount().getId(), amphiroEntity
                                        .getKey(), amphiroEntity.getName(), amphiroEntity.getMacAddress(),
                                        amphiroEntity.getAesKey(), entity.getRegisteredOn().getMillis());

                        for (eu.daiad.web.domain.application.DeviceProperty p : amphiroEntity.getProperties()) {
                            amphiro.getProperties().add(new KeyValuePair(p.getKey(), p.getValue()));
                        }

                        return amphiro;
                    case METER:
                        eu.daiad.web.domain.application.DeviceMeter meterEntity = (eu.daiad.web.domain.application.DeviceMeter) entity;

                        WaterMeterDevice meter = new WaterMeterDevice(meterEntity.getAccount().getId(), meterEntity
                                        .getKey(), meterEntity.getSerial(), meterEntity.getLocation(), entity
                                        .getRegisteredOn().getMillis());

                        for (eu.daiad.web.domain.application.DeviceProperty p : meterEntity.getProperties()) {
                            meter.getProperties().add(new KeyValuePair(p.getKey(), p.getValue()));
                        }

                        return meter;
                    default:
                        break;
                }

            }

            return null;
        } catch (Exception ex) {
            throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
        }
    }

    @Override
    public Device getDeviceByKey(UUID deviceKey) throws ApplicationException {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            AuthenticatedUser user = null;

            if (auth.getPrincipal() instanceof AuthenticatedUser) {
                user = (AuthenticatedUser) auth.getPrincipal();
            } else {
                throw createApplicationException(SharedErrorCode.AUTHORIZATION_ANONYMOUS_SESSION);
            }

            TypedQuery<eu.daiad.web.domain.application.Device> query = entityManager.createQuery(
                            "select d from device d "
                                            + "where d.key = :device_key and d.account.utility.id = :utility_id",
                            eu.daiad.web.domain.application.Device.class).setFirstResult(0).setMaxResults(1);
            query.setParameter("utility_id", user.getUtilityId());
            query.setParameter("device_key", deviceKey);

            List<eu.daiad.web.domain.application.Device> result = query.getResultList();

            if (result.size() == 1) {
                eu.daiad.web.domain.application.Device entity = result.get(0);

                switch (entity.getType()) {
                    case AMPHIRO:
                        eu.daiad.web.domain.application.DeviceAmphiro amphiroEntity = (eu.daiad.web.domain.application.DeviceAmphiro) entity;

                        AmphiroDevice amphiro = new AmphiroDevice(amphiroEntity.getAccount().getId(), amphiroEntity
                                        .getKey(), amphiroEntity.getName(), amphiroEntity.getMacAddress(),
                                        amphiroEntity.getAesKey(), entity.getRegisteredOn().getMillis());

                        for (eu.daiad.web.domain.application.DeviceProperty p : amphiroEntity.getProperties()) {
                            amphiro.getProperties().add(new KeyValuePair(p.getKey(), p.getValue()));
                        }

                        return amphiro;
                    case METER:
                        eu.daiad.web.domain.application.DeviceMeter meterEntity = (eu.daiad.web.domain.application.DeviceMeter) entity;

                        WaterMeterDevice meter = new WaterMeterDevice(meterEntity.getAccount().getId(), meterEntity
                                        .getKey(), meterEntity.getSerial(), meterEntity.getLocation(), entity
                                        .getRegisteredOn().getMillis());

                        for (eu.daiad.web.domain.application.DeviceProperty p : meterEntity.getProperties()) {
                            meter.getProperties().add(new KeyValuePair(p.getKey(), p.getValue()));
                        }

                        return meter;
                    default:
                        break;
                }

            }

            return null;
        } catch (Exception ex) {
            throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
        }
    }

    @Override
    public WaterMeterDevice getUserWaterMeterByKey(UUID userKey, UUID deviceKey) {
        TypedQuery<DeviceMeter> meterQuery = entityManager.createQuery("select d from device_meter d "
                        + "where d.key = :device_key and d.account.key = :user_key", DeviceMeter.class);

        meterQuery.setParameter("user_key", userKey);
        meterQuery.setParameter("device_key", deviceKey);

        List<DeviceMeter> meters = meterQuery.getResultList();

        if (!meters.isEmpty()) {
            DeviceMeter meterEntity = meters.get(0);

            WaterMeterDevice meter = new WaterMeterDevice(meterEntity.getAccount().getId(), meterEntity.getKey(),
                            meterEntity.getSerial(), meterEntity.getLocation(), meterEntity.getRegisteredOn()
                                            .getMillis());

            for (eu.daiad.web.domain.application.DeviceProperty p : meterEntity.getProperties()) {
                meter.getProperties().add(new KeyValuePair(p.getKey(), p.getValue()));
            }

            return meter;
        }

        return null;
    }

    @Override
    public ArrayList<Device> getUserDevices(UUID userKey, DeviceRegistrationQuery query) throws ApplicationException {
        ArrayList<Device> devices = new ArrayList<Device>();

        try {
            TypedQuery<eu.daiad.web.domain.application.Device> typedQuery = entityManager.createQuery(
                            "select d from device d where d.account.key = :user_key order by d.registeredOn",
                            eu.daiad.web.domain.application.Device.class).setFirstResult(0);
            typedQuery.setParameter("user_key", userKey);

            List<eu.daiad.web.domain.application.Device> result = typedQuery.getResultList();

            for (eu.daiad.web.domain.application.Device entity : result) {
                switch (entity.getType()) {
                    case AMPHIRO:
                        if ((query.getType() == EnumDeviceType.UNDEFINED) || (query.getType() == entity.getType())) {
                            eu.daiad.web.domain.application.DeviceAmphiro amphiroEntity = (eu.daiad.web.domain.application.DeviceAmphiro) entity;

                            AmphiroDevice amphiro = new AmphiroDevice(amphiroEntity.getAccount().getId(), amphiroEntity
                                            .getKey(), amphiroEntity.getName(), amphiroEntity.getMacAddress(),
                                            amphiroEntity.getAesKey(), entity.getRegisteredOn().getMillis());

                            for (eu.daiad.web.domain.application.DeviceProperty p : amphiroEntity.getProperties()) {
                                amphiro.getProperties().add(new KeyValuePair(p.getKey(), p.getValue()));
                            }

                            devices.add(amphiro);
                        }
                        break;
                    case METER:
                        if ((query.getType() == EnumDeviceType.UNDEFINED) || (query.getType() == entity.getType())) {
                            eu.daiad.web.domain.application.DeviceMeter meterEntity = (eu.daiad.web.domain.application.DeviceMeter) entity;

                            WaterMeterDevice meter = new WaterMeterDevice(meterEntity.getAccount().getId(), meterEntity
                                            .getKey(), meterEntity.getSerial(), meterEntity.getLocation(), entity
                                            .getRegisteredOn().getMillis());

                            for (eu.daiad.web.domain.application.DeviceProperty p : meterEntity.getProperties()) {
                                meter.getProperties().add(new KeyValuePair(p.getKey(), p.getValue()));
                            }

                            devices.add(meter);
                        }
                        break;
                    default:
                        break;
                }

            }
        } catch (Exception ex) {
            throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
        }

        return devices;
    }

    @Override
    public Device getUserAmphiroDeviceByMacAddress(UUID userKey, String macAddress) throws ApplicationException {
        try {
            TypedQuery<eu.daiad.web.domain.application.DeviceAmphiro> query = entityManager.createQuery(
                            "select d from device_amphiro d where d.macAddress = :macAddress and d.account.key = :key",
                            eu.daiad.web.domain.application.DeviceAmphiro.class).setFirstResult(0).setMaxResults(1);
            query.setParameter("key", userKey);
            query.setParameter("macAddress", macAddress);

            List<eu.daiad.web.domain.application.DeviceAmphiro> result = query.getResultList();

            if (result.size() == 1) {
                eu.daiad.web.domain.application.DeviceAmphiro entity = result.get(0);

                AmphiroDevice amphiro = new AmphiroDevice(entity.getAccount().getId(), entity.getKey(), entity
                                .getName(), entity.getMacAddress(), entity.getAesKey(), entity.getRegisteredOn()
                                .getMillis());

                for (eu.daiad.web.domain.application.DeviceProperty p : entity.getProperties()) {
                    amphiro.getProperties().add(new KeyValuePair(p.getKey(), p.getValue()));
                }

                return amphiro;
            }

            return null;
        } catch (Exception ex) {
            throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
        }
    }

    @Override
    public Device getUserWaterMeterDeviceBySerial(UUID userKey, String serial) throws ApplicationException {
        try {
            TypedQuery<eu.daiad.web.domain.application.DeviceMeter> query = entityManager.createQuery(
                            "select d from device_meter d where d.serial = :serial and d.account.key = :userKey",
                            eu.daiad.web.domain.application.DeviceMeter.class).setFirstResult(0).setMaxResults(1);
            query.setParameter("serial", serial);
            query.setParameter("userKey", userKey);

            List<eu.daiad.web.domain.application.DeviceMeter> result = query.getResultList();

            if (result.size() == 1) {
                eu.daiad.web.domain.application.DeviceMeter entity = result.get(0);

                WaterMeterDevice meter = new WaterMeterDevice(entity.getAccount().getId(), entity.getKey(), entity
                                .getSerial(), entity.getLocation(), entity.getRegisteredOn().getMillis());

                for (eu.daiad.web.domain.application.DeviceProperty p : entity.getProperties()) {
                    meter.getProperties().add(new KeyValuePair(p.getKey(), p.getValue()));
                }

                return meter;

            }

            return null;
        } catch (Exception ex) {
            throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
        }
    }

    @Override
    public Device getWaterMeterDeviceBySerial(String serial) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            AuthenticatedUser user = null;

            if (auth.getPrincipal() instanceof AuthenticatedUser) {
                user = (AuthenticatedUser) auth.getPrincipal();
            } else {
                throw createApplicationException(SharedErrorCode.AUTHORIZATION_ANONYMOUS_SESSION);
            }

            TypedQuery<eu.daiad.web.domain.application.DeviceMeter> query = entityManager
                            .createQuery("select d from device_meter d where d.serial = :serial and d.account.utility.id = :utility_id",
                                            eu.daiad.web.domain.application.DeviceMeter.class).setFirstResult(0)
                            .setMaxResults(1);
            query.setParameter("serial", serial);
            query.setParameter("utility_id", user.getUtilityId());

            List<eu.daiad.web.domain.application.DeviceMeter> result = query.getResultList();

            if (result.size() == 1) {
                eu.daiad.web.domain.application.DeviceMeter entity = result.get(0);

                WaterMeterDevice meter = new WaterMeterDevice(entity.getAccount().getId(), entity.getKey(), entity
                                .getSerial(), entity.getLocation(), entity.getRegisteredOn().getMillis());

                for (eu.daiad.web.domain.application.DeviceProperty p : entity.getProperties()) {
                    meter.getProperties().add(new KeyValuePair(p.getKey(), p.getValue()));
                }

                return meter;

            }

            return null;
        } catch (Exception ex) {
            throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
        }
    }

    @Override
    public void shareDevice(UUID ownerID, String assigneeUsername, UUID deviceKey, boolean shared)
                    throws ApplicationException {
        try {
            // Get device
            TypedQuery<eu.daiad.web.domain.application.DeviceAmphiro> deviceQuery = entityManager.createQuery(
                            "select d from device_amphiro d where d.key = :key",
                            eu.daiad.web.domain.application.DeviceAmphiro.class).setFirstResult(0).setMaxResults(1);
            deviceQuery.setParameter("key", deviceKey);

            List<eu.daiad.web.domain.application.DeviceAmphiro> devices = deviceQuery.getResultList();

            if (devices.size() != 1) {
                throw createApplicationException(DeviceErrorCode.NOT_FOUND).set("key", deviceKey.toString());
            }

            // Check owner
            eu.daiad.web.domain.application.DeviceAmphiro device = devices.get(0);

            if (!device.getAccount().getKey().equals(ownerID)) {
                throw createApplicationException(SharedErrorCode.AUTHORIZATION);
            }

            // Get assignee
            TypedQuery<eu.daiad.web.domain.application.Account> userQuery = entityManager.createQuery(
                            "select a from account a where a.username = :username and a.utility.id = :utility_id",
                            eu.daiad.web.domain.application.Account.class).setFirstResult(0).setMaxResults(1);
            userQuery.setParameter("username", assigneeUsername);
            userQuery.setParameter("utility_id", device.getAccount().getUtility().getId());

            List<eu.daiad.web.domain.application.Account> users = userQuery.getResultList();

            if (users.size() == 0) {
                throw createApplicationException(UserErrorCode.USERNANE_NOT_FOUND).set("username", assigneeUsername);
            }

            eu.daiad.web.domain.application.Account assignee = users.get(0);

            if (assignee.getId() == device.getAccount().getId()) {
                return;
            }

            eu.daiad.web.domain.application.DeviceAmphiroPermission permission = null;

            TypedQuery<eu.daiad.web.domain.application.DeviceAmphiroPermission> permissionQuery = entityManager
                            .createQuery("select p from device_amphiro_permission p where p.device.id = :deviceId and p.owner.id = :ownerId and p.assignee.id = :assigneeId",
                                            eu.daiad.web.domain.application.DeviceAmphiroPermission.class)
                            .setFirstResult(0).setMaxResults(1);
            permissionQuery.setParameter("deviceId", device.getId());
            permissionQuery.setParameter("ownerId", device.getAccount().getId());
            permissionQuery.setParameter("assigneeId", assignee.getId());

            List<eu.daiad.web.domain.application.DeviceAmphiroPermission> permissions = permissionQuery.getResultList();
            if (permissions.size() == 1) {
                permission = permissions.get(0);
            }

            if (shared) {
                if (permission == null) {
                    permission = new eu.daiad.web.domain.application.DeviceAmphiroPermission();
                    permission.setDevice(device);
                    permission.setOwner(device.getAccount());
                    permission.setAssignee(assignee);
                    permission.setAssignedOn(DateTime.now());

                    this.entityManager.persist(permission);
                }
            } else {
                if (permission != null) {
                    this.entityManager.remove(permission);
                }
            }

        } catch (Exception ex) {
            throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
        }
    }

    @Override
    public ArrayList<DeviceConfigurationCollection> getConfiguration(UUID userKey, UUID deviceKeys[])
                    throws ApplicationException {
        ArrayList<DeviceConfigurationCollection> collections = new ArrayList<DeviceConfigurationCollection>();
        try {
            for (UUID deviceKey : deviceKeys) {
                TypedQuery<eu.daiad.web.domain.application.DeviceAmphiro> deviceQuery = entityManager.createQuery(
                                "select d from device_amphiro d where d.key = :deviceKey and d.account.key = :userKey",
                                eu.daiad.web.domain.application.DeviceAmphiro.class).setFirstResult(0).setMaxResults(1);
                deviceQuery.setParameter("deviceKey", deviceKey);
                deviceQuery.setParameter("userKey", userKey);

                List<eu.daiad.web.domain.application.DeviceAmphiro> devices = deviceQuery.getResultList();

                if (devices.size() != 1) {
                    throw createApplicationException(DeviceErrorCode.NOT_FOUND).set("key", deviceKey.toString());
                }

                DeviceAmphiro device = devices.get(0);

                DeviceConfigurationCollection deviceConfigurationCollection = new DeviceConfigurationCollection();
                deviceConfigurationCollection.setKey(device.getKey());
                deviceConfigurationCollection.setMacAddress(device.getMacAddress());

                for (DeviceAmphiroConfiguration p : device.getConfigurations()) {
                    if (p.isActive()) {
                        eu.daiad.web.model.device.DeviceAmphiroConfiguration configuration = new eu.daiad.web.model.device.DeviceAmphiroConfiguration();

                        configuration.setTitle(p.getTitle());
                        configuration.setCreatedOn(p.getCreatedOn().getMillis());

                        configuration.getProperties().add(p.getValue1());
                        configuration.getProperties().add(p.getValue2());
                        configuration.getProperties().add(p.getValue3());
                        configuration.getProperties().add(p.getValue4());
                        configuration.getProperties().add(p.getValue5());
                        configuration.getProperties().add(p.getValue6());
                        configuration.getProperties().add(p.getValue7());
                        configuration.getProperties().add(p.getValue8());
                        configuration.getProperties().add(p.getValue9());
                        configuration.getProperties().add(p.getValue10());
                        configuration.getProperties().add(p.getValue11());
                        configuration.getProperties().add(p.getValue12());

                        configuration.setBlock(p.getBlock());
                        configuration.setFrameDuration(p.getFrameDuration());
                        configuration.setNumberOfFrames(p.getNumberOfFrames());

                        configuration.setVersion(p.getVersion());
                        if (p.getAcknowledgedOn() != null) {
                            configuration.setAcknowledgedOn(p.getAcknowledgedOn().getMillis());
                        }
                        if (p.getEnabledOn() != null) {
                            configuration.setEnabledOn(p.getEnabledOn().getMillis());
                        }

                        deviceConfigurationCollection.getConfigurations().add(configuration);
                    }
                }
                if (deviceConfigurationCollection.getConfigurations().size() > 0) {
                    collections.add(deviceConfigurationCollection);
                }
            }
        } catch (Exception ex) {
            throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
        }

        return collections;
    }

    @Override
    public void notifyConfiguration(UUID userKey, UUID deviceKey, UUID version, DateTime updatedOn)
                    throws ApplicationException {
        try {
            boolean found = false;

            TypedQuery<eu.daiad.web.domain.application.DeviceAmphiro> deviceQuery = entityManager.createQuery(
                            "select d from device_amphiro d where d.key = :deviceKey and d.account.key = :userKey",
                            eu.daiad.web.domain.application.DeviceAmphiro.class).setFirstResult(0).setMaxResults(1);
            deviceQuery.setParameter("deviceKey", deviceKey);
            deviceQuery.setParameter("userKey", userKey);

            List<eu.daiad.web.domain.application.DeviceAmphiro> devices = deviceQuery.getResultList();

            if (devices.size() != 1) {
                throw createApplicationException(DeviceErrorCode.NOT_FOUND).set("key", deviceKey.toString());
            }

            eu.daiad.web.domain.application.DeviceAmphiro device = devices.get(0);

            for (DeviceAmphiroConfiguration c : device.getConfigurations()) {
                if (c.getVersion().equals(version)) {
                    c.setAcknowledgedOn(new DateTime());
                    c.setEnabledOn(updatedOn);

                    found = true;
                }
            }

            if (!found) {
                throw createApplicationException(DeviceErrorCode.CONFIGURATION_NOT_FOUND).set("version",
                                version.toString());
            }
        } catch (Exception ex) {
            throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
        }
    }

    @Override
    public void removeDevice(UUID deviceKey) {
        try {
            TypedQuery<eu.daiad.web.domain.application.Device> deviceQuery = entityManager.createQuery(
                            "select d from device d where d.key = :device_key",
                            eu.daiad.web.domain.application.Device.class).setFirstResult(0).setMaxResults(1);

            deviceQuery.setParameter("device_key", deviceKey);

            List<eu.daiad.web.domain.application.Device> result = deviceQuery.getResultList();

            if (result.size() != 1) {
                throw createApplicationException(DeviceErrorCode.NOT_FOUND).set("key", deviceKey);
            }

            for (eu.daiad.web.domain.application.Device d : result) {
                switch (d.getType()) {
                    case AMPHIRO:
                    case METER:
                        d.getAccount().getDevices().remove(d);
                        this.entityManager.remove(d);
                        break;
                    default:
                        throw createApplicationException(DeviceErrorCode.NOT_SUPPORTED).set("type", d.getType());
                }
            }
        } catch (Exception ex) {
            throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
        }
    }

    public void setLastDataUploadDate(UUID userKey, UUID deviceKey, DateTime when, boolean success) {
        try {
            TypedQuery<eu.daiad.web.domain.application.Device> query = entityManager.createQuery(
                            "select d from device d where d.key = :device_key and d.account.key = :user_key",
                            eu.daiad.web.domain.application.Device.class).setFirstResult(0).setMaxResults(1);
            query.setParameter("user_key", userKey);
            query.setParameter("device_key", deviceKey);

            List<eu.daiad.web.domain.application.Device> result = query.getResultList();

            if (result.size() == 1) {
                eu.daiad.web.domain.application.Device entity = result.get(0);

                if (success) {
                    // Compute aggregates
                    if (entity.getLastDataUploadSuccess() != null) {
                        int interval = Math.max(1, Math.abs(Days.daysBetween(when.toLocalDate(),
                                        entity.getLastDataUploadSuccess().toLocalDate()).getDays()));

                        if (entity.getTransmissionCount() == null) {
                            entity.setTransmissionCount(1L);
                        } else {
                            entity.setTransmissionCount(entity.getTransmissionCount() + 1L);
                        }

                        if (entity.getTransmissionIntervalSum() == null) {
                            entity.setTransmissionIntervalSum((long) interval);
                        } else {
                            entity.setTransmissionIntervalSum(entity.getTransmissionIntervalSum() + interval);
                        }

                        if ((entity.getTransmissionIntervalMax() == null)
                                        || (entity.getTransmissionIntervalMax() < interval)) {
                            entity.setTransmissionIntervalMax(interval);
                        }
                    }

                    entity.setLastDataUploadSuccess(when);
                } else {
                    entity.setLastDataUploadFailure(when);
                }

            }
        } catch (Exception ex) {
            throw wrapApplicationException(ex, DeviceErrorCode.LOG_DATA_UPLOAD_FAILED).set("key", deviceKey);
        }
    }
}