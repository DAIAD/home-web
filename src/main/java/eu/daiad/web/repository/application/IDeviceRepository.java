package eu.daiad.web.repository.application;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;

import com.vividsolutions.jts.geom.Geometry;

import eu.daiad.web.domain.application.DeviceAmphiroConfigurationDefault;
import eu.daiad.web.model.KeyValuePair;
import eu.daiad.web.model.device.AmphiroDevice;
import eu.daiad.web.model.device.Device;
import eu.daiad.web.model.device.DeviceConfigurationCollection;
import eu.daiad.web.model.device.DeviceRegistrationQuery;
import eu.daiad.web.model.device.DeviceUpdateRequest;
import eu.daiad.web.model.device.WaterMeterDevice;
import eu.daiad.web.model.error.ApplicationException;

public interface IDeviceRepository {

    abstract void removeDevice(UUID deviceKey);

    abstract AmphiroDevice createAmphiroDevice(UUID userKey, String name, String macAddress, String aesKey,
                    ArrayList<KeyValuePair> properties) throws ApplicationException;

    List<DeviceAmphiroConfigurationDefault> getAmphiroDefaultConfigurations() throws ApplicationException;

    abstract UUID createMeterDevice(String username, String serial, ArrayList<KeyValuePair> properties,
                    Geometry location) throws ApplicationException;

    abstract void updateMeterLocation(String username, String serial, Geometry location) throws ApplicationException;

    abstract Device getUserDeviceByKey(UUID userKey, UUID deviceKey) throws ApplicationException;

    abstract WaterMeterDevice getUserWaterMeterByKey(UUID userKey, UUID deviceKey);

    abstract Device getDeviceByKey(UUID deviceKey) throws ApplicationException;

    abstract Device getUserAmphiroDeviceByMacAddress(UUID userKey, String macAddress) throws ApplicationException;

    abstract Device getUserWaterMeterDeviceBySerial(UUID userKey, String serial) throws ApplicationException;

    abstract Device getWaterMeterDeviceBySerial(String serial) throws ApplicationException;

    abstract ArrayList<Device> getUserDevices(UUID userKey, DeviceRegistrationQuery query) throws ApplicationException;

    abstract void shareDevice(UUID ownerID, String assigneeUsername, UUID deviceKey, boolean shared)
                    throws ApplicationException;

    abstract ArrayList<DeviceConfigurationCollection> getConfiguration(UUID userKey, UUID deviceKeys[])
                    throws ApplicationException;

    abstract void notifyConfiguration(UUID userKey, UUID deviceKey, UUID version, DateTime updatedOn)
                    throws ApplicationException;

    abstract void setLastDataUploadDate(UUID userKey, UUID deviceKey, DateTime when, boolean success);

    abstract void updateDevice(UUID userKey, DeviceUpdateRequest request);

}