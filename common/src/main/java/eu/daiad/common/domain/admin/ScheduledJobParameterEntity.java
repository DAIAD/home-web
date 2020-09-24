package eu.daiad.common.domain.admin;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import eu.daiad.common.model.scheduling.Constants;

@Entity(name = "scheduled_job_parameter")
@Table(schema = "public", name = "scheduled_job_parameter")
public class ScheduledJobParameterEntity {

    @Id()
    @Column(name = "id")
    @SequenceGenerator(sequenceName = "scheduled_job_parameter_id_seq", name = "scheduled_job_parameter_id_seq", allocationSize = 1, initialValue = 1)
    @GeneratedValue(generator = "scheduled_job_parameter_id_seq", strategy = GenerationType.SEQUENCE)
    private long id;

    @ManyToOne(cascade = { CascadeType.ALL })
    @JoinColumn(name = "scheduled_job_id", nullable = false)
    private ScheduledJobEntity scheduledJob;

    @Basic
    private String step;

    @Basic
    private String name;

    @Basic
    private String value;

    @Basic
    private boolean hidden;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public ScheduledJobEntity getScheduledJob() {
        return scheduledJob;
    }

    public void setScheduledJob(ScheduledJobEntity scheduledJob) {
        this.scheduledJob = scheduledJob;
    }

    public long getId() {
        return id;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public String getStep() {
        return step;
    }

    public void setStep(String step) {
        this.step = step;
    }

    public String getQualifiedName() {
        return (step + Constants.PARAMETER_NAME_DELIMITER + name);
    }
}
