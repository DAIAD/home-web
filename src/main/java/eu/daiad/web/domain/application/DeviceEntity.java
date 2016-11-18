package eu.daiad.web.domain.application;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import eu.daiad.web.model.device.EnumDeviceType;

@Entity(name = "device")
@Table(schema = "public", name = "device")
@Inheritance(strategy = InheritanceType.JOINED)
public class DeviceEntity {

	@Id()
	@Column(name = "id")
	@SequenceGenerator(sequenceName = "device_id_seq", name = "device_id_seq", allocationSize = 1, initialValue = 1)
	@GeneratedValue(generator = "device_id_seq", strategy = GenerationType.SEQUENCE)
	private int id;

	@Version()
	@Column(name = "row_version")
	private long rowVersion;

	@ManyToOne(cascade = { CascadeType.ALL })
	@JoinColumn(name = "account_id", nullable = false)
	private AccountEntity account;

	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
	@JoinColumn(name = "device_id")
	private Set<DevicePropertyEntity> properties = new HashSet<DevicePropertyEntity>();

	@Column()
	@Type(type = "pg-uuid")
	private UUID key = UUID.randomUUID();

	@Column(name = "registered_on")
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime registeredOn;

	@Column(name = "last_upload_success_on")
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime lastDataUploadSuccess;

	@Column(name = "transmission_count")
	private Long transmissionCount;

	@Column(name = "transmission_interval_sum")
	private Long transmissionIntervalSum;

	@Column(name = "transmission_interval_max")
	private Integer transmissionIntervalMax;

	@Column(name = "last_upload_failure_on")
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime lastDataUploadFailure;

	public int getId() {
		return id;
	}

	public AccountEntity getAccount() {
		return account;
	}

	public void setAccount(AccountEntity account) {
		this.account = account;
	}

	public UUID getKey() {
		return key;
	}

	public Set<DevicePropertyEntity> getProperties() {
		return properties;
	}

    public DevicePropertyEntity getProperty(String key) {
        for(DevicePropertyEntity property : this.properties) {
            if(property.getKey().equals(key)) {
                return property;
            }
        }
        
        return null;
    }
    
	public EnumDeviceType getType() {
		return EnumDeviceType.UNDEFINED;
	}

	public DateTime getLastDataUploadSuccess() {
		return lastDataUploadSuccess;
	}

	public void setLastDataUploadSuccess(DateTime lastDataUploadSuccess) {
		this.lastDataUploadSuccess = lastDataUploadSuccess;
	}

	public DateTime getLastDataUploadFailure() {
		return lastDataUploadFailure;
	}

	public void setLastDataUploadFailure(DateTime lastDataUploadFailure) {
		this.lastDataUploadFailure = lastDataUploadFailure;
	}

	public DateTime getRegisteredOn() {
		return registeredOn;
	}

	public void setRegisteredOn(DateTime registeredOn) {
		this.registeredOn = registeredOn;
	}

	public long getRowVersion() {
		return rowVersion;
	}

	public Long getTransmissionCount() {
		return transmissionCount;
	}

	public void setTransmissionCount(Long transmissionCount) {
		this.transmissionCount = transmissionCount;
	}

	public Long getTransmissionIntervalSum() {
		return transmissionIntervalSum;
	}

	public void setTransmissionIntervalSum(Long transmissionIntervalSum) {
		this.transmissionIntervalSum = transmissionIntervalSum;
	}

	public Integer getTransmissionIntervalMax() {
		return transmissionIntervalMax;
	}

	public void setTransmissionIntervalMax(Integer transmissionIntervalMax) {
		this.transmissionIntervalMax = transmissionIntervalMax;
	}

}
