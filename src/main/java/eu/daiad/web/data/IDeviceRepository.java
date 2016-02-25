package eu.daiad.web.data;

import java.util.ArrayList;
import java.util.UUID;

import eu.daiad.web.model.KeyValuePair;
import eu.daiad.web.model.device.Device;
import eu.daiad.web.model.device.DeviceConfigurationCollection;
import eu.daiad.web.model.device.DeviceRegistrationQuery;
import eu.daiad.web.model.error.ApplicationException;

public interface IDeviceRepository {

	public abstract void removeDevice(UUID deviceKey);
	
	public abstract UUID createAmphiroDevice(UUID userKey, String name, String macAddress, String aesKey,
					ArrayList<KeyValuePair> properties) throws ApplicationException;

	public abstract UUID createMeterDevice(UUID userKey, String serial, ArrayList<KeyValuePair> properties)
					throws ApplicationException;

	public abstract Device getUserDeviceByKey(UUID userKey, UUID deviceKey) throws ApplicationException;

	public abstract Device getUserAmphiroDeviceByMacAddress(UUID userKey, String macAddress)
					throws ApplicationException;

	public abstract Device getUserWaterMeterDeviceBySerial(UUID userKey, String serial) throws ApplicationException;

	public abstract ArrayList<Device> getUserDevices(UUID userKey, DeviceRegistrationQuery query)
					throws ApplicationException;

	public abstract void shareDevice(UUID ownerID, String assigneeUsername, UUID deviceKey, boolean shared)
					throws ApplicationException;

	public abstract ArrayList<DeviceConfigurationCollection> getConfiguration(UUID userKey, UUID deviceKeys[])
					throws ApplicationException;

}