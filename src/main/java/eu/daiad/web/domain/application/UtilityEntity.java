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
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.apache.commons.net.util.Base64;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import eu.daiad.web.model.profile.EnumMobileMode;
import eu.daiad.web.model.profile.EnumWebMode;

@Entity(name = "utility")
@Table(schema = "public", name = "utility")
public class UtilityEntity {

    @Id()
    @Column(name = "id")
    @SequenceGenerator(sequenceName = "utility_id_seq", name = "utility_id_seq", allocationSize = 1, initialValue = 1)
    @GeneratedValue(generator = "utility_id_seq", strategy = GenerationType.SEQUENCE)
    private int id;

    @Column()
    @Type(type = "pg-uuid")
    private UUID key = UUID.randomUUID();

    @Basic
    private String name;

    @Basic
    private String description;

    @Column(name = "date_created")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime createdOn = new DateTime();

    @Basic(fetch = FetchType.LAZY)
    @Type(type = "org.hibernate.type.BinaryType")
    private byte logo[];

    @OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
    @JoinColumn(name = "utility_id")
    private Set<AccountEntity> accounts = new HashSet<AccountEntity>();

    @Column(name = "default_admin_username", nullable = false, unique = true)
    private String defaultAdministratorUsername;

    @Column(name = "locale", columnDefinition = "bpchar", length = 2)
    private String locale;

    @Basic()
    private String timezone;

    @Basic()
    private String country;

    @Basic()
    private String city;

    @Column(name = "default_amphiro_mode")
    private int defaultAmphiroB1Mode;

    @Column(name = "default_mobile_mode")
    @Enumerated(EnumType.STRING)
    private EnumMobileMode defaultMobileMode;

    @Column(name = "default_web_mode")
    @Enumerated(EnumType.STRING)
    private EnumWebMode defaultWebMode;

    @Column(name = "default_social_mode")
    private boolean defaultSocialEnabled;

    @Column(name = "message_generation_enabled")
    private boolean messageGenerationEnabled;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<AccountEntity> getAccounts() {
        return accounts;
    }

    public DateTime getCreatedOn() {
        return createdOn;
    }

    public byte[] getLogo() {
        return logo;
    }

    public void setLogo(byte[] logo) {
        this.logo = logo;
    }

    public String logoToBase64() {
        if (logo != null) {
            return new String(Base64.encodeBase64(logo));
        }
        return "";
    }

    public String getDefaultAdministratorUsername() {
        return defaultAdministratorUsername;
    }

    public UUID getKey() {
        return key;
    }

    public String getLocale() {
        return locale;
    }

    public String getTimezone() {
        return timezone;
    }

    public String getCountry() {
        return country;
    }

    public String getCity() {
        return city;
    }

    public int getDefaultAmphiroB1Mode() {
        return defaultAmphiroB1Mode;
    }

    public void setDefaultAmphiroB1Mode(int defaultAmphiroB1Mode) {
        this.defaultAmphiroB1Mode = defaultAmphiroB1Mode;
    }

    public EnumMobileMode getDefaultMobileMode() {
        return defaultMobileMode;
    }

    public void setDefaultMobileMode(EnumMobileMode defaultMobileMode) {
        this.defaultMobileMode = defaultMobileMode;
    }

    public EnumWebMode getDefaultWebMode() {
        return defaultWebMode;
    }

    public void setDefaultWebMode(EnumWebMode defaultWebMode) {
        this.defaultWebMode = defaultWebMode;
    }

    public boolean isDefaultSocialEnabled() {
        return defaultSocialEnabled;
    }

    public void setDefaultSocialEnabled(boolean defaultSocialEnabled) {
        this.defaultSocialEnabled = defaultSocialEnabled;
    }

    public boolean isMessageGenerationEnabled() {
        return messageGenerationEnabled;
    }

    public void setMessageGenerationEnabled(boolean messageGenerationEnabled) {
        this.messageGenerationEnabled = messageGenerationEnabled;
    }

}
