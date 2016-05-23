package eu.daiad.web.model.scheduling;

import java.util.List;

import eu.daiad.web.model.RestResponse;

public class JobResponse extends RestResponse {

	private JobInfo job;

	private List<JobExecutionInfo> executions;

	public JobInfo getJob() {
		return job;
	}

	public void setJob(JobInfo job) {
		this.job = job;
	}

	public List<JobExecutionInfo> getExecutions() {
		return executions;
	}

	public void setExecutions(List<JobExecutionInfo> executions) {
		this.executions = executions;
	}

}
