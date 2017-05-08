package eu.daiad.web.domain.application;

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
import javax.persistence.SequenceGenerator;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import eu.daiad.web.model.query.savings.EnumSavingScenarioStatus;

@Entity(name = "savings_potential_scenario")
@Table(schema = "public", name = "savings_potential_scenario")
@SqlResultSetMapping(name = "ScenarioClusterSegmentResult", classes = {
    @ConstructorResult(targetClass = eu.daiad.web.domain.application.mappings.SavingScenarioSegmentEntity.class, columns = {
        @ColumnResult(name = "id"),
        @ColumnResult(name = "cluster_name"),
        @ColumnResult(name = "cluster_key", type = UUID.class),
        @ColumnResult(name = "segment_name"),
        @ColumnResult(name = "segment_key", type = UUID.class),
        @ColumnResult(name = "potential", type = Double.class),
        @ColumnResult(name = "consumption", type = Double.class)
    })
})
public class SavingsPotentialScenarioEntity {

    @Id()
    @Column(name = "id")
    @SequenceGenerator(sequenceName = "savings_potential_scenario_id_seq", name = "savings_potential_scenario_id_seq", allocationSize = 1, initialValue = 1)
    @GeneratedValue(generator = "savings_potential_scenario_id_seq", strategy = GenerationType.SEQUENCE)
    private long id;

    @Version()
    @Column(name = "row_version")
    private long rowVersion;

    @Column()
    @Type(type = "pg-uuid")
    private UUID key = UUID.randomUUID();

    @Column(name = "job_id")
    private Long jobId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "utility_id", nullable = false)
    private UtilityEntity utility;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "account_id", nullable = false)
    private AccountEntity owner;

    @Column(name = "created_on")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime createdOn = new DateTime();

    @Basic()
    private String name;

    @Basic()
    private String parameters;

    @Column(name = "savings_volume")
    private Double savingsVolume;

    @Column(name = "savings_percent")
    private Double savingsPercent;

    @Column(name = "consumption_volume")
    private Double consumption;

    @Column(name = "processing_start")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime processingDateBegin = null;

    @Column(name = "processing_end")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime processingDateEnd = null;

    @Enumerated(EnumType.STRING)
    private EnumSavingScenarioStatus status = EnumSavingScenarioStatus.PENDING;

    @Column(name = "number_of_consumers")
    private Integer numberOfConsumers;

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public UUID getKey() {
        return key;
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

    public DateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(DateTime createdOn) {
        this.createdOn = createdOn;
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

    public Double getSavingsVolume() {
        return savingsVolume;
    }

    public void setSavingsVolume(Double savingsVolume) {
        this.savingsVolume = savingsVolume;
    }

    public Double getSavingsPercent() {
        return savingsPercent;
    }

    public void setSavingsPercent(Double savingsPercent) {
        this.savingsPercent = savingsPercent;
    }

    public Double getConsumption() {
        return consumption;
    }

    public void setConsumption(Double consumption) {
        this.consumption = consumption;
    }

    public DateTime getProcessingDateBegin() {
        return processingDateBegin;
    }

    public void setProcessingDateBegin(DateTime processingDateBegin) {
        this.processingDateBegin = processingDateBegin;
    }

    public DateTime getProcessingDateEnd() {
        return processingDateEnd;
    }

    public void setProcessingDateEnd(DateTime processingDateEnd) {
        this.processingDateEnd = processingDateEnd;
    }

    public EnumSavingScenarioStatus getStatus() {
        return status;
    }

    public void setStatus(EnumSavingScenarioStatus status) {
        this.status = status;
    }

    public long getId() {
        return id;
    }

    public long getRowVersion() {
        return rowVersion;
    }

    public Integer getNumberOfConsumers() {
        return numberOfConsumers;
    }

    public void setNumberOfConsumers(Integer numberOfConsumers) {
        this.numberOfConsumers = numberOfConsumers;
    }

}
