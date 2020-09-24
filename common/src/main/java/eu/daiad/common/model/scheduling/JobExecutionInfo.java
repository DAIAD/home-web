package eu.daiad.common.model.scheduling;

import java.util.List;

public class JobExecutionInfo {

	private Long jobId;

	private String jobName;
	
	private long instanceId;

	private long executionId;

	private long startedOn;

	private Long completedOn;

	private String statusCode;

	private EnumExecutionExitCode exitCode;

	private List<JobExecutionParameter> parameters;

	public Long getJobId() {
		return jobId;
	}

	public void setJobId(Long jobId) {
		this.jobId = jobId;
	}

	public long getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(long instanceId) {
		this.instanceId = instanceId;
	}

	public long getExecutionId() {
		return executionId;
	}

	public void setExecutionId(long executionId) {
		this.executionId = executionId;
	}

	public long getStartedOn() {
		return startedOn;
	}

	public void setStartedOn(long startedOn) {
		this.startedOn = startedOn;
	}

	public Long getCompletedOn() {
		return completedOn;
	}

	public void setCompletedOn(Long completedOn) {
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

	public List<JobExecutionParameter> getParameters() {
		return parameters;
	}

	public void setParameters(List<JobExecutionParameter> parameters) {
		this.parameters = parameters;
	}

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

}
