package eu.daiad.web.domain.admin;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

@Entity(name = "scheduled_job_execution")
@Table(schema = "batch", name = "scheduled_job_execution")
public class ScheduledJobExecution {

	@Id()
	@Column(name = "job_instance_id")
	private long jobInstanceId;

	@Column(name = "job_execution_id")
	private long jobExecutionId;

	@Column(name = "start_time")
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime startedOn;

	@Column(name = "end_time")
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime completedOn;

	@Column(name = "job_name")
	private String jobName;

	@Column(name = "status")
	private String statusCode;

	@Column(name = "exit_code")
	private String exitCode;

	@Column(name = "exit_message")
	private String exitMessage;

	public long getJobInstanceId() {
		return jobInstanceId;
	}

	public void setJobInstanceId(long jobInstanceId) {
		this.jobInstanceId = jobInstanceId;
	}

	public long getJobExecutionId() {
		return jobExecutionId;
	}

	public void setJobExecutionId(long jobExecutionId) {
		this.jobExecutionId = jobExecutionId;
	}

	public DateTime getStartedOn() {
		return startedOn;
	}

	public void setStartedOn(DateTime startedOn) {
		this.startedOn = startedOn;
	}

	public DateTime getCompletedOn() {
		return completedOn;
	}

	public void setCompletedOn(DateTime completedOn) {
		this.completedOn = completedOn;
	}

	public String getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}

	public String getExitCode() {
		return exitCode;
	}

	public void setExitCode(String exitCode) {
		this.exitCode = exitCode;
	}

	public String getExitMessage() {
		return exitMessage;
	}

	public void setExitMessage(String exitMessage) {
		this.exitMessage = exitMessage;
	}

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

}
