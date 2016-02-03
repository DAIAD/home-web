package eu.daiad.web.data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

import eu.daiad.web.domain.DeviceProperty;
import eu.daiad.web.model.KeyValuePair;
import eu.daiad.web.model.device.AmphiroDevice;
import eu.daiad.web.model.device.Device;
import eu.daiad.web.model.device.DeviceRegistrationQuery;
import eu.daiad.web.model.device.EnumDeviceType;
import eu.daiad.web.model.device.WaterMeterDevice;
import eu.daiad.web.model.error.ApplicationException;
import eu.daiad.web.model.error.SharedErrorCode;

@Primary
@Repository()
@Transactional()
@Scope("prototype")
public class JpaDeviceRepository implements IDeviceRepository {

	@Autowired
	EntityManager entityManager;

	@Override
	public UUID createAmphiroDevice(UUID userKey, String name, String macAddress, ArrayList<KeyValuePair> properties)
					throws ApplicationException {
		UUID deviceKey = null;

		try {
			TypedQuery<eu.daiad.web.domain.Account> query = entityManager
							.createQuery("select a from account a where a.key = :key",
											eu.daiad.web.domain.Account.class).setFirstResult(0).setMaxResults(1);
			query.setParameter("key", userKey);

			eu.daiad.web.domain.Account account = query.getSingleResult();

			eu.daiad.web.domain.DeviceAmphiro amphiro = new eu.daiad.web.domain.DeviceAmphiro();
			amphiro.setName(name);
			amphiro.setMacAddress(macAddress);

			for (KeyValuePair p : properties) {
				amphiro.getProperties().add(new DeviceProperty(p.getKey(), p.getValue()));
			}

			account.getDevices().add(amphiro);

			this.entityManager.persist(account);

			deviceKey = amphiro.getKey();
		} catch (Exception ex) {
			throw ApplicationException.wrap(ex, SharedErrorCode.UNKNOWN);
		}

		return deviceKey;
	}

	@Override
	public UUID createMeterDevice(UUID userKey, String serial, ArrayList<KeyValuePair> properties)
					throws ApplicationException {
		UUID deviceKey = null;

		try {
			TypedQuery<eu.daiad.web.domain.Account> query = entityManager
							.createQuery("select a from account a where a.key = :key",
											eu.daiad.web.domain.Account.class).setFirstResult(0).setMaxResults(1);
			query.setParameter("key", userKey);

			eu.daiad.web.domain.Account account = query.getSingleResult();

			eu.daiad.web.domain.DeviceMeter meter = new eu.daiad.web.domain.DeviceMeter();
			meter.setSerial(serial);

			for (KeyValuePair p : properties) {
				meter.getProperties().add(new DeviceProperty(p.getKey(), p.getValue()));
			}

			account.getDevices().add(meter);

			this.entityManager.persist(account);

			deviceKey = meter.getKey();
		} catch (Exception ex) {
			throw ApplicationException.wrap(ex, SharedErrorCode.UNKNOWN);
		}

		return deviceKey;
	}

	@Override
	public Device getUserDeviceByKey(UUID userKey, UUID deviceKey) throws ApplicationException {
		try {
			TypedQuery<eu.daiad.web.domain.Device> query = entityManager
							.createQuery("select d from device d where d.key = :device_key and d.account.key = :user_key",
											eu.daiad.web.domain.Device.class).setFirstResult(0).setMaxResults(1);
			query.setParameter("user_key", userKey);
			query.setParameter("device_key", deviceKey);

			List<eu.daiad.web.domain.Device> result = query.getResultList();

			if (result.size() == 1) {
				eu.daiad.web.domain.Device entity = result.get(0);

				switch (entity.getType()) {
				case AMPHIRO:
					eu.daiad.web.domain.DeviceAmphiro amphiroEntiry = (eu.daiad.web.domain.DeviceAmphiro) entity;

					AmphiroDevice amphiro = new AmphiroDevice(amphiroEntiry.getKey(), amphiroEntiry.getName(),
									amphiroEntiry.getMacAddress());

					for (eu.daiad.web.domain.DeviceProperty p : amphiroEntiry.getProperties()) {
						amphiro.getProperties().add(new KeyValuePair(p.getKey(), p.getValue()));
					}

					return amphiro;
				case METER:
					eu.daiad.web.domain.DeviceMeter meterEntiry = (eu.daiad.web.domain.DeviceMeter) entity;

					WaterMeterDevice meter = new WaterMeterDevice(meterEntiry.getKey(), meterEntiry.getSerial());

					for (eu.daiad.web.domain.DeviceProperty p : meterEntiry.getProperties()) {
						meter.getProperties().add(new KeyValuePair(p.getKey(), p.getValue()));
					}

					return meter;
				default:
					break;
				}

			}

			return null;
		} catch (Exception ex) {
			throw ApplicationException.wrap(ex, SharedErrorCode.UNKNOWN);
		}
	}

