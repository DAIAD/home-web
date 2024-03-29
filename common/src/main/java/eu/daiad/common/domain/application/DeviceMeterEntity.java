package eu.daiad.common.domain.application;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.locationtech.jts.geom.Geometry;

import eu.daiad.common.model.device.EnumDeviceType;

@Entity(name = "device_meter")
@Table(schema = "public", name = "device_meter")
public class DeviceMeterEntity extends DeviceEntity {

	@Basic()
	private String serial;

	@Column(name = "location")
	private Geometry location;

	public String getSerial() {
		return serial;
	}

	public void setSerial(String serial) {
		this.serial = serial;
	}

	public EnumDeviceType getType() {
		return EnumDeviceType.METER;
	}

	public Geometry getLocation() {
		return location;
	}

	public void setLocation(Geometry location) {
		this.location = location;
	}

}
