package eu.daiad.web.domain.application;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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

import eu.daiad.web.model.profile.EnumUnit;

@Entity(name = "account_profile")
@Table(schema = "public", name = "account_profile")
public class AccountProfileEntity {

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
    private AccountEntity account;

    @OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id")
    private Set<AccountProfileHistoryEntity> history = new HashSet<AccountProfileHistoryEntity>();

    @Column(name = "updated_on")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime updatedOn;

    @Column(name = "mobile_mode")
    private int mobileMode;

    @Column(name = "web_mode")
    private int webMode;

    @Column(name = "utility_mode")
    private int utilityMode;

    @Column(name = "social_enabled")
    private boolean socialEnabled;

    @Column(name = "mobile_config")
    private String mobileConfiguration;

    @Column(name = "web_config")
    private String webConfiguration;

    @Column(name = "utility_config")
    private String utilityConfiguration;

    @Column(name = "daily_amphiro_budget")
    private Integer dailyAmphiroBudget;

    @Column(name = "daily_meter_budget")
    private Integer dailyMeterBudget;

    @Enumerated(EnumType.STRING)
    private EnumUnit unit;

    @Basic()
    private Boolean garden;

    @Column(name = "send_mail")
    private boolean sendMailEnabled;

    @Column(name = "send_message")
    private boolean sendMessageEnabled;

    @Column(name = "mobile_app_version")
    private String mobileApplicationVersion;
    
    @Column(name = "layouts")
    private String layouts;

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

    public void setAccount(AccountEntity account) {
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

    public boolean isSocialEnabled() {
        return socialEnabled;
    }

    public void setSocialEnabled(boolean socialEnabled) {
        this.socialEnabled = socialEnabled;
    }

    public Set<AccountProfileHistoryEntity> getHistory() {
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

    public Integer getDailyAmphiroBudget() {
        return dailyAmphiroBudget;
    }

    public void setDailyAmphiroBudget(Integer dailyAmphiroBudget) {
        this.dailyAmphiroBudget = dailyAmphiroBudget;
    }

    public Integer getDailyMeterBudget() {
        return dailyMeterBudget;
    }

    public void setDailyMeterBudget(Integer dailyMeterBudget) {
        this.dailyMeterBudget = dailyMeterBudget;
    }

    public EnumUnit getUnit() {
        return unit;
    }

    public void setUnit(EnumUnit unit) {
        this.unit = unit;
    }

    public Boolean getGarden() {
        return garden;
    }

    public void setGarden(Boolean garden) {
        this.garden = garden;
    }

    public boolean isSendMailEnabled() {
        return sendMailEnabled;
    }

    public void setSendMailEnabled(boolean sendMailEnabled) {
        this.sendMailEnabled = sendMailEnabled;
    }

    public String getMobileApplicationVersion() {
        return mobileApplicationVersion;
    }

    public void setMobileApplicationVersion(String mobileApplicationVersion) {
        this.mobileApplicationVersion = mobileApplicationVersion;
    }

    public boolean isSendMessageEnabled() {
        return sendMessageEnabled;
    }

    public void setSendMessageEnabled(boolean sendMessageEnabled) {
        this.sendMessageEnabled = sendMessageEnabled;
    }

    public String getLayouts() {
        return layouts;
    }

    public void setLayouts(String layouts) {
        this.layouts = layouts;
    }

}
