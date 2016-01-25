package eu.daiad.web.domain;

import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

@Entity(name = "device")
@Table(schema = "public", name = "device")
@Inheritance(strategy = InheritanceType.JOINED)
public class Device {

	@Id()
	@Column(name = "id")
	@SequenceGenerator(sequenceName = "device_id_seq", name = "device_id_seq", allocationSize = 1, initialValue = 1)
	@GeneratedValue(generator = "device_id_seq", strategy = GenerationType.SEQUENCE)
	private int id;

	@ManyToOne(cascade = { CascadeType.ALL })
	@JoinColumn(name = "account_id", nullable = false)
	private Account account;

	@Column()
	@Type(type = "pg-uuid")
	private UUID key = UUID.randomUUID();

	public int getId() {
		return id;
	}

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	public UUID getKey() {
		return key;
	}

}
