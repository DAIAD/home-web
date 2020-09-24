package eu.daiad.common.domain.application;

import javax.persistence.Basic;
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

@Entity(name = "savings_potential_result")
@Table(schema = "public", name = "savings_potential_result")
public class SavingsPotentialResultEntity {

    @Id()
    @Column(name = "id")
    @SequenceGenerator(sequenceName = "savings_potential_result_id_seq", name = "savings_potential_result_id_seq", allocationSize = 1, initialValue = 1)
    @GeneratedValue(generator = "savings_potential_result_id_seq", strategy = GenerationType.SEQUENCE)
    private long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "scenario_id", nullable = false)
    private SavingsPotentialScenarioEntity scenario;

    @Column(name = "created_on")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime createdOn = new DateTime();

    @Basic()
    private String cluster;

    @Basic()
    private int month;

    @Basic()
    private String serial;

    @Column(name = "savings_percent")
    private double savingsPercent;

    @Column(name = "savings_volume")
    private double savingsVolume;

    @Column(name = "cluster_size")
    private int clusterSize;

    @Basic()
    private String iq;

    @Basic()
    private double deviation;

    public SavingsPotentialScenarioEntity getScenario() {
        return scenario;
    }

    public void setScenario(SavingsPotentialScenarioEntity scenario) {
        this.scenario = scenario;
    }

    public DateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(DateTime createdOn) {
        this.createdOn = createdOn;
    }

    public String getCluster() {
        return cluster;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
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

    public int getClusterSize() {
        return clusterSize;
    }

    public void setClusterSize(int clusterSize) {
        this.clusterSize = clusterSize;
    }

    public String getIq() {
        return iq;
    }

    public void setIq(String iq) {
        this.iq = iq;
    }

    public double getDeviation() {
        return deviation;
    }

    public void setDeviation(double deviation) {
        this.deviation = deviation;
    }

    public long getId() {
        return id;
    }

}
