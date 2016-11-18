package eu.daiad.web.domain.application;

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

@Entity(name = "account_alert_property")
@Table(schema = "public", name = "account_alert_property")
public class AccountAlertPropertyEntity {

	@Id()
	@Column(name = "id")
	@SequenceGenerator(sequenceName = "account_alert_property_id_seq", name = "account_alert_property_id_seq", allocationSize = 1, initialValue = 1)
	@GeneratedValue(generator = "account_alert_property_id_seq", strategy = GenerationType.SEQUENCE)
	private int id;

	@ManyToOne(cascade = { CascadeType.ALL })
	@JoinColumn(name = "account_alert_id", nullable = false)
	private AccountAlertEntity alert;

	@Basic()
	private String key;

	@Basic()
	private String value;

	public AccountAlertEntity getAlert() {
		return alert;
	}

	public void setAlert(AccountAlertEntity alert) {
		this.alert = alert;
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
