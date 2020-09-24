package eu.daiad.common.domain.application;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

@Entity(name = "budget_account_snapshot")
@Table(schema = "public", name = "budget_account_snapshot")
@SqlResultSetMapping(name = "BudgetConsumerResult", classes = { @ConstructorResult(targetClass = eu.daiad.common.domain.application.mappings.BudgetConsumerEntity.class, columns = {
                @ColumnResult(name = "id"), @ColumnResult(name = "user_name"),
                @ColumnResult(name = "user_key", type = UUID.class),
                @ColumnResult(name = "consumption_before", type = Double.class),
                @ColumnResult(name = "consumption_after", type = Double.class) }) })
public class BudgetSnapshotAccountEntity {

    @Id()
    @Column(name = "id")
    @SequenceGenerator(sequenceName = "budget_account_snapshot_id_seq", name = "budget_account_snapshot_id_seq", allocationSize = 1, initialValue = 1)
    @GeneratedValue(generator = "budget_account_snapshot_id_seq", strategy = GenerationType.SEQUENCE)
    private long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "budget_snapshot_id", nullable = false)
    private BudgetSnapshotEntity snapshot;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "account_id", nullable = false)
    private AccountEntity account;

    @Column(name = "created_on")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime createdOn = new DateTime();

    @Column(name = "consumption_volume_before")
    private Double consumptionBefore;

    @Column(name = "consumption_volume_after")
    private Double consumptionAfter;

    @Column(name = "savings_percent")
    private Double percent;

    @Column(name = "expected_savings_percent")
    private Double expectedPercent;

    public BudgetSnapshotEntity getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(BudgetSnapshotEntity snapshot) {
        this.snapshot = snapshot;
    }

    public AccountEntity getAccount() {
        return account;
    }

    public void setAccount(AccountEntity account) {
        this.account = account;
    }

    public Double getConsumptionBefore() {
        return consumptionBefore;
    }

    public void setConsumptionBefore(Double consumptionBefore) {
        this.consumptionBefore = consumptionBefore;
    }

    public Double getConsumptionAfter() {
        return consumptionAfter;
    }

    public void setConsumptionAfter(Double consumptionAfter) {
        this.consumptionAfter = consumptionAfter;
    }

    public Double getPercent() {
        return percent;
    }

    public void setPercent(Double percent) {
        this.percent = percent;
    }

    public Double getExpectedPercent() {
        return expectedPercent;
    }

    public void setExpectedPercent(Double expectedPercent) {
        this.expectedPercent = expectedPercent;
    }

    public long getId() {
        return id;
    }

    public DateTime getCreatedOn() {
        return createdOn;
    }

}
