package eu.daiad.web.model.device;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

import eu.daiad.web.model.KeyValuePair;

public class WaterMeterDevice extends Device {

	private String serial;

	public WaterMeterDevice(UUID key, String serial) {
		super(key);

		this.serial = serial;
	}

	public WaterMeterDevice(UUID key, String serial,
			ArrayList<KeyValuePair> properties) {
		super(key, properties);

		this.serial = serial;
	}

	@Override
	public EnumDeviceType getType() {
		return EnumDeviceType.METER;
	}

	public String getSerial() {
		return serial;
	}

	public void setSerial(String serial) {
		this.serial = serial;
	}

	@Override
	public DeviceRegistration toDeviceRegistration() {
		WaterMeterDeviceRegistration r = new WaterMeterDeviceRegistration();

		r.setDeviceKey(this.getKey());
		r.setSerial(this.getSerial());

		for (Iterator<KeyValuePair> p = this.getProperties().iterator(); p
				.hasNext();) {
			KeyValuePair property = p.next();
			r.getProperties().add(
					new KeyValuePair(property.getKey(), property.getValue()));
		}

		return r;
	}
}
