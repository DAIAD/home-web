package eu.daiad.web.domain.admin;

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

@Entity(name = "scheduled_job_parameter")
@Table(schema = "public", name = "scheduled_job_parameter")
public class ScheduledJobParameter {

	@Id()
	@Column(name = "id")
	@SequenceGenerator(sequenceName = "scheduled_job_parameter_id_seq", name = "scheduled_job_parameter_id_seq", allocationSize = 1, initialValue = 1)
	@GeneratedValue(generator = "scheduled_job_parameter_id_seq", strategy = GenerationType.SEQUENCE)
	private long id;

	@ManyToOne(cascade = { CascadeType.ALL })
	@JoinColumn(name = "scheduled_job_id", nullable = false)
	private ScheduledJob scheduledJob;

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

	public ScheduledJob getScheduledJob() {
		return scheduledJob;
	}

	public void setScheduledJob(ScheduledJob scheduledJob) {
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

}
