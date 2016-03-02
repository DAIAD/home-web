package eu.daiad.web.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

@Entity(name = "account_profile")
@Table(schema = "public", name = "account_profile")
public class AccountProfile {

	@Id
	@GenericGenerator(name = "generator", strategy = "foreign", parameters = @Parameter(name = "property", value = "account"))
	@GeneratedValue(generator = "generator")
	@Column(name = "id", unique = true, nullable = false)
	private int id;

	@OneToOne(fetch = FetchType.LAZY)
	@PrimaryKeyJoinColumn()
	private Account account;

	@Column(name = "mobile_enabled")
	private boolean mobileEnabled;

	@Column(name = "web_enabled")
	private boolean webEnabled;

	@Column(name = "utility_enabled")
	private boolean utilityEnabled;
	
	@Column(name = "mobile_config")
	private String mobileConfiguration;

	@Column(name = "web_config")
	private String webConfiguration;

	@Column(name = "utility_config")
	private String utilityConfiguration;
	
	public int getId() {
		return id;
	}

	public boolean isMobileEnabled() {
		return mobileEnabled;
	}

	public void setMobileEnabled(boolean mobileEnabled) {
		this.mobileEnabled = mobileEnabled;
	}

	public boolean isWebEnabled() {
		return webEnabled;
	}

	public void setWebEnabled(boolean webEnabled) {
		this.webEnabled = webEnabled;
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

	public boolean isUtilityEnabled() {
		return utilityEnabled;
	}

	public void setUtilityEnabled(boolean utilityEnabled) {
		this.utilityEnabled = utilityEnabled;
	}

	public String getUtilityConfiguration() {
		return utilityConfiguration;
	}

	public void setUtilityConfiguration(String utilityConfiguration) {
		this.utilityConfiguration = utilityConfiguration;
	}

}
