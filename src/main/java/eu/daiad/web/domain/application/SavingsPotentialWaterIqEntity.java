package eu.daiad.web.domain.application;

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

@Entity(name = "savings_potential_water_iq")
@Table(schema = "public", name = "savings_potential_water_iq")
public class SavingsPotentialWaterIqEntity {

    @Id()
    @Column(name = "id")
    @SequenceGenerator(sequenceName = "savings_potential_water_iq_id_seq", name = "savings_potential_water_iq_id_seq", allocationSize = 1, initialValue = 1)
    @GeneratedValue(generator = "savings_potential_water_iq_id_seq", strategy = GenerationType.SEQUENCE)
    private long id;

    @Column(name = "job_id")
    private long jobId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utility_id", nullable = false)
    private UtilityEntity utility;

    @Column(name = "created_on")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime createdOn = new DateTime();

    @Basic()
    private int month;

    @Basic()
    private String serial;

    @Basic()
    private String iq;

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

    public String getIq() {
        return iq;
    }

    public void setIq(String iq) {
        this.iq = iq;
    }

    public long getId() {
        return id;
    }

    public DateTime getCreatedOn() {
        return createdOn;
    }

    public UtilityEntity getUtility() {
        return utility;
    }

    public void setUtility(UtilityEntity utility) {
        this.utility = utility;
    }

    public long getJobId() {
        return jobId;
    }

    public void setJobId(long jobId) {
        this.jobId = jobId;
    }

}
