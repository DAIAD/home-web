package eu.daiad.web.model.scheduling;

import java.util.List;

import eu.daiad.web.model.RestResponse;

public class JobCollectionResponse extends RestResponse {

	private List<JobInfo> jobs;

	public JobCollectionResponse(List<JobInfo> jobs) {
		this.jobs = jobs;
	}

	public List<JobInfo> getJobs() {
		return jobs;
	}

}