	@Override
	public ArrayList<Device> getUserDevices(UUID userKey, DeviceRegistrationQuery query) throws ApplicationException {
		ArrayList<Device> devices = new ArrayList<Device>();

		try {
			TypedQuery<eu.daiad.web.domain.Device> typedQuery = entityManager.createQuery(
							"select d from device d where d.account.key = :user_key", eu.daiad.web.domain.Device.class)
							.setFirstResult(0);
			typedQuery.setParameter("user_key", userKey);

			List<eu.daiad.web.domain.Device> result = typedQuery.getResultList();

			for (eu.daiad.web.domain.Device entity : result) {
				switch (entity.getType()) {
				case AMPHIRO:
					if ((query.getType() == EnumDeviceType.UNDEFINED) || (query.getType() == entity.getType())) {
						eu.daiad.web.domain.DeviceAmphiro amphiroEntiry = (eu.daiad.web.domain.DeviceAmphiro) entity;

						AmphiroDevice amphiro = new AmphiroDevice(amphiroEntiry.getKey(), amphiroEntiry.getName(),
										amphiroEntiry.getMacAddress());

						for (eu.daiad.web.domain.DeviceProperty p : amphiroEntiry.getProperties()) {
							amphiro.getProperties().add(new KeyValuePair(p.getKey(), p.getValue()));
						}

						devices.add(amphiro);
					}
					break;
				case METER:
					if ((query.getType() == EnumDeviceType.UNDEFINED) || (query.getType() == entity.getType())) {
						eu.daiad.web.domain.DeviceMeter meterEntiry = (eu.daiad.web.domain.DeviceMeter) entity;

						WaterMeterDevice meter = new WaterMeterDevice(meterEntiry.getKey(), meterEntiry.getSerial());

						for (eu.daiad.web.domain.DeviceProperty p : meterEntiry.getProperties()) {
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
			throw ApplicationException.wrap(ex, SharedErrorCode.UNKNOWN);
		}

		return devices;
	}

	@Override
	public Device getUserAmphiroDeviceByMacAddress(UUID userKey, String macAddress) throws ApplicationException {
		try {
			TypedQuery<eu.daiad.web.domain.DeviceAmphiro> query = entityManager
							.createQuery("select d from device_amphiro d where d.macAddress = :macAddress",
											eu.daiad.web.domain.DeviceAmphiro.class).setFirstResult(0).setMaxResults(1);
			query.setParameter("macAddress", macAddress);

			List<eu.daiad.web.domain.DeviceAmphiro> result = query.getResultList();

			if (result.size() == 1) {
				eu.daiad.web.domain.DeviceAmphiro entity = result.get(0);

				AmphiroDevice amphiro = new AmphiroDevice(entity.getKey(), entity.getName(), entity.getMacAddress());

				for (eu.daiad.web.domain.DeviceProperty p : entity.getProperties()) {
					amphiro.getProperties().add(new KeyValuePair(p.getKey(), p.getValue()));
				}

				return amphiro;
			}

			return null;
		} catch (Exception ex) {
			throw ApplicationException.wrap(ex, SharedErrorCode.UNKNOWN);
		}
	}

	@Override
	public Device getUserWaterMeterDeviceBySerial(UUID userKey, String serial) throws ApplicationException {
		try {
			TypedQuery<eu.daiad.web.domain.DeviceMeter> query = entityManager
							.createQuery("select d from device_meter d where d.serial = :serial",
											eu.daiad.web.domain.DeviceMeter.class).setFirstResult(0).setMaxResults(1);
			query.setParameter("serial", serial);

			List<eu.daiad.web.domain.DeviceMeter> result = query.getResultList();

			if (result.size() == 1) {
				eu.daiad.web.domain.DeviceMeter entity = result.get(0);

				WaterMeterDevice meter = new WaterMeterDevice(entity.getKey(), entity.getSerial());

				for (eu.daiad.web.domain.DeviceProperty p : entity.getProperties()) {
					meter.getProperties().add(new KeyValuePair(p.getKey(), p.getValue()));
				}

				return meter;

			}

			return null;
		} catch (Exception ex) {
			throw ApplicationException.wrap(ex, SharedErrorCode.UNKNOWN);
		}
	}
}