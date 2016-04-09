package eu.daiad.web.model.device;

import java.util.UUID;

import com.vividsolutions.jts.geom.Geometry;

public class WaterMeterDeviceRegistrationRequest extends DeviceRegistrationRequest {

	private UUID userKey;

	private String serial;

	private Geometry location;

	public String getSerial() {
		return serial;
	}

	public void setSerial(String serial) {
		this.serial = serial;
	}

	@Override
	public EnumDeviceType getType() {
		return EnumDeviceType.METER;
	}

	public UUID getUserKey() {
		return userKey;
	}

	public void setUserKey(UUID userKey) {
		this.userKey = userKey;
	}

	public Geometry getLocation() {
		return location;
	}

	public void setLocation(Geometry location) {
		this.location = location;
	}
}
