package eu.daiad.web.model.device;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

import com.vividsolutions.jts.geom.Geometry;

import eu.daiad.web.model.KeyValuePair;

public class WaterMeterDevice extends Device {

	private String serial;

	private Geometry location;

	public WaterMeterDevice(int ownerId, UUID key, String serial) {
		super(ownerId, key);

		this.serial = serial;
	}

	public WaterMeterDevice(int ownerId, UUID key, String serial, ArrayList<KeyValuePair> properties) {
		super(ownerId, key, properties);

		this.serial = serial;
	}

	public WaterMeterDevice(int ownerId, UUID key, String serial, Geometry location) {
		super(ownerId, key);

		this.serial = serial;
		this.location = location;
	}

	public WaterMeterDevice(int ownerId, UUID key, String serial, ArrayList<KeyValuePair> properties, Geometry location) {
		super(ownerId, key, properties);

		this.serial = serial;
		this.location = location;
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

		for (Iterator<KeyValuePair> p = this.getProperties().iterator(); p.hasNext();) {
			KeyValuePair property = p.next();
			r.getProperties().add(new KeyValuePair(property.getKey(), property.getValue()));
		}

		return r;
	}

	public Geometry getLocation() {
		return location;
	}

	public void setLocation(Geometry location) {
		this.location = location;
	}
}
