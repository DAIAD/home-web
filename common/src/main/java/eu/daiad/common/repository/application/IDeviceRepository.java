package eu.daiad.common.repository.application;

import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;
import org.locationtech.jts.geom.Geometry;

import eu.daiad.common.domain.application.DeviceAmphiroDefaultConfigurationEntity;
import eu.daiad.common.model.KeyValuePair;
import eu.daiad.common.model.device.AmphiroDevice;
import eu.daiad.common.model.device.Device;
import eu.daiad.common.model.device.DeviceConfigurationCollection;
import eu.daiad.common.model.device.DeviceRegistrationQuery;
import eu.daiad.common.model.device.DeviceUpdateRequest;
import eu.daiad.common.model.device.EnumDeviceType;
import eu.daiad.common.model.device.WaterMeterDevice;
import eu.daiad.common.model.error.ApplicationException;

public interface IDeviceRepository {

    void removeDevice(UUID deviceKey);

    AmphiroDevice createAmphiroDevice(UUID userKey, String name, String macAddress, String aesKey, List<KeyValuePair> properties) throws ApplicationException;

    List<DeviceAmphiroDefaultConfigurationEntity> getAmphiroDefaultConfigurations() throws ApplicationException;

    UUID createMeterDevice(String username, String serial, List<KeyValuePair> properties, Geometry location) throws ApplicationException;

    void updateMeterLocation(String username, String serial, Geometry location) throws ApplicationException;

    Device getUserDeviceByKey(UUID userKey, UUID deviceKey) throws ApplicationException;

    WaterMeterDevice getUserWaterMeterByKey(UUID userKey, UUID deviceKey);

    Device getDeviceByKey(UUID deviceKey) throws ApplicationException;

    AmphiroDevice getUserAmphiroByKey(UUID userKey, UUID deviceKey);

    Device getUserAmphiroDeviceByMacAddress(UUID userKey, String macAddress) throws ApplicationException;

    Device getUserWaterMeterDeviceBySerial(UUID userKey, String serial) throws ApplicationException;

    Device getWaterMeterDeviceBySerial(String serial) throws ApplicationException;

    List<Device> getUserDevices(UUID userKey, DeviceRegistrationQuery query) throws ApplicationException;

    List<Device> getUserDevices(UUID userKey, EnumDeviceType deviceType) throws ApplicationException;

    void shareDevice(UUID ownerID, String assigneeUsername, UUID deviceKey, boolean shared) throws ApplicationException;

    List<DeviceConfigurationCollection> getConfiguration(UUID userKey, UUID deviceKeys[]) throws ApplicationException;

    void notifyConfiguration(UUID userKey, UUID deviceKey, UUID version, DateTime updatedOn) throws ApplicationException;

    void setLastDataUploadDate(UUID userKey, UUID deviceKey, DateTime when, boolean success);

    void updateDevice(UUID userKey, DeviceUpdateRequest request);

    List<eu.daiad.common.model.device.DeviceAmphiroConfiguration> getDeviceConfigurationHistory(UUID deviceKey);
}
