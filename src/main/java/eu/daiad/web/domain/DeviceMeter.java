package eu.daiad.web.domain;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Table;

import eu.daiad.web.model.device.EnumDeviceType;

@Entity(name = "device_meter")
@Table(schema = "public", name = "device_meter")
public class DeviceMeter extends Device {

	@Basic()
	private String serial;

	public String getSerial() {
		return serial;
	}

	public void setSerial(String serial) {
		this.serial = serial;
	}
	
	public EnumDeviceType getType() {
		return EnumDeviceType.METER;
	}
}
