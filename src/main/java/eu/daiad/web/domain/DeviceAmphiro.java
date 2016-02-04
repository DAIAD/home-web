package eu.daiad.web.domain;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import eu.daiad.web.model.device.EnumDeviceType;

@Entity(name = "device_amphiro")
@Table(schema = "public", name = "device_amphiro")
public class DeviceAmphiro extends Device {

	@Column(name = "mac_address")
	private String macAddress;

	@Basic()
	private String name;

	@Column(name = "aes_key")
	private String aesKey;

	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
	@JoinColumn(name = "device_id")
	private Set<DeviceAmphiroConfiguration> configurations = new HashSet<DeviceAmphiroConfiguration>();

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

	public EnumDeviceType getType() {
		return EnumDeviceType.AMPHIRO;
	}

	public String getAesKey() {
		return aesKey;
	}

	public void setAesKey(String aesKey) {
		this.aesKey = aesKey;
	}

	public Set<DeviceAmphiroConfiguration> getConfigurations() {
		return configurations;
	}

}
