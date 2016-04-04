package eu.daiad.web.domain.application;

import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

@Entity(name = "account_profile_history")
@Table(schema = "public", name = "account_profile_history")
public class AccountProfileHistoryEntry {

	@Id()
	@Column(name = "id")
	@SequenceGenerator(sequenceName = "account_profile_history_id_seq", name = "account_profile_history_id_seq", allocationSize = 1, initialValue = 1)
	@GeneratedValue(generator = "account_profile_history_id_seq", strategy = GenerationType.SEQUENCE)
	private int id;

	@ManyToOne(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
	@JoinColumn(name = "profile_id", nullable = false)
	private AccountProfile profile;

	@Column()
	@Type(type = "pg-uuid")
	private UUID version;

	@Column(name = "updated_on")
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime updatedOn;

	@Column(name = "acknowledged_on")
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime acknowledgedOn;

	@Column(name = "enabled_on")
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime enabledOn;

	@Column(name = "mobile_mode")
	private int mobileMode;

	@Column(name = "web_mode")
	private int webMode;

	@Column(name = "utility_mode")
	private int utilityMode;
	
	public UUID getVersion() {
		return version;
	}

	public void setVersion(UUID version) {
		this.version = version;
	}

	public DateTime getUpdatedOn() {
		return updatedOn;
	}

	public void setUpdatedOn(DateTime createdOn) {
		this.updatedOn = createdOn;
	}

	public DateTime getEnabledOn() {
		return enabledOn;
	}

	public void setEnabledOn(DateTime enabledOn) {
		this.enabledOn = enabledOn;
	}

	public int getId() {
		return id;
	}

	public void setProfile(AccountProfile profile) {
		this.profile = profile;
	}

	public int getMobileMode() {
		return mobileMode;
	}

	public void setMobileMode(int mobileMode) {
		this.mobileMode = mobileMode;
	}

	public int getWebMode() {
		return webMode;
	}

	public void setWebMode(int webMode) {
		this.webMode = webMode;
	}

	public int getUtilityMode() {
		return utilityMode;
	}

	public void setUtilityMode(int utilityMode) {
		this.utilityMode = utilityMode;
	}

	public DateTime getAcknowledgedOn() {
		return acknowledgedOn;
	}

	public void setAcknowledgedOn(DateTime acknowledgedOn) {
		this.acknowledgedOn = acknowledgedOn;
	}

}
