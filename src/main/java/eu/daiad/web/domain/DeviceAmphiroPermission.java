package eu.daiad.web.domain;

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

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

@Entity(name = "device_amphiro_permission")
@Table(schema = "public", name = "device_amphiro_permission")
public class DeviceAmphiroPermission {

	@Id()
	@Column(name = "id")
	@SequenceGenerator(sequenceName = "device_amphiro_permission_id_seq", name = "device_amphiro_permission_id_seq", allocationSize = 1, initialValue = 1)
	@GeneratedValue(generator = "device_amphiro_permission_id_seq", strategy = GenerationType.SEQUENCE)
	private int id;

	@ManyToOne(cascade = { CascadeType.ALL })
	@JoinColumn(name = "owner_id", nullable = false)
	private Account owner;

	@ManyToOne(cascade = { CascadeType.ALL })
	@JoinColumn(name = "assignee_id", nullable = false)
	private Account assignee;

	@ManyToOne(cascade = { CascadeType.ALL })
	@JoinColumn(name = "device_id", nullable = false)
	private DeviceAmphiro device;

	@Column(name = "date_assigned")
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime assignedOn = new DateTime();

	public Account getOwner() {
		return owner;
	}

	public void setOwner(Account owner) {
		this.owner = owner;
	}

	public Account getAssignee() {
		return assignee;
	}

	public void setAssignee(Account assignee) {
		this.assignee = assignee;
	}

	public DeviceAmphiro getDevice() {
		return device;
	}

	public void setDevice(DeviceAmphiro device) {
		this.device = device;
	}

	public DateTime getAssignedOn() {
		return assignedOn;
	}

	public void setAssignedOn(DateTime assignedOn) {
		this.assignedOn = assignedOn;
	}

	public int getId() {
		return id;
	}

}
