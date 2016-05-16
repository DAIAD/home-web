package eu.daiad.web.model.scheduling;

import java.util.List;

public class JobExecutionInfo {

	private long jobId;

	private long instanceId;

	private long executionId;

	private long startedOn;

	private Long completedOn;

	private String statusCode;

	private String exitCode;

	private List<JobExecutionParameter> parameters;

	public long getJobId() {
		return jobId;
	}

	public void setJobId(long jobId) {
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

	public String getExitCode() {
		return exitCode;
	}

	public void setExitCode(String exitCode) {
		this.exitCode = exitCode;
	}

	public List<JobExecutionParameter> getParameters() {
		return parameters;
	}

	public void setParameters(List<JobExecutionParameter> parameters) {
		this.parameters = parameters;
	}

}
