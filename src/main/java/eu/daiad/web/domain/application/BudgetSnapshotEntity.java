package eu.daiad.web.domain.application;

import javax.persistence.Basic;
import javax.persistence.Column;
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
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import eu.daiad.web.model.query.savings.EnumBudgetStatus;

@Entity(name = "budget_snapshot")
@Table(schema = "public", name = "budget_snapshot")
public class BudgetSnapshotEntity {

    @Id()
    @Column(name = "id")
    @SequenceGenerator(sequenceName = "budget_snapshot_id_seq", name = "budget_snapshot_id_seq", allocationSize = 1, initialValue = 1)
    @GeneratedValue(generator = "budget_snapshot_id_seq", strategy = GenerationType.SEQUENCE)
    private long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "budget_id", nullable = false)
    private BudgetEntity budget;

    @Column(name = "job_id")
    private Long jobId;

    @Column(name = "created_on")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime createdOn = new DateTime();

    @Column(name = "processing_start")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime processingDateBegin = null;

    @Column(name = "processing_end")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime processingDateEnd = null;

    @Enumerated(EnumType.STRING)
    private EnumBudgetStatus status = EnumBudgetStatus.PENDING;

    @Basic()
    private int year;

    @Basic()
    private int month;

    @Column(name = "consumption_volume_before")
    private Double consumptionBefore;

    @Column(name = "consumption_volume_after")
    private Double consumptionAfter;

    @Column(name = "savings_percent")
    private Double percent;

    @Column(name = "expected_savings_percent")
    private Double expectedPercent;

    public long getId() {
        return id;
    }

    public BudgetEntity getBudget() {
        return budget;
    }

    public void setBudget(BudgetEntity budget) {
        this.budget = budget;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
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

    public EnumBudgetStatus getStatus() {
        return status;
    }

    public void setStatus(EnumBudgetStatus status) {
        this.status = status;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
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

    public DateTime getCreatedOn() {
        return createdOn;
    }

}
