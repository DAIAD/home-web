package eu.daiad.common.domain.admin;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.joda.time.LocalDateTime;

import eu.daiad.common.model.scheduling.EnumExecutionExitCode;

@Entity(name = "scheduled_job_execution")
@Table(schema = "batch", name = "scheduled_job_execution")
public class ScheduledJobExecutionEntity {

    @Id()
    @Column(name = "job_instance_id")
    private long jobInstanceId;

    @Column(name = "job_id")
    private Long jobId;

    @Column(name = "job_key")
    private String jobKey;

    @Column(name = "job_execution_id")
    private long jobExecutionId;

    @Column(name = "start_time")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentLocalDateTime")
    private LocalDateTime startedOn;

    @Column(name = "end_time")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentLocalDateTime")
    private LocalDateTime completedOn;

    @Column(name = "job_name")
    private String jobName;

    @Column(name = "status")
    private String statusCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "exit_code")
    private EnumExecutionExitCode exitCode;

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

    public LocalDateTime getStartedOn() {
        return startedOn;
    }

    public void setStartedOn(LocalDateTime startedOn) {
        this.startedOn = startedOn;
    }

    public LocalDateTime getCompletedOn() {
        return completedOn;
    }

    public void setCompletedOn(LocalDateTime completedOn) {
        this.completedOn = completedOn;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public EnumExecutionExitCode getExitCode() {
        return exitCode;
    }

    public void setExitCode(EnumExecutionExitCode exitCode) {
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

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public String getJobKey() {
        return jobKey;
    }

    public void setJobKey(String jobKey) {
        this.jobKey = jobKey;
    }

}
