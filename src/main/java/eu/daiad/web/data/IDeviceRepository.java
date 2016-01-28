package eu.daiad.web.data;

import java.util.ArrayList;
import java.util.UUID;

import eu.daiad.web.model.KeyValuePair;
import eu.daiad.web.model.device.Device;
import eu.daiad.web.model.device.DeviceRegistrationQuery;

public interface IDeviceRepository {

	public abstract UUID createAmphiroDevice(UUID userKey, String name,
			String macAddress, ArrayList<KeyValuePair> properties)
			throws Exception;

	public abstract UUID createMeterDevice(UUID userKey, String serial,
			ArrayList<KeyValuePair> properties) throws Exception;

	public abstract Device getUserDeviceByKey(UUID userKey,
			UUID deviceKey) throws Exception;

	public abstract Device getUserAmphiroDeviceByMacAddress(UUID userKey,
			String macAddress) throws Exception;

	public abstract Device getUserWaterMeterDeviceBySerial(UUID userKey,
			String serial) throws Exception;

	public abstract ArrayList<Device> getUserDevices(UUID userKey,
			DeviceRegistrationQuery query) throws Exception;

}