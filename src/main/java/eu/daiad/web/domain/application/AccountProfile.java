package eu.daiad.web.domain.application;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

@Entity(name = "account_profile")
@Table(schema = "public", name = "account_profile")
public class AccountProfile {

	@Id
	@GenericGenerator(name = "generator", strategy = "foreign", parameters = @Parameter(name = "property", value = "account"))
	@GeneratedValue(generator = "generator")
	@Column(name = "id", unique = true, nullable = false)
	private int id;

	@Version()
	@Column(name = "row_version")
	private long rowVersion;

	@Column()
	@Type(type = "pg-uuid")
	private UUID version = UUID.randomUUID();

	@OneToOne(fetch = FetchType.LAZY)
	@PrimaryKeyJoinColumn()
	private Account account;

	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
	@JoinColumn(name = "profile_id")
	private Set<AccountProfileHistoryEntry> history = new HashSet<AccountProfileHistoryEntry>();

	@Column(name = "updated_on")
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime updatedOn;

	@Column(name = "mobile_mode")
	private int mobileMode;

	@Column(name = "web_mode")
	private int webMode;

	@Column(name = "utility_mode")
	private int utilityMode;

	@Column(name = "mobile_config")
	private String mobileConfiguration;

	@Column(name = "web_config")
	private String webConfiguration;

	@Column(name = "utility_config")
	private String utilityConfiguration;

	@Column(name = "amphiro_budget")
	private Integer amphiroBudget;

	@Column(name = "meter_budget")
	private Integer meterBudget;

	public int getId() {
		return id;
	}

	public String getMobileConfiguration() {
		return mobileConfiguration;
	}

	public void setMobileConfiguration(String mobileConfiguration) {
		this.mobileConfiguration = mobileConfiguration;
	}

	public String getWebConfiguration() {
		return webConfiguration;
	}

	public void setWebConfiguration(String webConfiguration) {
		this.webConfiguration = webConfiguration;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	public String getUtilityConfiguration() {
		return utilityConfiguration;
	}

	public void setUtilityConfiguration(String utilityConfiguration) {
		this.utilityConfiguration = utilityConfiguration;
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

	public Set<AccountProfileHistoryEntry> getHistory() {
		return history;
	}

	public DateTime getUpdatedOn() {
		return updatedOn;
	}

	public void setUpdatedOn(DateTime updatedOn) {
		this.updatedOn = updatedOn;
	}

	public UUID getVersion() {
		return version;
	}

	public void setVersion(UUID version) {
		this.version = version;
	}

	public long getRowVersion() {
		return rowVersion;
	}

	public Integer getAmphiroBudget() {
		return amphiroBudget;
	}

	public void setAmphiroBudget(Integer amphiroBudget) {
		this.amphiroBudget = amphiroBudget;
	}

	public Integer getMeterBudget() {
		return meterBudget;
	}

	public void setMeterBudget(Integer meterBudget) {
		this.meterBudget = meterBudget;
	}

}
