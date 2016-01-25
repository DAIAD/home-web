package eu.daiad.web.domain;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity(name = "device_amphiro")
@Table(schema = "public", name = "device_amphiro")
public class DeviceAmphiro extends Device {

	@Column(name = "mac_address")
	private String macAddress;

	@Basic()
	private String name;

	public String getMacAddress() {
		return macAddress;
	}

	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
