package eu.daiad.common.domain.application;

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

@Entity(name = "savings_potential_account")
@Table(schema = "public", name = "savings_potential_account")
public class SavingsPotentialAccountEntity {

    @Id()
    @Column(name = "id")
    @SequenceGenerator(sequenceName = "savings_potential_account_id_seq", name = "savings_potential_account_id_seq", allocationSize = 1, initialValue = 1)
    @GeneratedValue(generator = "savings_potential_account_id_seq", strategy = GenerationType.SEQUENCE)
    private long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "scenario_id", nullable = false)
    private SavingsPotentialScenarioEntity scenario;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "account_id", nullable = false)
    private AccountEntity account;

    @Column(name = "created_on")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime createdOn = new DateTime();

    @Column(name = "savings_percent")
    private double savingsPercent;

    @Column(name = "savings_volume")
    private double savingsVolume;

    @Column()
    private double consumption;

    public SavingsPotentialScenarioEntity getScenario() {
        return scenario;
    }

    public void setScenario(SavingsPotentialScenarioEntity scenario) {
        this.scenario = scenario;
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

    public double getSavingsPercent() {
        return savingsPercent;
    }

    public void setSavingsPercent(double savingsPercent) {
        this.savingsPercent = savingsPercent;
    }

    public double getSavingsVolume() {
        return savingsVolume;
    }

    public void setSavingsVolume(double savingsVolume) {
        this.savingsVolume = savingsVolume;
    }

    public double getConsumption() {
        return consumption;
    }

    public void setConsumption(double consumption) {
        this.consumption = consumption;
    }

    public long getId() {
        return id;
    }

}
