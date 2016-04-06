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

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

@Entity(name = "job_execution")
@Table(schema = "public", name = "job_execution")
public class JobExecution {

	@Id()
	@Column(name = "id")
	@SequenceGenerator(sequenceName = "job_execution_id_seq", name = "job_execution_id_seq", allocationSize = 1, initialValue = 1)
	@GeneratedValue(generator = "job_execution_id_seq", strategy = GenerationType.SEQUENCE)
	private long id;

	@ManyToOne(cascade = { CascadeType.ALL })
	@JoinColumn(name = "job_id", nullable = false)
	private Job job;

	@Column(name = "batch_execution_id")
	private long batchExecutionId;

	@Column(name = "account_id")
	private int accountId;

	@Basic
	private String username;

	@Basic
	private String description;

	@Column(name = "date_created")
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime createdOn;

	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
	@JoinColumn(name = "job_execution_id")
	private Set<JobExecutionParameter> parameters = new HashSet<JobExecutionParameter>();

	public Job getJob() {
		return job;
	}

	public void setJob(Job job) {
		this.job = job;
	}

	public long getBatchExecutionId() {
		return batchExecutionId;
	}

	public void setBatchExecutionId(long batchExecutionId) {
		this.batchExecutionId = batchExecutionId;
	}

	public int getAccountId() {
		return accountId;
	}

	public void setAccountId(int accountId) {
		this.accountId = accountId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public DateTime getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(DateTime createdOn) {
		this.createdOn = createdOn;
	}

	public Set<JobExecutionParameter> getParameters() {
		return parameters;
	}

	public void setParameters(Set<JobExecutionParameter> parameters) {
		this.parameters = parameters;
	}

	public long getId() {
		return id;
	}

}
