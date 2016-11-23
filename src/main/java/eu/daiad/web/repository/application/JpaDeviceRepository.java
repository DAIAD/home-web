package eu.daiad.web.repository.application;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

import eu.daiad.web.domain.application.AccountEntity;
import eu.daiad.web.domain.application.AccountWhiteListEntity;
import eu.daiad.web.domain.application.DeviceAmphiroConfigurationEntity;
import eu.daiad.web.domain.application.DeviceAmphiroDefaultConfigurationEntity;
import eu.daiad.web.domain.application.DeviceAmphiroEntity;
import eu.daiad.web.domain.application.DeviceAmphiroPermissionEntity;
import eu.daiad.web.domain.application.DeviceEntity;
import eu.daiad.web.domain.application.DeviceMeterEntity;
import eu.daiad.web.domain.application.DevicePropertyEntity;
import eu.daiad.web.model.KeyValuePair;
import eu.daiad.web.model.device.AmphiroDevice;
import eu.daiad.web.model.device.AmphiroDeviceUpdate;
import eu.daiad.web.model.device.DefaultAmphiroProperties;
import eu.daiad.web.model.device.Device;
import eu.daiad.web.model.device.DeviceConfigurationCollection;
import eu.daiad.web.model.device.DeviceRegistrationQuery;
import eu.daiad.web.model.device.DeviceUpdate;
import eu.daiad.web.model.device.DeviceUpdateRequest;
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

    private DefaultAmphiroProperties defaultAmphiroProperties;

    @PersistenceContext(unitName = "default")
    EntityManager entityManager;

    public JpaDeviceRepository(DefaultAmphiroProperties defaultAmphiroProperties) {
        this.defaultAmphiroProperties = defaultAmphiroProperties;
    }

    @Override
    public AmphiroDevice createAmphiroDevice(UUID userKey, String name, String macAddress, String aesKey, List<KeyValuePair> properties) throws ApplicationException {
        try {
            TypedQuery<AccountEntity> query = entityManager.createQuery("select a from account a where a.key = :key", AccountEntity.class)
                                                           .setFirstResult(0)
                                                           .setMaxResults(1);
            query.setParameter("key", userKey);

            AccountEntity account = query.getSingleResult();

            DeviceAmphiroEntity amphiro = new DeviceAmphiroEntity();
            amphiro.setName(name);
            amphiro.setRegisteredOn(new DateTime());
            amphiro.setMacAddress(macAddress);
            amphiro.setAesKey(aesKey);

            if (properties != null) {
                for (KeyValuePair p : properties) {
                    amphiro.getProperties().add(new DevicePropertyEntity(p.getKey(), p.getValue()));
                }
            }

            String deviceQueryString = "select c from device_amphiro_config_default c where c.id = :id";
            TypedQuery<DeviceAmphiroDefaultConfigurationEntity> configQuery = entityManager.createQuery(deviceQueryString, DeviceAmphiroDefaultConfigurationEntity.class)
                                                                                           .setFirstResult(0)
                                                                                           .setMaxResults(1);
            configQuery.setParameter("id", account.getUtility().getDefaultAmphiroB1Mode());

            DeviceAmphiroDefaultConfigurationEntity defaultConfiguration = configQuery.getSingleResult();

            DeviceAmphiroConfigurationEntity configuration = new DeviceAmphiroConfigurationEntity();
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

            entityManager.persist(account);

            return (AmphiroDevice) getUserDeviceByKey(userKey, amphiro.getKey());
        } catch (Exception ex) {
            throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
        }
    }

    @Override
    public List<DeviceAmphiroDefaultConfigurationEntity> getAmphiroDefaultConfigurations() throws ApplicationException {
        List<DeviceAmphiroDefaultConfigurationEntity> configurations = null;

        try {
            TypedQuery<DeviceAmphiroDefaultConfigurationEntity> configQuery = entityManager.createQuery(
                            "select c from device_amphiro_config_default c", DeviceAmphiroDefaultConfigurationEntity.class)
                            .setFirstResult(0);

            configurations = configQuery.getResultList();

        } catch (Exception ex) {
            throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
        }

        return configurations;
    }

    @Override
    public UUID createMeterDevice(String username, String serial, List<KeyValuePair> properties, Geometry location) throws ApplicationException {
        UUID deviceKey = null;

        // Get user
        TypedQuery<AccountEntity> accountQuery = entityManager.createQuery(
                        "select a from account a where a.username = :username",
                        AccountEntity.class).setFirstResult(0).setMaxResults(1);

        accountQuery.setParameter("username", username);

        List<AccountEntity> accounts = accountQuery.getResultList();

        // If user exists add meter
        if (accounts.size() == 1) {
            AccountEntity account = accounts.get(0);

            // Check if a meter already exists
            DeviceRegistrationQuery meterQuery = new DeviceRegistrationQuery();
            meterQuery.setType(EnumDeviceType.METER);

            List<Device> meters = getUserDevices(account.getKey(), meterQuery);

            if (!meters.isEmpty()) {
                throw createApplicationException(DeviceErrorCode.ALREADY_EXISTS).set("id", serial);
            }

            // Update account location if not already set
            if (account.getLocation() == null) {
                account.setLocation(location);
            }

            // Create new device
            DeviceMeterEntity meter = new DeviceMeterEntity();
            meter.setSerial(serial);
            meter.setLocation(location);
            meter.setRegisteredOn(new DateTime());

            if (properties != null) {
                for (KeyValuePair p : properties) {
                    meter.getProperties().add(new DevicePropertyEntity(p.getKey(), p.getValue()));
                }
            }

            account.getDevices().add(meter);

            entityManager.persist(account);

            deviceKey = meter.getKey();
        } else if (enforceWhiteListCheck) {
            // If the user does not exist, update white list
            TypedQuery<AccountWhiteListEntity> entryQuery = entityManager.createQuery(
                            "select a from account_white_list a where a.username = :username",
                            AccountWhiteListEntity.class).setFirstResult(0).setMaxResults(1);

            entryQuery.setParameter("username", username);

            List<AccountWhiteListEntity> entries = entryQuery.getResultList();

            if (entries.size() == 1) {
                AccountWhiteListEntity entry = entries.get(0);

                entry.setMeterSerial(serial);
                entry.setMeterLocation(location);

                if (entry.getLocation() == null) {
                    entry.setLocation(location);
                }
                entityManager.persist(entry);
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

        TypedQuery<AccountEntity> accountQuery = entityManager.createQuery(
                        "select a from account a where a.username = :username and a.utility.id = :utility_id",
                        AccountEntity.class).setFirstResult(0).setMaxResults(1);

        accountQuery.setParameter("username", username);
        accountQuery.setParameter("utility_id", user.getUtilityId());

        List<AccountEntity> accounts = accountQuery.getResultList();

        if (accounts.size() == 1) {
            // Update account location if not already set
            if (accounts.get(0).getLocation() == null) {
                accounts.get(0).setLocation(location);
            }

            // Update device location
            TypedQuery<DeviceMeterEntity> query = entityManager.createQuery(
                            "select d from device_meter d where d.serial = :serial and d.account.key = :userKey",
                            DeviceMeterEntity.class).setFirstResult(0).setMaxResults(1);
            query.setParameter("serial", serial);
            query.setParameter("userKey", accounts.get(0).getKey());

            List<DeviceMeterEntity> result = query.getResultList();

            if (result.size() == 1) {
                result.get(0).setLocation(location);

                entityManager.flush();
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
            TypedQuery<DeviceEntity> query = entityManager.createQuery(
                            "select d from device d where d.key = :device_key and d.account.key = :user_key",
                            DeviceEntity.class).setFirstResult(0).setMaxResults(1);
            query.setParameter("user_key", userKey);
            query.setParameter("device_key", deviceKey);

            List<DeviceEntity> result = query.getResultList();

            if (result.size() == 1) {
                DeviceEntity entity = result.get(0);

                switch (entity.getType()) {
                    case AMPHIRO:
                        DeviceAmphiroEntity amphiroEntity = (DeviceAmphiroEntity) entity;

                        AmphiroDevice amphiro = new AmphiroDevice(amphiroEntity.getId(), amphiroEntity.getKey(),
                                        amphiroEntity.getName(), amphiroEntity.getMacAddress(), amphiroEntity
                                                        .getAesKey(), entity.getRegisteredOn().getMillis());

                        for (DevicePropertyEntity p : amphiroEntity.getProperties()) {
                            amphiro.getProperties().add(new KeyValuePair(p.getKey(), p.getValue()));
                        }


                        // Add default properties
                        for(String key : defaultAmphiroProperties.getProperties().keySet()){
                            if(amphiro.getProperty(key) == null) {
                                amphiro.getProperties().add(new KeyValuePair(key, defaultAmphiroProperties.getProperties().get(key)));
                            }
                        }

                        return amphiro;
                    case METER:
                        DeviceMeterEntity meterEntity = (DeviceMeterEntity) entity;

                        WaterMeterDevice meter = new WaterMeterDevice(meterEntity.getAccount().getId(), meterEntity
                                        .getKey(), meterEntity.getSerial(), meterEntity.getLocation(), entity
                                        .getRegisteredOn().getMillis());

                        for (DevicePropertyEntity p : meterEntity.getProperties()) {
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

            TypedQuery<DeviceEntity> query = entityManager.createQuery(
                            "select d from device d "
                                            + "where d.key = :device_key and d.account.utility.id = :utility_id",
                            DeviceEntity.class).setFirstResult(0).setMaxResults(1);
            query.setParameter("utility_id", user.getUtilityId());
            query.setParameter("device_key", deviceKey);

            List<DeviceEntity> result = query.getResultList();

            if (result.size() == 1) {
                DeviceEntity entity = result.get(0);

                switch (entity.getType()) {
                    case AMPHIRO:
                        DeviceAmphiroEntity amphiroEntity = (DeviceAmphiroEntity) entity;

                        AmphiroDevice amphiro = new AmphiroDevice(amphiroEntity.getId(), amphiroEntity.getKey(),
                                        amphiroEntity.getName(), amphiroEntity.getMacAddress(), amphiroEntity
                                                        .getAesKey(), entity.getRegisteredOn().getMillis());

                        for (DevicePropertyEntity p : amphiroEntity.getProperties()) {
                            amphiro.getProperties().add(new KeyValuePair(p.getKey(), p.getValue()));
                        }

                        // Add default properties
                        for(String key : defaultAmphiroProperties.getProperties().keySet()){
                            if(amphiro.getProperty(key) == null) {
                                amphiro.getProperties().add(new KeyValuePair(key, defaultAmphiroProperties.getProperties().get(key)));
                            }
                        }

                        return amphiro;
                    case METER:
                        DeviceMeterEntity meterEntity = (DeviceMeterEntity) entity;

                        WaterMeterDevice meter = new WaterMeterDevice(meterEntity.getAccount().getId(), meterEntity
                                        .getKey(), meterEntity.getSerial(), meterEntity.getLocation(), entity
                                        .getRegisteredOn().getMillis());

                        for (DevicePropertyEntity p : meterEntity.getProperties()) {
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
        TypedQuery<DeviceMeterEntity> meterQuery = entityManager.createQuery("select d from device_meter d "
                        + "where d.key = :device_key and d.account.key = :user_key", DeviceMeterEntity.class);

        meterQuery.setParameter("user_key", userKey);
        meterQuery.setParameter("device_key", deviceKey);

        List<DeviceMeterEntity> meters = meterQuery.getResultList();

        if (!meters.isEmpty()) {
            DeviceMeterEntity meterEntity = meters.get(0);

            WaterMeterDevice meter = new WaterMeterDevice(meterEntity.getAccount().getId(), meterEntity.getKey(),
                            meterEntity.getSerial(), meterEntity.getLocation(), meterEntity.getRegisteredOn()
                                            .getMillis());

            for (DevicePropertyEntity p : meterEntity.getProperties()) {
                meter.getProperties().add(new KeyValuePair(p.getKey(), p.getValue()));
            }

            return meter;
        }

        return null;
    }

    @Override
    public List<Device> getUserDevices(UUID userKey, DeviceRegistrationQuery query) throws ApplicationException {
        List<Device> devices = new ArrayList<Device>();

        try {
            TypedQuery<DeviceEntity> typedQuery = entityManager.createQuery(
                            "select d from device d where d.account.key = :user_key order by d.registeredOn",
                            DeviceEntity.class).setFirstResult(0);
            typedQuery.setParameter("user_key", userKey);

            List<DeviceEntity> result = typedQuery.getResultList();

            for (DeviceEntity entity : result) {
                switch (entity.getType()) {
                    case AMPHIRO:
                        if ((query.getType() == EnumDeviceType.UNDEFINED) || (query.getType() == entity.getType())) {
                            DeviceAmphiroEntity amphiroEntity = (DeviceAmphiroEntity) entity;

                            AmphiroDevice amphiro = new AmphiroDevice(amphiroEntity.getId(), amphiroEntity.getKey(),
                                            amphiroEntity.getName(), amphiroEntity.getMacAddress(), amphiroEntity
                                                            .getAesKey(), entity.getRegisteredOn().getMillis());

                            for (DevicePropertyEntity p : amphiroEntity.getProperties()) {
                                amphiro.getProperties().add(new KeyValuePair(p.getKey(), p.getValue()));
                            }

                            // Add default properties
                            for(String key : defaultAmphiroProperties.getProperties().keySet()){
                                if(amphiro.getProperty(key) == null) {
                                    amphiro.getProperties().add(new KeyValuePair(key, defaultAmphiroProperties.getProperties().get(key)));
                                }
                            }

                            for (DeviceAmphiroConfigurationEntity c : amphiroEntity.getConfigurations()) {
                                if (c.isActive()) {
                                    amphiro.setConfiguration(new eu.daiad.web.model.device.DeviceAmphiroConfiguration(c));
                                    break;
                                }
                            }

                            devices.add(amphiro);
                        }
                        break;
                    case METER:
                        if ((query.getType() == EnumDeviceType.UNDEFINED) || (query.getType() == entity.getType())) {
                            DeviceMeterEntity meterEntity = (DeviceMeterEntity) entity;

                            WaterMeterDevice meter = new WaterMeterDevice(meterEntity.getAccount().getId(), meterEntity
                                            .getKey(), meterEntity.getSerial(), meterEntity.getLocation(), entity
                                            .getRegisteredOn().getMillis());

                            for (DevicePropertyEntity p : meterEntity.getProperties()) {
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
            TypedQuery<DeviceAmphiroEntity> query = entityManager.createQuery(
                            "select d from device_amphiro d where d.macAddress = :macAddress and d.account.key = :key",
                            DeviceAmphiroEntity.class).setFirstResult(0).setMaxResults(1);
            query.setParameter("key", userKey);
            query.setParameter("macAddress", macAddress);

            List<DeviceAmphiroEntity> result = query.getResultList();

            if (result.size() == 1) {
                DeviceAmphiroEntity entity = result.get(0);

                AmphiroDevice amphiro = new AmphiroDevice(entity.getId(), entity.getKey(), entity.getName(),
                                entity.getMacAddress(), entity.getAesKey(), entity.getRegisteredOn().getMillis());

                for (DevicePropertyEntity p : entity.getProperties()) {
                    amphiro.getProperties().add(new KeyValuePair(p.getKey(), p.getValue()));
                }

                // Add default properties
                for(String key : defaultAmphiroProperties.getProperties().keySet()){
                    if(amphiro.getProperty(key) == null) {
                        amphiro.getProperties().add(new KeyValuePair(key, defaultAmphiroProperties.getProperties().get(key)));
                    }
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
            TypedQuery<DeviceMeterEntity> query = entityManager.createQuery(
                            "select d from device_meter d where d.serial = :serial and d.account.key = :userKey",
                            DeviceMeterEntity.class).setFirstResult(0).setMaxResults(1);
            query.setParameter("serial", serial);
            query.setParameter("userKey", userKey);

            List<DeviceMeterEntity> result = query.getResultList();

            if (result.size() == 1) {
                DeviceMeterEntity entity = result.get(0);

                WaterMeterDevice meter = new WaterMeterDevice(entity.getAccount().getId(), entity.getKey(), entity
                                .getSerial(), entity.getLocation(), entity.getRegisteredOn().getMillis());

                for (DevicePropertyEntity p : entity.getProperties()) {
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
            TypedQuery<DeviceMeterEntity> query = entityManager.createQuery(
                            "select d from device_meter d where d.serial = :serial ",
                            DeviceMeterEntity.class).setFirstResult(0).setMaxResults(1);
            query.setParameter("serial", serial);

            List<DeviceMeterEntity> result = query.getResultList();

            if (result.size() == 1) {
                DeviceMeterEntity entity = result.get(0);

                WaterMeterDevice meter = new WaterMeterDevice(entity.getAccount().getId(), entity.getKey(), entity
                                .getSerial(), entity.getLocation(), entity.getRegisteredOn().getMillis());

                for (DevicePropertyEntity p : entity.getProperties()) {
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
            TypedQuery<DeviceAmphiroEntity> deviceQuery = entityManager.createQuery(
                            "select d from device_amphiro d where d.key = :key",
                            DeviceAmphiroEntity.class).setFirstResult(0).setMaxResults(1);
            deviceQuery.setParameter("key", deviceKey);

            List<DeviceAmphiroEntity> devices = deviceQuery.getResultList();

            if (devices.size() != 1) {
                throw createApplicationException(DeviceErrorCode.NOT_FOUND).set("key", deviceKey.toString());
            }

            // Check owner
            DeviceAmphiroEntity device = devices.get(0);

            if (!device.getAccount().getKey().equals(ownerID)) {
                throw createApplicationException(SharedErrorCode.AUTHORIZATION);
            }

            // Get assignee
            TypedQuery<AccountEntity> userQuery = entityManager.createQuery(
                            "select a from account a where a.username = :username and a.utility.id = :utility_id",
                            AccountEntity.class).setFirstResult(0).setMaxResults(1);
            userQuery.setParameter("username", assigneeUsername);
            userQuery.setParameter("utility_id", device.getAccount().getUtility().getId());

            List<AccountEntity> users = userQuery.getResultList();

            if (users.size() == 0) {
                throw createApplicationException(UserErrorCode.USERNANE_NOT_FOUND).set("username", assigneeUsername);
            }

            AccountEntity assignee = users.get(0);

            if (assignee.getId() == device.getAccount().getId()) {
                return;
            }

            DeviceAmphiroPermissionEntity permission = null;

            TypedQuery<DeviceAmphiroPermissionEntity> permissionQuery = entityManager
                            .createQuery("select p from device_amphiro_permission p where p.device.id = :deviceId and p.owner.id = :ownerId and p.assignee.id = :assigneeId",
                                            DeviceAmphiroPermissionEntity.class)
                            .setFirstResult(0).setMaxResults(1);
            permissionQuery.setParameter("deviceId", device.getId());
            permissionQuery.setParameter("ownerId", device.getAccount().getId());
            permissionQuery.setParameter("assigneeId", assignee.getId());

            List<DeviceAmphiroPermissionEntity> permissions = permissionQuery.getResultList();
            if (permissions.size() == 1) {
                permission = permissions.get(0);
            }

            if (shared) {
                if (permission == null) {
                    permission = new DeviceAmphiroPermissionEntity();
                    permission.setDevice(device);
                    permission.setOwner(device.getAccount());
                    permission.setAssignee(assignee);
                    permission.setAssignedOn(DateTime.now());

                    entityManager.persist(permission);
                }
            } else {
                if (permission != null) {
                    entityManager.remove(permission);
                }
            }

        } catch (Exception ex) {
            throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
        }
    }

    @Override
    public List<DeviceConfigurationCollection> getConfiguration(UUID userKey, UUID deviceKeys[]) throws ApplicationException {
        List<DeviceConfigurationCollection> collections = new ArrayList<DeviceConfigurationCollection>();
        try {
            for (UUID deviceKey : deviceKeys) {
                TypedQuery<DeviceAmphiroEntity> deviceQuery = entityManager.createQuery(
                                "select d from device_amphiro d where d.key = :deviceKey and d.account.key = :userKey",
                                DeviceAmphiroEntity.class).setFirstResult(0).setMaxResults(1);
                deviceQuery.setParameter("deviceKey", deviceKey);
                deviceQuery.setParameter("userKey", userKey);

                List<DeviceAmphiroEntity> devices = deviceQuery.getResultList();

                if (devices.size() != 1) {
                    throw createApplicationException(DeviceErrorCode.NOT_FOUND).set("key", deviceKey.toString());
                }

                DeviceAmphiroEntity device = devices.get(0);

                DeviceConfigurationCollection deviceConfigurationCollection = new DeviceConfigurationCollection();
                deviceConfigurationCollection.setKey(device.getKey());
                deviceConfigurationCollection.setMacAddress(device.getMacAddress());
                deviceConfigurationCollection.setAesKey(device.getAesKey());
                deviceConfigurationCollection.setName(device.getName());
                deviceConfigurationCollection.setRegisteredOn(device.getRegisteredOn().getMillis());

                for (DevicePropertyEntity p : device.getProperties()) {
                    deviceConfigurationCollection.getProperties().add(new KeyValuePair(p.getKey(), p.getValue()));
                }

                // Add default properties
                for(String key : defaultAmphiroProperties.getProperties().keySet()){
                    if(deviceConfigurationCollection.getProperty(key) == null) {
                        deviceConfigurationCollection.getProperties().add(new KeyValuePair(key, defaultAmphiroProperties.getProperties().get(key)));
                    }
                }

                for (DeviceAmphiroConfigurationEntity c : device.getConfigurations()) {
                    if (c.isActive()) {
                        eu.daiad.web.model.device.DeviceAmphiroConfiguration configuration = new eu.daiad.web.model.device.DeviceAmphiroConfiguration(
                                        c);

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

            TypedQuery<DeviceAmphiroEntity> deviceQuery = entityManager.createQuery(
                            "select d from device_amphiro d where d.key = :deviceKey and d.account.key = :userKey",
                            DeviceAmphiroEntity.class).setFirstResult(0).setMaxResults(1);
            deviceQuery.setParameter("deviceKey", deviceKey);
            deviceQuery.setParameter("userKey", userKey);

            List<DeviceAmphiroEntity> devices = deviceQuery.getResultList();

            if (devices.size() != 1) {
                throw createApplicationException(DeviceErrorCode.NOT_FOUND).set("key", deviceKey.toString());
            }

            DeviceAmphiroEntity device = devices.get(0);

            for (DeviceAmphiroConfigurationEntity c : device.getConfigurations()) {
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
            TypedQuery<DeviceEntity> deviceQuery = entityManager.createQuery(
                            "select d from device d where d.key = :device_key",
                            DeviceEntity.class).setFirstResult(0).setMaxResults(1);

            deviceQuery.setParameter("device_key", deviceKey);

            List<DeviceEntity> result = deviceQuery.getResultList();

            if (result.size() != 1) {
                throw createApplicationException(DeviceErrorCode.NOT_FOUND).set("key", deviceKey);
            }

            for (DeviceEntity d : result) {
                switch (d.getType()) {
                    case AMPHIRO:
                    case METER:
                        d.getAccount().getDevices().remove(d);
                        entityManager.remove(d);
                        break;
                    default:
                        throw createApplicationException(DeviceErrorCode.NOT_SUPPORTED).set("type", d.getType());
                }
            }
        } catch (Exception ex) {
            throw wrapApplicationException(ex, SharedErrorCode.UNKNOWN);
        }
    }

    @Override
    public void setLastDataUploadDate(UUID userKey, UUID deviceKey, DateTime when, boolean success) {
        try {
            TypedQuery<DeviceEntity> query = entityManager.createQuery(
                            "select d from device d where d.key = :device_key and d.account.key = :user_key",
                            DeviceEntity.class).setFirstResult(0).setMaxResults(1);
            query.setParameter("user_key", userKey);
            query.setParameter("device_key", deviceKey);

            List<DeviceEntity> result = query.getResultList();

            if (result.size() == 1) {
                DeviceEntity entity = result.get(0);

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

    @Override
    public void updateDevice(UUID userKey, DeviceUpdateRequest request) {
        for (DeviceUpdate update : request.getUpdates()) {
            switch (update.getType()) {
                case AMPHIRO:
                    AmphiroDeviceUpdate amphiroUpdate = (AmphiroDeviceUpdate) update;

                    // Get device
                    TypedQuery<DeviceEntity> query = entityManager.createQuery(
                                    "select d from device d where d.key = :device_key and d.account.key = :user_key",
                                    DeviceEntity.class).setFirstResult(0).setMaxResults(1);
                    query.setParameter("user_key", userKey);
                    query.setParameter("device_key", update.getKey());

                    List<DeviceEntity> result = query.getResultList();

                    if (result.size() != 1) {
                        throw createApplicationException(DeviceErrorCode.NOT_FOUND).set("key",
                                        update.getKey().toString());
                    } else {
                        // Set new values
                        DeviceAmphiroEntity entity = (DeviceAmphiroEntity) result
                                        .get(0);

                        entity.setName(amphiroUpdate.getName());

                        for (String key : defaultAmphiroProperties.getProperties().keySet()) {
                            KeyValuePair updateProperty = update.getProperty(key);

                            if (updateProperty != null) {
                                DevicePropertyEntity entityProperty = entity.getProperty(key);

                                if (entityProperty == null) {
                                    entity.getProperties().add(new DevicePropertyEntity(key, updateProperty.getValue()));
                                } else {
                                    entityProperty.setValue(updateProperty.getValue());
                                }
                            }
                        }
                    }
                    break;
                default:
                    throw createApplicationException(DeviceErrorCode.NOT_SUPPORTED).set("type",
                                    update.getType().toString());
            }
        }
    }


    @Override
    public List<eu.daiad.web.model.device.DeviceAmphiroConfiguration> getDeviceConfigurationHistory(UUID deviceKey) {
        List<eu.daiad.web.model.device.DeviceAmphiroConfiguration> configurations = new ArrayList<eu.daiad.web.model.device.DeviceAmphiroConfiguration>();

        String sqlString = "select d from device_amphiro d where d.key = :deviceKey";

        TypedQuery<DeviceAmphiroEntity> deviceQuery = entityManager.createQuery(sqlString, DeviceAmphiroEntity.class)
                                                             .setFirstResult(0)
                                                             .setMaxResults(1);

        deviceQuery.setParameter("deviceKey", deviceKey);

        DeviceAmphiroEntity device = deviceQuery.getSingleResult();

        for (DeviceAmphiroConfigurationEntity c : device.getConfigurations()) {
            eu.daiad.web.model.device.DeviceAmphiroConfiguration configuration = new eu.daiad.web.model.device.DeviceAmphiroConfiguration(c);

            configurations.add(configuration);
        }

        Collections.sort(configurations, new Comparator<eu.daiad.web.model.device.DeviceAmphiroConfiguration>() {

            @Override
            public int compare(eu.daiad.web.model.device.DeviceAmphiroConfiguration c1,
                               eu.daiad.web.model.device.DeviceAmphiroConfiguration c2) {
                if (c1.getCreatedOn() == c2.getCreatedOn()) {
                    throw new RuntimeException("Configuration creation timestamp must be unique.");
                } else if (c1.getCreatedOn() < c2.getCreatedOn()) {
                    return -1;
                } else {
                    return 1;
                }
            }

        });

        return configurations;
    }

}
