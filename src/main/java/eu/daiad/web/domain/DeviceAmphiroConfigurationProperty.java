package eu.daiad.web.domain;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity(name = "device_amphiro_config")
@Table(schema = "public", name = "device_amphiro_config")
public class DeviceAmphiroConfigurationProperty {

	@Id()
	@Column(name = "id")
	@SequenceGenerator(sequenceName = "device_amphiro_config_id_seq", name = "device_amphiro_config_id_seq", allocationSize = 1, initialValue = 1)
	@GeneratedValue(generator = "device_amphiro_config_id_seq", strategy = GenerationType.SEQUENCE)
	private int id;

	@ManyToOne(cascade = { CascadeType.ALL })
	@JoinColumn(name = "device_id", nullable = false)
	private DeviceAmphiro device;

	@Basic()
	private String key;

	@Basic()
	private String value;

	public DeviceAmphiro getDevice() {
		return device;
	}

	public void setDevice(DeviceAmphiro device) {
		this.device = device;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public int getId() {
		return id;
	}

}
