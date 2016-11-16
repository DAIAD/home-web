package eu.daiad.web.domain.application;

import java.util.UUID;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import eu.daiad.web.model.EnumApplication;

@Entity(name = "password_reset_token")
@Table(schema = "public", name = "password_reset_token")
public class PasswordResetTokenEntity {

    @Id()
    @Column(name = "id")
    @SequenceGenerator(sequenceName = "password_reset_token_id_seq", name = "password_reset_token_id_seq", allocationSize = 1, initialValue = 1)
    @GeneratedValue(generator = "password_reset_token_id_seq", strategy = GenerationType.SEQUENCE)
    private int id;

    @ManyToOne(cascade = { CascadeType.ALL })
    @JoinColumn(name = "account_id", nullable = false)
    private AccountEntity account;

    @Column(name = "created_on")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime createdOn = DateTime.now();

    @Column(name = "redeemed_on")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime redeemedOn;

    @Column()
    @Type(type = "pg-uuid")
    private UUID token = UUID.randomUUID();

    @Basic()
    private String pin;

    @Enumerated(EnumType.STRING)
    private EnumApplication application;

    @Basic()
    private boolean valid = true;

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public AccountEntity getAccount() {
        return account;
    }

    public void setAccount(AccountEntity account) {
        this.account = account;
    }

    public DateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(DateTime createdOn) {
        this.createdOn = createdOn;
    }

    public DateTime getRedeemedOn() {
        return redeemedOn;
    }

    public void setRedeemedOn(DateTime redeemedOn) {
        this.redeemedOn = redeemedOn;
    }

    public int getId() {
        return id;
    }

    public UUID getToken() {
        return token;
    }

    public boolean isExpired(int days) {
        return this.createdOn.plusDays(days).isBeforeNow();
    }

    public boolean isReedemed() {
        return (this.redeemedOn != null);
    }

    public EnumApplication getApplication() {
        return application;
    }

    public void setApplication(EnumApplication application) {
        this.application = application;
    }

}
