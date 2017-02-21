package eu.daiad.web.repository.application;

import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;

import com.vividsolutions.jts.geom.Geometry;

import eu.daiad.web.domain.application.DeviceAmphiroDefaultConfigurationEntity;
import eu.daiad.web.model.KeyValuePair;
import eu.daiad.web.model.device.AmphiroDevice;
import eu.daiad.web.model.device.Device;
import eu.daiad.web.model.device.DeviceConfigurationCollection;
import eu.daiad.web.model.device.DeviceRegistrationQuery;
import eu.daiad.web.model.device.DeviceUpdateRequest;
import eu.daiad.web.model.device.EnumDeviceType;
import eu.daiad.web.model.device.WaterMeterDevice;
import eu.daiad.web.model.error.ApplicationException;

public interface IDeviceRepository {

    void removeDevice(UUID deviceKey);

    AmphiroDevice createAmphiroDevice(UUID userKey, String name, String macAddress, String aesKey, List<KeyValuePair> properties) throws ApplicationException;

    List<DeviceAmphiroDefaultConfigurationEntity> getAmphiroDefaultConfigurations() throws ApplicationException;

    UUID createMeterDevice(String username, String serial, List<KeyValuePair> properties, Geometry location) throws ApplicationException;

    void updateMeterLocation(String username, String serial, Geometry location) throws ApplicationException;

    Device getUserDeviceByKey(UUID userKey, UUID deviceKey) throws ApplicationException;

    WaterMeterDevice getUserWaterMeterByKey(UUID userKey, UUID deviceKey);

    Device getDeviceByKey(UUID deviceKey) throws ApplicationException;

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

    List<eu.daiad.web.model.device.DeviceAmphiroConfiguration> getDeviceConfigurationHistory(UUID deviceKey);
}
