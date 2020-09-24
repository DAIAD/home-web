package eu.daiad.common.domain.application;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import eu.daiad.common.model.query.savings.EnumBudgetDistribution;
import eu.daiad.common.model.query.savings.EnumBudgetStatus;

@Entity(name = "budget")
@Table(schema = "public", name = "budget")
@SqlResultSetMapping(name = "BudgetClusterSegmentResult", classes = { @ConstructorResult(targetClass = eu.daiad.common.domain.application.mappings.BudgetSegmentEntity.class, columns = {
                @ColumnResult(name = "id"), @ColumnResult(name = "cluster_name"),
                @ColumnResult(name = "cluster_key", type = UUID.class), @ColumnResult(name = "segment_name"),
                @ColumnResult(name = "segment_key", type = UUID.class),
                @ColumnResult(name = "consumption_before", type = Double.class),
                @ColumnResult(name = "consumption_after", type = Double.class) }) })
public class BudgetEntity {

    @Id()
    @Column(name = "id")
    @SequenceGenerator(sequenceName = "budget_id_seq", name = "budget_id_seq", allocationSize = 1, initialValue = 1)
    @GeneratedValue(generator = "budget_id_seq", strategy = GenerationType.SEQUENCE)
    private long id;

    @Version()
    @Column(name = "row_version")
    private long rowVersion;

    @Column()
    @Type(type = "pg-uuid")
    private UUID key = UUID.randomUUID();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "utility_id", nullable = false)
    private UtilityEntity utility;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "account_id", nullable = false)
    private AccountEntity owner;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "scenario_id")
    private SavingsPotentialScenarioEntity scenario;

    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "budget_id")
    private Set<BudgetSnapshotEntity> snapshots = new HashSet<>();

    @Column(name = "scenario_percent")
    private BigDecimal scenarioPercent;

    @Column(name = "budget_goal_percent")
    private BigDecimal goal;

    @Column(name = "created_on")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime createdOn = new DateTime();

    @Column(name = "updated_on")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime updatedOn;

    @Column(name = "next_update_on")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime nextUpdateOn;

    @Basic()
    private String name;

    @Basic()
    private String parameters;

    @Basic()
    private boolean active;

    @Column(name = "activated_on")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime activatedOn;

    @Column(name = "number_of_consumers")
    private Integer numberOfConsumers;

    @Column(name = "expected_savings_percent")
    private Double expectedPercent;

    @Enumerated(EnumType.STRING)
    @Column(name = "budget_distribution")
    private EnumBudgetDistribution distribution = EnumBudgetDistribution.UNDEFINED;

    @Basic()
    private boolean initialized;

    public long getRowVersion() {
        return rowVersion;
    }

    public void setRowVersion(long rowVersion) {
        this.rowVersion = rowVersion;
    }

    public UtilityEntity getUtility() {
        return utility;
    }

    public void setUtility(UtilityEntity utility) {
        this.utility = utility;
    }

    public AccountEntity getOwner() {
        return owner;
    }

    public void setOwner(AccountEntity owner) {
        this.owner = owner;
    }

    public SavingsPotentialScenarioEntity getScenario() {
        return scenario;
    }

    public void setScenario(SavingsPotentialScenarioEntity scenario) {
        this.scenario = scenario;
    }

    public BigDecimal getScenarioPercent() {
        return scenarioPercent;
    }

    public void setScenarioPercent(BigDecimal scenarioPercent) {
        this.scenarioPercent = scenarioPercent;
    }

    public BigDecimal getGoal() {
        return goal;
    }

    public void setGoal(BigDecimal goal) {
        this.goal = goal;
    }

    public DateTime getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(DateTime updatedOn) {
        this.updatedOn = updatedOn;
    }

    public DateTime getNextUpdateOn() {
        return nextUpdateOn;
    }

    public void setNextUpdateOn(DateTime nextUpdateOn) {
        this.nextUpdateOn = nextUpdateOn;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public DateTime getActivatedOn() {
        return activatedOn;
    }

    public void setActivatedOn(DateTime activatedOn) {
        this.activatedOn = activatedOn;
    }

    public Integer getNumberOfConsumers() {
        return numberOfConsumers;
    }

    public void setNumberOfConsumers(Integer numberOfConsumers) {
        this.numberOfConsumers = numberOfConsumers;
    }

    public EnumBudgetDistribution getDistribution() {
        return distribution;
    }

    public void setDistribution(EnumBudgetDistribution distribution) {
        this.distribution = distribution;
    }

    public long getId() {
        return id;
    }

    public UUID getKey() {
        return key;
    }

    public DateTime getCreatedOn() {
        return createdOn;
    }

    public Double getConsumptionBefore() {
        if ((snapshots == null) || (snapshots.isEmpty())) {
            return null;
        }
        double value = 0;
        for (BudgetSnapshotEntity entity : snapshots) {
            if(entity.getStatus() == EnumBudgetStatus.COMPLETED)  {
                value += entity.getConsumptionBefore();
            }
        }

        return value;
    }

    public Double getConsumptionAfter() {
        if ((snapshots == null) || (snapshots.isEmpty())) {
            return null;
        }
        double value = 0;
        for (BudgetSnapshotEntity entity : snapshots) {
            if(entity.getStatus() == EnumBudgetStatus.COMPLETED)  {
                value += entity.getConsumptionAfter();
            }
        }

        return value;
    }

    public Double getSavingsPercent() {
        Double before = getConsumptionBefore();
        Double after = getConsumptionAfter();

        if ((before != null) && (after != null) && (before > 0)) {
            return ((before - after) / before);
        }

        return 0D;
    }

    public Set<BudgetSnapshotEntity> getSnapshots() {
        return snapshots;
    }

    public Double getExpectedPercent() {
        return expectedPercent;
    }

    public void setExpectedPercent(Double expectedPercent) {
        this.expectedPercent = expectedPercent;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

}
