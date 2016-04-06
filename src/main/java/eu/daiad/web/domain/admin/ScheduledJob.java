package eu.daiad.web.domain.admin;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity(name = "scheduled_job")
@Table(schema = "public", name = "scheduled_job")
public class ScheduledJob {

	@Id()
	@Column(name = "id")
	@SequenceGenerator(sequenceName = "scheduled_job_id_seq", name = "scheduled_job_id_seq", allocationSize = 1, initialValue = 1)
	@GeneratedValue(generator = "scheduled_job_id_seq", strategy = GenerationType.SEQUENCE)
	private long id;

	@ManyToOne(cascade = { CascadeType.ALL })
	@JoinColumn(name = "job_id", nullable = false)
	private Job job;

	@Basic
	private Long period;

	@Column(name = "cron_expression")
	private String cronExpression;

	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
	@JoinColumn(name = "scheduled_job_id")
	private Set<ScheduledJobParameter> parameters = new HashSet<ScheduledJobParameter>();

	public Long getPeriod() {
		return period;
	}

	public void setPeriod(Long period) {
		this.period = period;
	}

	public long getId() {
		return id;
	}

	public String getCronExpression() {
		return cronExpression;
	}

	public void setCronExpression(String cronExpression) {
		this.cronExpression = cronExpression;
	}

	public Job getJob() {
		return job;
	}

	public void setJob(Job job) {
		this.job = job;
	}

	public Set<ScheduledJobParameter> getParameters() {
		return parameters;
	}

	public void setParameters(Set<ScheduledJobParameter> parameters) {
		this.parameters = parameters;
	}

}
