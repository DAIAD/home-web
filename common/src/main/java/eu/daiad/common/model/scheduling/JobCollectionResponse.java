package eu.daiad.common.model.scheduling;

import java.util.List;

import eu.daiad.common.model.RestResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class JobCollectionResponse extends RestResponse {

	@Getter
	private List<JobInfo> jobs;

	public JobCollectionResponse(List<JobInfo> jobs) {
		this.jobs = jobs;
	}


}
